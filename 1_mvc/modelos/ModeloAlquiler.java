package sigepac.mvc.modelos;

import sigepac.mvc.configuracion.BaseDatos;
import java.util.*;

public class ModeloAlquiler {
    private final BaseDatos bd = BaseDatos.obtenerInstancia();

    public boolean crear(int idNegocio, String fechaInicio, String fechaLimite, double valor) {
        synchronized (bd) {
            if (!bd.tablaNegocios.containsKey(idNegocio)) {
                return false;
            }
            int nuevoId = bd.obtenerSiguienteIdAlquiler();
            Map<String, Object> alquiler = new HashMap<>();
            alquiler.put("id_alquiler", nuevoId);
            alquiler.put("id_negocio", idNegocio);
            alquiler.put("fecha_inicio", fechaInicio);
            alquiler.put("fecha_limite", fechaLimite);
            alquiler.put("valor_total", valor);
            alquiler.put("saldo_pendiente", valor);
            alquiler.put("estado", "PENDIENTE");
            bd.tablaAlquileres.put(nuevoId, alquiler);
            return true;
        }
    }

    public boolean actualizarDeuda(int idAlquiler, double nuevoValor, String nuevaFechaLimite) {
        synchronized (bd) {
            Map<String, Object> alq = bd.tablaAlquileres.get(idAlquiler);
            if (alq == null) return false;
            alq.put("valor_total", nuevoValor);
            alq.put("saldo_pendiente", nuevoValor);
            alq.put("fecha_limite", nuevaFechaLimite);
            return true;
        }
    }

    public List<Map<String, Object>> obtenerTodos() {
        synchronized (bd) {
            List<Map<String, Object>> lista = new ArrayList<>();
            for (Map<String, Object> a : bd.tablaAlquileres.values()) {
                Map<String, Object> copia = new HashMap<>(a);
                Map<String, Object> neg = bd.tablaNegocios.get((Integer) a.get("id_negocio"));
                if (neg != null) {
                    copia.put("nombre_negocio", neg.get("nombre"));
                    copia.put("local_numero", neg.get("local_numero"));
                }
                lista.add(copia);
            }
            return lista;
        }
    }

    public List<Map<String, Object>> obtenerActivosPorNegocio(int idNegocio) {
        synchronized (bd) {
            List<Map<String, Object>> lista = new ArrayList<>();
            for (Map<String, Object> a : bd.tablaAlquileres.values()) {
                if ((Integer) a.get("id_negocio") == idNegocio && (Double) a.get("saldo_pendiente") > 0) {
                    lista.add(new HashMap<>(a));
                }
            }
            return lista;
        }
    }
}
