package sigepac.hexagonal.adaptadores.entrada.web;

import sigepac.hexagonal.nucleo.puertos.entrada.ICasoUsoGestionarUsuario;
import sigepac.hexagonal.nucleo.dominio.modelos.Usuario;
import java.util.List;

public class ControladorUsuarioHexagonal {
    private final ICasoUsoGestionarUsuario casoUsoUsuario;

    public ControladorUsuarioHexagonal(ICasoUsoGestionarUsuario casoUsoUsuario) {
        this.casoUsoUsuario = casoUsoUsuario;
    }

    public Usuario crear(String nombre, String apellido, String correo, String contrasena, String rol, String telefono) {
        return casoUsoUsuario.registrarUsuario(nombre, apellido, correo, contrasena, rol, telefono);
    }

    public boolean actualizar(int id, String nombre, String apellido, String correo, String rol, String telefono) {
        return casoUsoUsuario.actualizarUsuario(id, nombre, apellido, correo, rol, telefono);
    }

    public boolean eliminar(int id) {
        return casoUsoUsuario.eliminarUsuario(id);
    }

    public List<Usuario> listar() {
        return casoUsoUsuario.listarUsuarios();
    }

    public Usuario buscar(int id) {
        return casoUsoUsuario.buscarPorId(id);
    }
}
