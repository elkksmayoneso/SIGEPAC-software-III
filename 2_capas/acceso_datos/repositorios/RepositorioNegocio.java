package sigepac.capas.acceso_datos.repositorios;

import sigepac.capas.acceso_datos.contratos.IRepositorioNegocio;
import sigepac.capas.dominio.entidades.EntidadNegocio;
import sigepac.capas.infraestructura.BaseDatosCapas;
import java.util.*;

public class RepositorioNegocio implements IRepositorioNegocio {
    private final BaseDatosCapas bd = BaseDatosCapas.obtenerInstancia();

    @Override
    public EntidadNegocio buscarPorId(int id) {
        synchronized (bd) {
            return bd.tablaNegocios.get(id);
        }
    }

    @Override
    public boolean existe(int id) {
        synchronized (bd) {
            return bd.tablaNegocios.containsKey(id);
        }
    }

    @Override
    public List<EntidadNegocio> obtenerTodos() {
        synchronized (bd) {
            return new ArrayList<>(bd.tablaNegocios.values());
        }
    }

    @Override
    public int guardar(EntidadNegocio negocio) {
        synchronized (bd) {
            int nuevoId = bd.siguienteIdNegocio();
            negocio.setId(nuevoId);
            bd.tablaNegocios.put(nuevoId, negocio);
            return nuevoId;
        }
    }

    @Override
    public boolean eliminar(int id) {
        synchronized (bd) {
            return bd.tablaNegocios.remove(id) != null;
        }
    }
}
