package sigepac.mvc.modelos;

import sigepac.mvc.configuracion.BaseDatos;
import java.util.*;

public class ModeloPago {
    private final BaseDatos bd = BaseDatos.obtenerInstancia();

    public boolean registrarPago(int idNegocio, double monto, String fechaPago, String metodo) throws Exception {
        if (monto <= 0) {
            throw new IllegalArgumentException("El monto a pagar debe ser mayor a cero.");
        }

        // RNF5: Bloqueo transaccional sincronizado
        synchronized (bd) {
            bd.iniciarTransaccion();
            try {
                // 1. Validar existencia del negocio (HU-05)
                if (!bd.tablaNegocios.containsKey(idNegocio)) {
                    throw new IllegalArgumentException("Error: El negocio no existe en la base de datos. Debe registrarlo previamente.");
                }

                // 2. Obtener deudas pendientes
                List<Map<String, Object>> deudasPendientes = new ArrayList<>();
                for (Map<String, Object> a : bd.tablaAlquileres.values()) {
                    if ((Integer) a.get("id_negocio") == idNegocio && (Double) a.get("saldo_pendiente") > 0) {
                        deudasPendientes.add(a);
                    }
                }

                if (deudasPendientes.isEmpty()) {
                    throw new IllegalStateException("El negocio no presenta deudas ni alquileres pendientes con saldo por liquidar.");
                }

                double montoRestante = monto;

                for (Map<String, Object> deuda : deudasPendientes) {
                    if (montoRestante <= 0) break;

                    int idAlquiler = (Integer) deuda.get("id_alquiler");
                    double saldoActual = (Double) deuda.get("saldo_pendiente");
                    double montoAplicado;
                    double nuevoSaldo;
                    String nuevoEstado;

                    if (montoRestante >= saldoActual) {
                        montoAplicado = saldoActual;
                        nuevoSaldo = 0.00;
                        nuevoEstado = "PAGADO";
                        montoRestante -= saldoActual;
                    } else {
                        montoAplicado = montoRestante;
                        nuevoSaldo = saldoActual - montoRestante;
                        nuevoEstado = "PENDIENTE";
                        montoRestante = 0.00;
                    }

                    // 3. Registrar el pago
                    int idNuevoPago = bd.obtenerSiguienteIdPago();
                    Map<String, Object> pago = new HashMap<>();
                    pago.put("id_pago", idNuevoPago);
                    pago.put("id_alquiler", idAlquiler);
                    pago.put("monto", montoAplicado);
                    pago.put("fecha_pago", fechaPago);
                    pago.put("metodo_pago", metodo);
                    bd.tablaPagos.put(idNuevoPago, pago);

                    // 4. Actualizar inmediatamente el saldo de la deuda (HU-05, RNF5)
                    deuda.put("saldo_pendiente", nuevoSaldo);
                    deuda.put("estado", nuevoEstado);
                }

                bd.confirmarTransaccion();
                return true;
            } catch (Exception e) {
                bd.cancelarTransaccion();
                throw e;
            }
        }
    }

    public List<Map<String, Object>> obtenerTodos() {
        synchronized (bd) {
            List<Map<String, Object>> lista = new ArrayList<>();
            for (Map<String, Object> p : bd.tablaPagos.values()) {
                Map<String, Object> copia = new HashMap<>(p);
                Map<String, Object> alq = bd.tablaAlquileres.get((Integer) p.get("id_alquiler"));
                if (alq != null) {
                    Map<String, Object> neg = bd.tablaNegocios.get((Integer) alq.get("id_negocio"));
                    if (neg != null) {
                        copia.put("nombre_negocio", neg.get("nombre"));
                        copia.put("local_numero", neg.get("local_numero"));
                    }
                }
                lista.add(copia);
            }
            return lista;
        }
    }
}
