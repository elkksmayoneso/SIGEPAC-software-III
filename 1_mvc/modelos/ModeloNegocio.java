package sigepac.mvc.modelos;

import sigepac.mvc.configuracion.BaseDatos;
import java.util.*;

public class ModeloNegocio {
    private final BaseDatos bd = BaseDatos.obtenerInstancia();

    public Map<String, Object> buscarPorId(int id) {
        synchronized (bd) {
            Map<String, Object> n = bd.tablaNegocios.get(id);
            if (n == null) return null;
            Map<String, Object> copia = new HashMap<>(n);
            Map<String, Object> usuario = bd.tablaUsuarios.get((Integer) n.get("id_usuario"));
            if (usuario != null) {
                copia.put("propietario", usuario.get("nombre") + " " + usuario.get("apellido"));
            }
            return copia;
        }
    }

    public List<Map<String, Object>> obtenerTodos() {
        synchronized (bd) {
            List<Map<String, Object>> lista = new ArrayList<>();
            for (Map<String, Object> n : bd.tablaNegocios.values()) {
                Map<String, Object> copia = new HashMap<>(n);
                Map<String, Object> usuario = bd.tablaUsuarios.get((Integer) n.get("id_usuario"));
                if (usuario != null) {
                    copia.put("propietario", usuario.get("nombre") + " " + usuario.get("apellido"));
                }
                lista.add(copia);
            }
            return lista;
        }
    }

    public boolean crear(int idUsuario, String nombre, String localNumero) {
        synchronized (bd) {
            if (!bd.tablaUsuarios.containsKey(idUsuario)) {
                return false;
            }
            int nuevoId = bd.obtenerSiguienteIdNegocio();
            Map<String, Object> negocio = new HashMap<>();
            negocio.put("id_negocio", nuevoId);
            negocio.put("id_usuario", idUsuario);
            negocio.put("nombre", nombre);
            negocio.put("local_numero", localNumero);
            negocio.put("estado", "ACTIVO");
            bd.tablaNegocios.put(nuevoId, negocio);
            return true;
        }
    }

    public boolean actualizar(int id, String nombre, String localNumero, String estado) {
        synchronized (bd) {
            Map<String, Object> negocio = bd.tablaNegocios.get(id);
            if (negocio == null) return false;
            negocio.put("nombre", nombre);
            negocio.put("local_numero", localNumero);
            negocio.put("estado", estado);
            return true;
        }
    }

    public boolean eliminar(int id) {
        synchronized (bd) {
            return bd.tablaNegocios.remove(id) != null;
        }
    }
}
