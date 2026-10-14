package sigepac.capas.logica_negocio.servicios;

import sigepac.capas.logica_negocio.contratos.IServicioAutenticacion;
import sigepac.capas.acceso_datos.contratos.IRepositorioUsuario;
import sigepac.capas.dominio.entidades.EntidadUsuario;
import sigepac.capas.infraestructura.BaseDatosCapas;

public class ServicioAutenticacion implements IServicioAutenticacion {
    private final IRepositorioUsuario repoUsuario;

    public ServicioAutenticacion(IRepositorioUsuario repoUsuario) {
        this.repoUsuario = repoUsuario;
    }

    @Override
    public EntidadUsuario autenticar(String correo, String contrasena) {
        if (correo == null || correo.trim().isEmpty() || contrasena == null || contrasena.trim().isEmpty()) {
            throw new IllegalArgumentException("Todos los campos son obligatorios.");
        }

        EntidadUsuario usuario = repoUsuario.buscarPorCorreo(correo.trim());

        // HU-01 & RNF3: Validación de credenciales
        if (usuario == null || !BaseDatosCapas.verificarContrasena(contrasena.trim(), usuario.getContrasenaHash())) {
            throw new SecurityException("Credenciales incorrectas. Verifique correo y contraseña.");
        }

        return usuario;
    }
}
