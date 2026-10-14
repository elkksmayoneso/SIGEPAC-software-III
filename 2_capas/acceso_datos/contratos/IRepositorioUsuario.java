package sigepac.capas.acceso_datos.contratos;

import sigepac.capas.dominio.entidades.EntidadUsuario;
import java.util.List;

public interface IRepositorioUsuario {
    EntidadUsuario buscarPorCorreo(String correo);
    EntidadUsuario buscarPorId(int id);
    List<EntidadUsuario> obtenerTodos();
    int guardar(EntidadUsuario usuario);
    boolean eliminar(int id);
}
