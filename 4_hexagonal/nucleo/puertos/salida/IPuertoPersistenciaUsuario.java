package sigepac.hexagonal.nucleo.puertos.salida;

import sigepac.hexagonal.nucleo.dominio.modelos.Usuario;
import java.util.List;
import java.util.Optional;

public interface IPuertoPersistenciaUsuario {
    Usuario guardar(Usuario usuario);
    boolean actualizar(Usuario usuario);
    boolean eliminar(int id);
    Optional<Usuario> buscarPorId(int id);
    Optional<Usuario> buscarPorCorreo(String correo);
    List<Usuario> listarTodos();
}
