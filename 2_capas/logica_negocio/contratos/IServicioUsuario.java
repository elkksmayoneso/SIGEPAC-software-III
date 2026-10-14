package sigepac.capas.logica_negocio.contratos;

import sigepac.capas.dominio.entidades.*;
import java.util.List;

public interface IServicioUsuario {
    List<EntidadUsuario> listarUsuarios();
    int registrarUsuario(String nombre, String apellido, String correo, String contrasena, String rol, String telefono);
    boolean eliminarUsuario(int id);
}
