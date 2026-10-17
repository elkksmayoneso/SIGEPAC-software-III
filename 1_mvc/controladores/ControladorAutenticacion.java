package sigepac.mvc.controladores;

import sigepac.mvc.modelos.contratos.IModeloUsuario;
import sigepac.mvc.configuracion.IPuertoCifradorContrasena;
import java.util.Map;

public class ControladorAutenticacion {
    private final IModeloUsuario modeloUsuario;
    private final IPuertoCifradorContrasena cifrador;
    public ControladorAutenticacion(IModeloUsuario modeloUsuario, IPuertoCifradorContrasena cifrador) { this.modeloUsuario = modeloUsuario; this.cifrador = cifrador; }

    public Map<String, Object> iniciarSesion(String correo, String contrasena) throws Exception {
        if (correo == null || correo.trim().isEmpty() || contrasena == null || contrasena.trim().isEmpty()) {
            throw new IllegalArgumentException("Todos los campos son obligatorios.");
        }

        Map<String, Object> usuario = modeloUsuario.buscarPorCorreo(correo.trim());

        // HU-01 & RNF3: Validación de credenciales
        if (usuario == null || !cifrador.verificar(contrasena.trim(), (String) usuario.get("contrasena_hash"))) {
            throw new SecurityException("Credenciales incorrectas. Por favor, verifique el usuario y la contraseña.");
        }

        return usuario;
    }
}
