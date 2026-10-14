package sigepac.capas.presentacion.controladores;

import sigepac.capas.logica_negocio.contratos.IServicioUsuario;
import sigepac.capas.dominio.entidades.EntidadUsuario;
import java.util.List;

public class ControladorUsuarioCapas {
    private final IServicioUsuario servicioUsuario;

    public ControladorUsuarioCapas(IServicioUsuario servicioUsuario) {
        this.servicioUsuario = servicioUsuario;
    }

    public List<EntidadUsuario> listar() {
        return servicioUsuario.listarUsuarios();
    }

    public int crear(String nombre, String apellido, String correo, String contrasena, String rol, String telefono) {
        return servicioUsuario.registrarUsuario(nombre, apellido, correo, contrasena, rol, telefono);
    }

    public boolean eliminar(int id) {
        return servicioUsuario.eliminarUsuario(id);
    }
}
