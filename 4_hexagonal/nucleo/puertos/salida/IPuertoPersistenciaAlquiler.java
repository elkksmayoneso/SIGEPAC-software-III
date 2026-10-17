package sigepac.hexagonal.nucleo.puertos.salida;

import sigepac.hexagonal.nucleo.dominio.modelos.Alquiler;
import java.util.List;
import java.util.Optional;

public interface IPuertoPersistenciaAlquiler {
    Alquiler guardar(Alquiler alquiler);
    boolean actualizar(Alquiler alquiler);
    Optional<Alquiler> buscarPorId(int id);
    List<Alquiler> listarTodos();
    List<Alquiler> listarPorNegocio(int negocioId);
    Optional<Alquiler> buscarActivoPorNegocio(int negocioId);
    List<Alquiler> listarActivosPorNegocio(int negocioId);
}
