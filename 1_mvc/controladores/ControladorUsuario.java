package sigepac.mvc.controladores;

import sigepac.mvc.modelos.ModeloUsuario;
import java.util.*;

public class ControladorUsuario {
    private final ModeloUsuario modeloUsuario = new ModeloUsuario();

    public List<Map<String, Object>> listarUsuarios() {
        return modeloUsuario.obtenerTodos();
    }

    public boolean guardarUsuario(String nombre, String apellido, String correo, String contrasena, String rol, String telefono) {
        // HU-02: Exigir campos obligatorios
        if (nombre == null || nombre.trim().isEmpty() ||
            apellido == null || apellido.trim().isEmpty() ||
            correo == null || correo.trim().isEmpty() ||
            contrasena == null || contrasena.trim().isEmpty() ||
            telefono == null || telefono.trim().isEmpty()) {
            throw new IllegalArgumentException("Todos los campos son obligatorios. Complete la información faltante antes de guardar.");
        }

        boolean exito = modeloUsuario.crear(nombre.trim(), apellido.trim(), correo.trim(), contrasena.trim(), rol != null ? rol : "PROPIETARIO", telefono.trim());
        if (!exito) {
            throw new IllegalStateException("No se pudo registrar el usuario. El correo ya se encuentra en uso.");
        }
        return true;
    }

    public boolean eliminarUsuario(int id) {
        return modeloUsuario.eliminar(id);
    }
}
