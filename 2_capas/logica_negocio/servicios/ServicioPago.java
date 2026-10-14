package sigepac.capas.logica_negocio.servicios;

import sigepac.capas.logica_negocio.contratos.IServicioPago;
import sigepac.capas.acceso_datos.contratos.*;
import sigepac.capas.dominio.entidades.*;
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
    public synchronized boolean registrarPago(int idNegocio, double monto, String fechaPago, String formaPago) throws Exception {
        // HU-05: El flujo debe requerir buscar y seleccionar el negocio primero; si no existe, error explícito
        if (!repoNegocio.existe(idNegocio)) {
            throw new IllegalArgumentException("Error: El negocio no existe en el sistema. Registre el negocio previamente.");
        }

        if (monto <= 0) {
            throw new IllegalArgumentException("El monto a pagar debe ser mayor a cero.");
        }

        // RNF5: Transacción ACID con consistencia inmediata
        List<EntidadAlquiler> deudas = repoAlquiler.obtenerActivosPorNegocio(idNegocio);
        if (deudas.isEmpty()) {
            throw new IllegalStateException("El negocio no posee alquileres pendientes con saldo por liquidar.");
        }

        double montoRestante = monto;
        EntidadNegocio negocio = repoNegocio.buscarPorId(idNegocio);

        for (EntidadAlquiler deuda : deudas) {
            if (montoRestante <= 0) break;

            double saldo = deuda.getSaldoActual();
            double abono = Math.min(montoRestante, saldo);
            double nuevoSaldo = saldo - abono;
            String nuevoEstado = (nuevoSaldo == 0.0) ? "AL_DIA" : "PENDIENTE";

            // Guardar pago
            EntidadPago pago = new EntidadPago(
                null,
                deuda.getId(),
                abono,
                fechaPago,
                formaPago != null ? formaPago : "EFECTIVO",
                negocio.getNombre()
            );
            repoPago.guardar(pago);

            // Actualizar inmediatamente saldo de la deuda (HU-05)
            repoAlquiler.actualizarSaldoYEstado(deuda.getId(), nuevoSaldo, nuevoEstado);

            montoRestante -= abono;
        }

        return true;
    }
}
