package sigepac.hexagonal.adaptadores.salida.persistencia;

import sigepac.hexagonal.nucleo.puertos.salida.IPuertoPersistenciaAlquiler;
import sigepac.hexagonal.nucleo.dominio.modelos.Alquiler;

import java.util.*;
import java.util.stream.Collectors;

public class AdaptadorPersistenciaAlquiler implements IPuertoPersistenciaAlquiler {
    private final BaseDatosHexagonal bd = BaseDatosHexagonal.getInstancia();

    @Override
    public Alquiler guardar(Alquiler alquiler) {
        int id = bd.secuenciaAlquiler.getAndIncrement();
        Alquiler nuevo = new Alquiler(
                id,
                alquiler.getNegocioId(),
                alquiler.getFechaInicio(),
                alquiler.getFechaCorte(),
                alquiler.getCanonMensual(),
                alquiler.getSaldoPorPagar(),
                alquiler.getEstadoDeuda()
        );
        bd.tablaAlquileres.put(id, nuevo);
        return nuevo;
    }

    @Override
    public boolean actualizar(Alquiler alquiler) {
        if (alquiler.getId() != null && bd.tablaAlquileres.containsKey(alquiler.getId())) {
            bd.tablaAlquileres.put(alquiler.getId(), alquiler);
            return true;
        }
        return false;
    }

    @Override
    public Optional<Alquiler> buscarPorId(int id) {
        return Optional.ofNullable(bd.tablaAlquileres.get(id));
    }

    @Override
    public List<Alquiler> listarTodos() {
        return new ArrayList<>(bd.tablaAlquileres.values());
    }

    @Override
    public List<Alquiler> listarPorNegocio(int negocioId) {
        return bd.tablaAlquileres.values().stream()
                .filter(a -> a.getNegocioId() == negocioId)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Alquiler> buscarActivoPorNegocio(int negocioId) {
        return bd.tablaAlquileres.values().stream()
                .filter(a -> a.getNegocioId() == negocioId && a.getSaldoPorPagar() > 0)
                .findFirst();
    }
}
