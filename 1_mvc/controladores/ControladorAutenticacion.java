package sigepac.mvc.controladores;

import sigepac.mvc.modelos.ModeloUsuario;
import sigepac.mvc.configuracion.BaseDatos;
import java.util.Map;

public class ControladorAutenticacion {
    private final ModeloUsuario modeloUsuario = new ModeloUsuario();

    public Map<String, Object> iniciarSesion(String correo, String contrasena) throws Exception {
        if (correo == null || correo.trim().isEmpty() || contrasena == null || contrasena.trim().isEmpty()) {
            throw new IllegalArgumentException("Todos los campos son obligatorios.");
        }

        Map<String, Object> usuario = modeloUsuario.buscarPorCorreo(correo.trim());

        // HU-01 & RNF3: Validación de credenciales
        if (usuario == null || !BaseDatos.verificarContrasena(contrasena.trim(), (String) usuario.get("contrasena_hash"))) {
            throw new SecurityException("Credenciales incorrectas. Por favor, verifique el usuario y la contraseña.");
        }

        return usuario;
    }
}
