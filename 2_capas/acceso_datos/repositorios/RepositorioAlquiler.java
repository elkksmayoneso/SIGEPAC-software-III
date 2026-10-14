package sigepac.capas.acceso_datos.repositorios;

import sigepac.capas.acceso_datos.contratos.IRepositorioAlquiler;
import sigepac.capas.dominio.entidades.EntidadAlquiler;
import sigepac.capas.infraestructura.BaseDatosCapas;
import java.util.*;

public class RepositorioAlquiler implements IRepositorioAlquiler {
    private final BaseDatosCapas bd = BaseDatosCapas.obtenerInstancia();

    @Override
    public List<EntidadAlquiler> obtenerTodos() {
        synchronized (bd) {
            return new ArrayList<>(bd.tablaAlquileres.values());
        }
    }

    @Override
    public List<EntidadAlquiler> obtenerActivosPorNegocio(int idNegocio) {
        synchronized (bd) {
            List<EntidadAlquiler> lista = new ArrayList<>();
            for (EntidadAlquiler a : bd.tablaAlquileres.values()) {
                if (a.getIdNegocio() == idNegocio && a.getSaldoActual() > 0) {
                    lista.add(a);
                }
            }
            return lista;
        }
    }

    @Override
    public int guardar(EntidadAlquiler alquiler) {
        synchronized (bd) {
            int nuevoId = bd.siguienteIdAlquiler();
            alquiler.setId(nuevoId);
            bd.tablaAlquileres.put(nuevoId, alquiler);
            return nuevoId;
        }
    }

    @Override
    public boolean actualizarSaldoYEstado(int idAlquiler, double nuevoSaldo, String nuevoEstado) {
        synchronized (bd) {
            EntidadAlquiler alq = bd.tablaAlquileres.get(idAlquiler);
            if (alq == null) return false;
            alq.setSaldoActual(nuevoSaldo);
            alq.setEstado(nuevoEstado);
            return true;
        }
    }
}
