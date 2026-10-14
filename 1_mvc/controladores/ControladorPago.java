package sigepac.mvc.controladores;

import sigepac.mvc.modelos.ModeloPago;
import sigepac.mvc.modelos.ModeloNegocio;
import java.util.*;

public class ControladorPago {
    private final ModeloPago modeloPago = new ModeloPago();
    private final ModeloNegocio modeloNegocio = new ModeloNegocio();

    public List<Map<String, Object>> listarPagos() {
        return modeloPago.obtenerTodos();
    }

    public boolean procesarPago(int idNegocio, double monto, String fechaPago, String metodo) throws Exception {
        // HU-05: El flujo debe requerir buscar y seleccionar el negocio primero; si no existe, error explícito
        Map<String, Object> negocio = modeloNegocio.buscarPorId(idNegocio);
        if (negocio == null) {
            throw new IllegalArgumentException("Error: El negocio no existe en el sistema. Debe registrarlo previamente antes de recibir pagos.");
        }

        if (monto <= 0) {
            throw new IllegalArgumentException("El monto del abono debe ser mayor a cero.");
        }

        // Registrar pago y actualizar saldo inmediatamente
        return modeloPago.registrarPago(idNegocio, monto, fechaPago, metodo != null ? metodo : "EFECTIVO");
    }
}
