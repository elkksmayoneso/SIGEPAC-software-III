package sigepac.hexagonal.nucleo.puertos.salida;

import sigepac.hexagonal.nucleo.dominio.modelos.Negocio;
import java.util.List;
import java.util.Optional;

public interface IPuertoPersistenciaNegocio {
    Negocio guardar(Negocio negocio);
    boolean actualizar(Negocio negocio);
    boolean eliminar(int id);
    Optional<Negocio> buscarPorId(int id);
    Optional<Negocio> buscarPorLocal(String numeroLocal);
    List<Negocio> listarTodos();
}
