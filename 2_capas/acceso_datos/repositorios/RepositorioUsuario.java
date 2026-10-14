package sigepac.capas.acceso_datos.repositorios;

import sigepac.capas.acceso_datos.contratos.IRepositorioUsuario;
import sigepac.capas.dominio.entidades.EntidadUsuario;
import sigepac.capas.infraestructura.BaseDatosCapas;
import java.util.*;

public class RepositorioUsuario implements IRepositorioUsuario {
    private final BaseDatosCapas bd = BaseDatosCapas.obtenerInstancia();

    @Override
    public EntidadUsuario buscarPorCorreo(String correo) {
        synchronized (bd) {
            for (EntidadUsuario u : bd.tablaUsuarios.values()) {
                if (correo.equalsIgnoreCase(u.getCorreo())) {
                    return u;
                }
            }
            return null;
        }
    }

    @Override
    public EntidadUsuario buscarPorId(int id) {
        synchronized (bd) {
            return bd.tablaUsuarios.get(id);
        }
    }

    @Override
    public List<EntidadUsuario> obtenerTodos() {
        synchronized (bd) {
            return new ArrayList<>(bd.tablaUsuarios.values());
        }
    }

    @Override
    public int guardar(EntidadUsuario usuario) {
        synchronized (bd) {
            int nuevoId = bd.siguienteIdUsuario();
            usuario.setId(nuevoId);
            bd.tablaUsuarios.put(nuevoId, usuario);
            return nuevoId;
        }
    }

    @Override
    public boolean eliminar(int id) {
        synchronized (bd) {
            return bd.tablaUsuarios.remove(id) != null;
        }
    }
}
