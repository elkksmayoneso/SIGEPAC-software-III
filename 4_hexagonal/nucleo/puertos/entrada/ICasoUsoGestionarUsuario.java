package sigepac.hexagonal.nucleo.puertos.entrada;

import sigepac.hexagonal.nucleo.dominio.modelos.Usuario;
import java.util.List;

public interface ICasoUsoGestionarUsuario {
    Usuario registrarUsuario(String nombre, String apellido, String correo, String contrasena, String rol, String telefono);
    boolean actualizarUsuario(int id, String nombre, String apellido, String correo, String rol, String telefono);
    boolean eliminarUsuario(int id);
    List<Usuario> listarUsuarios();
    Usuario buscarPorId(int id);
}
