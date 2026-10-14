package sigepac.poo.infraestructura.persistencia;

import sigepac.poo.dominio.repositorios.IRepositorioNegocioComercial;
import sigepac.poo.dominio.entidades.NegocioComercial;
import sigepac.poo.infraestructura.BaseDatosPoo;
import java.util.*;

public class RepositorioNegocioComercialMemoria implements IRepositorioNegocioComercial {
    private final BaseDatosPoo bd = BaseDatosPoo.obtenerInstancia();

    @Override
    public NegocioComercial buscarPorId(int id) {
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
    public List<NegocioComercial> obtenerTodos() {
        synchronized (bd) {
            return new ArrayList<>(bd.tablaNegocios.values());
        }
    }

    @Override
    public int guardar(NegocioComercial negocio) {
        synchronized (bd) {
            int nuevoId = bd.siguienteIdNegocio();
            NegocioComercial entidadConId = new NegocioComercial(
                nuevoId,
                negocio.getIdPropietario(),
                negocio.getNombreComercial(),
                negocio.getNumeroLocal(),
                negocio.isEstaActivo(),
                negocio.getNombrePropietario()
            );
            bd.tablaNegocios.put(nuevoId, entidadConId);
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
