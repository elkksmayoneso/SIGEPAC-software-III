package sigepac.hexagonal.nucleo.aplicacion.servicios;

import sigepac.hexagonal.nucleo.puertos.entrada.ICasoUsoProcesarPago;
import sigepac.hexagonal.nucleo.puertos.salida.*;
import sigepac.hexagonal.nucleo.dominio.modelos.Pago;
import sigepac.hexagonal.nucleo.dominio.modelos.Alquiler;
import sigepac.hexagonal.nucleo.dominio.modelos.Negocio;
import sigepac.hexagonal.nucleo.dominio.excepciones.NegocioNoEncontradoExcepcion;
import sigepac.hexagonal.nucleo.dominio.excepciones.ValidacionDatosExcepcion;

import java.util.List;
import java.util.Optional;

public class ServicioProcesarPagoHexagonal implements ICasoUsoProcesarPago {
    private final IPuertoPersistenciaNegocio puertoNegocio;
    private final IPuertoPersistenciaAlquiler puertoAlquiler;
    private final IPuertoPersistenciaPago puertoPago;
    private final IPuertoTransaccion puertoTransaccion;

    public ServicioProcesarPagoHexagonal(
            IPuertoPersistenciaNegocio puertoNegocio,
            IPuertoPersistenciaAlquiler puertoAlquiler,
            IPuertoPersistenciaPago puertoPago,
            IPuertoTransaccion puertoTransaccion) {
        this.puertoNegocio = puertoNegocio;
        this.puertoAlquiler = puertoAlquiler;
        this.puertoPago = puertoPago;
        this.puertoTransaccion = puertoTransaccion;
    }

    @Override
    public Pago procesarPago(int negocioId, double monto, String fechaAbono, String formaAbono) {
        // Criterio HU-05: El flujo debe requerir buscar y seleccionar el negocio primero;
        // si no existe, debe arrojar un error solicitando registrarlo previamente.
        if (monto <= 0) {
            throw new ValidacionDatosExcepcion("El monto a pagar debe ser estrictamente mayor a 0.");
        }

        // Ejecución transaccional protegida contra concurrencia (RNF5)
        return puertoTransaccion.ejecutarEnTransaccion(() -> {
            Negocio negocio = puertoNegocio.buscarPorId(negocioId)
                    .orElseThrow(() -> new NegocioNoEncontradoExcepcion("El negocio no existe. Regístrelo antes de procesar pagos."));
            List<Alquiler> deudas = puertoAlquiler.listarActivosPorNegocio(negocioId);
            if (deudas.isEmpty()) throw new ValidacionDatosExcepcion("El negocio no tiene deudas pendientes.");
            java.math.BigDecimal restante = java.math.BigDecimal.valueOf(monto).setScale(2, java.math.RoundingMode.HALF_UP);
            Pago ultimoPago = null;
            for (Alquiler deuda : deudas) {
                if (restante.signum() <= 0) break;
                java.math.BigDecimal saldo = java.math.BigDecimal.valueOf(deuda.getSaldoPorPagar());
                java.math.BigDecimal abono = restante.min(saldo);
                deuda.abonar(abono.doubleValue());
                if (!puertoAlquiler.actualizar(deuda)) throw new IllegalStateException("No se pudo actualizar el saldo del alquiler.");
                Pago pago = new Pago(null, deuda.getId(), abono.doubleValue(), fechaAbono, formaAbono);
                pago.setNombreNegocio(negocio.getNombreComercial() + " (" + negocio.getNumeroLocal() + ")");
                ultimoPago = puertoPago.guardar(pago);
                restante = restante.subtract(abono);
            }
            if (restante.signum() > 0) throw new ValidacionDatosExcepcion("El monto supera el total de las deudas pendientes.");
            return ultimoPago;
        });
    }

    @Override
    public List<Pago> listarPagos() {
        List<Pago> pagos = puertoPago.listarTodos();
        for (Pago p : pagos) {
            Optional<Alquiler> alq = puertoAlquiler.buscarPorId(p.getAlquilerId());
            if (alq.isPresent()) {
                Optional<Negocio> neg = puertoNegocio.buscarPorId(alq.get().getNegocioId());
                neg.ifPresent(n -> p.setNombreNegocio(n.getNombreComercial() + " (" + n.getNumeroLocal() + ")"));
            }
        }
        return pagos;
    }
}
