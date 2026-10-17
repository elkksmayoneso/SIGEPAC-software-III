package sigepac.poo.aplicacion.manejadores;

import sigepac.poo.dominio.repositorios.IRepositorioDeudaAlquiler;
import sigepac.poo.dominio.repositorios.IRepositorioNegocioComercial;
import sigepac.poo.dominio.entidades.DeudaAlquiler;
import sigepac.poo.dominio.entidades.TransaccionPago;
import sigepac.poo.dominio.objetos_valor.Dinero;
import sigepac.poo.dominio.excepciones.NegocioNoEncontradoExcepcion;
import sigepac.poo.infraestructura.ConexionPoo;
import java.util.List;

public class ManejadorPago {
    private final IRepositorioDeudaAlquiler repoDeuda;
    private final IRepositorioNegocioComercial repoNegocio;

    public ManejadorPago(IRepositorioDeudaAlquiler repoDeuda, IRepositorioNegocioComercial repoNegocio) {
        this.repoDeuda = repoDeuda;
        this.repoNegocio = repoNegocio;
    }

    public List<TransaccionPago> listarPagos() {
        return repoDeuda.obtenerTodosLosPagos();
    }

    public boolean procesarPago(int idNegocio, double monto, String fechaPago, String metodo) {
        // HU-05: El flujo debe requerir buscar y seleccionar el negocio primero; si no existe, error explícito
        if (monto <= 0) {
            throw new IllegalArgumentException("El monto a pagar debe ser mayor a cero.");
        }

        return ConexionPoo.transaccion(() -> {
            if (!repoNegocio.existe(idNegocio)) throw new NegocioNoEncontradoExcepcion("El negocio no existe o está inactivo.");
            List<DeudaAlquiler> activas = repoDeuda.obtenerActivosPorNegocio(idNegocio);
            if (activas.isEmpty()) throw new IllegalStateException("El negocio no tiene deudas pendientes con saldo por liquidar.");
            java.math.BigDecimal restante = java.math.BigDecimal.valueOf(monto).setScale(2, java.math.RoundingMode.HALF_UP);
            for (DeudaAlquiler deuda : activas) {
                if (restante.signum() <= 0) break;
                java.math.BigDecimal saldo = java.math.BigDecimal.valueOf(deuda.getSaldoActual().getCantidad());
                java.math.BigDecimal aplicado = restante.min(saldo);
                TransaccionPago tx = deuda.registrarAbono(new Dinero(aplicado), metodo == null ? "EFECTIVO" : metodo, fechaPago);
                repoDeuda.registrarPago(tx);
                if (!repoDeuda.actualizarDeuda(deuda)) throw new IllegalStateException("No se pudo actualizar la deuda.");
                restante = restante.subtract(aplicado);
            }
            if (restante.signum() > 0) throw new IllegalArgumentException("El monto supera el total de las deudas pendientes.");
            return true;
        });
    }
}
