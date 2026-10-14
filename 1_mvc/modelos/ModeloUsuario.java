package sigepac.mvc.modelos;

import sigepac.mvc.configuracion.BaseDatos;
import java.util.*;

public class ModeloUsuario {
    private final BaseDatos bd = BaseDatos.obtenerInstancia();

    public Map<String, Object> buscarPorCorreo(String correo) {
        synchronized (bd) {
            for (Map<String, Object> u : bd.tablaUsuarios.values()) {
                if (correo.equalsIgnoreCase((String) u.get("correo"))) {
                    return new HashMap<>(u);
                }
            }
            return null;
        }
    }

    public Map<String, Object> buscarPorId(int id) {
        synchronized (bd) {
            Map<String, Object> u = bd.tablaUsuarios.get(id);
            return u != null ? new HashMap<>(u) : null;
        }
    }

    public List<Map<String, Object>> obtenerTodos() {
        synchronized (bd) {
            List<Map<String, Object>> lista = new ArrayList<>();
            for (Map<String, Object> u : bd.tablaUsuarios.values()) {
                lista.add(new HashMap<>(u));
            }
            return lista;
        }
    }

    public boolean crear(String nombre, String apellido, String correo, String contrasena, String rol, String telefono) {
        synchronized (bd) {
            if (buscarPorCorreo(correo) != null) {
                return false;
            }
            int nuevoId = bd.obtenerSiguienteIdUsuario();
            Map<String, Object> usuario = new HashMap<>();
            usuario.put("id_usuario", nuevoId);
            usuario.put("nombre", nombre);
            usuario.put("apellido", apellido);
            usuario.put("correo", correo);
            usuario.put("contrasena_hash", BaseDatos.cifrarContrasena(contrasena));
            usuario.put("rol", rol);
            usuario.put("telefono", telefono);
            bd.tablaUsuarios.put(nuevoId, usuario);
            return true;
        }
    }

    public boolean actualizar(int id, String nombre, String apellido, String telefono, String rol) {
        synchronized (bd) {
            Map<String, Object> usuario = bd.tablaUsuarios.get(id);
            if (usuario == null) return false;
            usuario.put("nombre", nombre);
            usuario.put("apellido", apellido);
            usuario.put("telefono", telefono);
            usuario.put("rol", rol);
            return true;
        }
    }

    public boolean eliminar(int id) {
        synchronized (bd) {
            return bd.tablaUsuarios.remove(id) != null;
        }
    }
}
