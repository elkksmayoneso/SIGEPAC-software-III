package sigepac.poo.presentacion.controladores;

import sigepac.poo.aplicacion.manejadores.ManejadorUsuario;
import sigepac.poo.dominio.entidades.CuentaUsuario;
import java.util.List;

public class ControladorUsuarioPoo {
    private final ManejadorUsuario manejadorUsuario;

    public ControladorUsuarioPoo(ManejadorUsuario manejadorUsuario) {
        this.manejadorUsuario = manejadorUsuario;
    }

    public List<CuentaUsuario> listar() {
        return manejadorUsuario.listar();
    }

    public int crear(String nombre, String apellido, String correo, String contrasena, String rol, String telefono) {
        return manejadorUsuario.registrar(nombre, apellido, correo, contrasena, rol, telefono);
    }

    public boolean eliminar(int id) {
        return manejadorUsuario.eliminar(id);
    }
}
