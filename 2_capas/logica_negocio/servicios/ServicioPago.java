package sigepac.capas.logica_negocio.servicios;

import sigepac.capas.logica_negocio.contratos.IServicioPago;
import sigepac.capas.acceso_datos.contratos.*;
import sigepac.capas.dominio.entidades.*;
import sigepac.capas.infraestructura.ConexionCapas;
import java.util.List;

public class ServicioPago implements IServicioPago {
    private final IRepositorioNegocio repoNegocio;
    private final IRepositorioAlquiler repoAlquiler;
    private final IRepositorioPago repoPago;

    public ServicioPago(IRepositorioNegocio repoNegocio, IRepositorioAlquiler repoAlquiler, IRepositorioPago repoPago) {
        this.repoNegocio = repoNegocio;
        this.repoAlquiler = repoAlquiler;
        this.repoPago = repoPago;
    }

    @Override
    public List<EntidadPago> listarPagos() {
        return repoPago.obtenerTodos();
    }

    @Override
    public boolean registrarPago(int idNegocio, double monto, String fechaPago, String formaPago) throws Exception {
        if (monto <= 0) {
            throw new IllegalArgumentException("El monto a pagar debe ser mayor a cero.");
        }

        return ConexionCapas.transaccion(() -> {
            if (!repoNegocio.existe(idNegocio)) throw new IllegalArgumentException("El negocio no existe o está inactivo.");
            List<EntidadAlquiler> deudas = repoAlquiler.obtenerActivosPorNegocio(idNegocio);
            if (deudas.isEmpty()) throw new IllegalStateException("El negocio no posee alquileres pendientes con saldo por liquidar.");
            EntidadNegocio negocio = repoNegocio.buscarPorId(idNegocio);
            if (negocio == null) throw new IllegalArgumentException("El negocio ya no está disponible.");
            double restante = monto;
            for (EntidadAlquiler deuda : deudas) {
                if (restante <= 0) break;
                double abono = Math.min(restante, deuda.getSaldoActual());
                double saldo = Math.max(0, deuda.getSaldoActual() - abono);
                repoPago.guardar(new EntidadPago(null, deuda.getId(), abono, fechaPago,
                        formaPago == null ? "EFECTIVO" : formaPago, negocio.getNombre()));
                if (!repoAlquiler.actualizarSaldoYEstado(deuda.getId(), saldo, saldo == 0 ? "PAGADO" : "PENDIENTE"))
                    throw new IllegalStateException("No fue posible actualizar el saldo del alquiler.");
                restante -= abono;
            }
            if (restante > 0.000001) throw new IllegalArgumentException("El monto supera el total de las deudas pendientes.");
            return true;
        });
    }
}
