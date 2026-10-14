package sigepac.poo.aplicacion.manejadores;

import sigepac.poo.dominio.repositorios.IRepositorioDeudaAlquiler;
import sigepac.poo.dominio.repositorios.IRepositorioNegocioComercial;
import sigepac.poo.dominio.entidades.DeudaAlquiler;
import sigepac.poo.dominio.entidades.TransaccionPago;
import sigepac.poo.dominio.objetos_valor.Dinero;
import sigepac.poo.dominio.excepciones.NegocioNoEncontradoExcepcion;
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

    public synchronized boolean procesarPago(int idNegocio, double monto, String fechaPago, String metodo) {
        // HU-05: El flujo debe requerir buscar y seleccionar el negocio primero; si no existe, error explícito
        if (!repoNegocio.existe(idNegocio)) {
            throw new NegocioNoEncontradoExcepcion("Error: El negocio no existe en el sistema. Registre el negocio previamente.");
        }

        if (monto <= 0) {
            throw new IllegalArgumentException("El monto a pagar debe ser mayor a cero.");
        }

        List<DeudaAlquiler> activas = repoDeuda.obtenerActivosPorNegocio(idNegocio);
        if (activas.isEmpty()) {
            throw new IllegalStateException("El negocio no tiene deudas pendientes con saldo por liquidar.");
        }

        double montoRestante = monto;

        for (DeudaAlquiler deuda : activas) {
            if (montoRestante <= 0) break;

            double saldoVal = deuda.getSaldoActual().getCantidad();
            double porcion = Math.min(montoRestante, saldoVal);

            // HU-05: El Aggregate Root aplica la lógica de abono y muta su saldo interno
            TransaccionPago tx = deuda.registrarAbono(new Dinero(porcion), metodo != null ? metodo : "EFECTIVO", fechaPago);

            // Persistir transacción y actualizar estado del Aggregate
            repoDeuda.registrarPago(tx);
            repoDeuda.actualizarDeuda(deuda);

            montoRestante -= porcion;
        }

        return true;
    }
}
