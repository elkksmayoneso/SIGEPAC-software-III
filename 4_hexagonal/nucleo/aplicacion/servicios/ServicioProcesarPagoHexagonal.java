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
        Negocio negocio = puertoNegocio.buscarPorId(negocioId)
                .orElseThrow(() -> new NegocioNoEncontradoExcepcion("Error: El negocio no existe en la base de datos. Debe registrar el negocio previamente para poder procesar pagos."));

        if (monto <= 0) {
            throw new ValidacionDatosExcepcion("El monto a pagar debe ser estrictamente mayor a 0.");
        }

        // Ejecución transaccional protegida contra concurrencia (RNF5)
        return puertoTransaccion.ejecutarEnTransaccion(() -> {
            Alquiler alquilerActivo = puertoAlquiler.buscarActivoPorNegocio(negocioId)
                    .orElseThrow(() -> new ValidacionDatosExcepcion("El negocio '" + negocio.getNombreComercial() + "' no posee deudas o alquileres pendientes de cobro."));

            // Descontar saldo y actualizar estado inmediatamente
            alquilerActivo.abonar(monto);
            puertoAlquiler.actualizar(alquilerActivo);

            // Registrar comprobante de pago
            Pago nuevoPago = new Pago(null, alquilerActivo.getId(), monto, fechaAbono, formaAbono);
            nuevoPago.setNombreNegocio(negocio.getNombreComercial() + " (" + negocio.getNumeroLocal() + ")");
            return puertoPago.guardar(nuevoPago);
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
