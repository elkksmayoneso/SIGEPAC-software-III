package sigepac.hexagonal.nucleo.aplicacion.servicios;

import sigepac.hexagonal.nucleo.puertos.entrada.ICasoUsoIniciarSesion;
import sigepac.hexagonal.nucleo.puertos.salida.IPuertoPersistenciaUsuario;
import sigepac.hexagonal.nucleo.puertos.salida.IPuertoCifradorContrasena;
import sigepac.hexagonal.nucleo.dominio.modelos.Usuario;
import sigepac.hexagonal.nucleo.dominio.excepciones.CredencialesInvalidasExcepcion;

public class ServicioAutenticacionHexagonal implements ICasoUsoIniciarSesion {
    private final IPuertoPersistenciaUsuario puertoUsuario;
    private final IPuertoCifradorContrasena puertoCifrador;

    public ServicioAutenticacionHexagonal(IPuertoPersistenciaUsuario puertoUsuario, IPuertoCifradorContrasena puertoCifrador) {
        this.puertoUsuario = puertoUsuario;
        this.puertoCifrador = puertoCifrador;
    }

    @Override
    public Usuario ejecutar(String correo, String contrasena) {
        if (correo == null || correo.trim().isEmpty() || contrasena == null || contrasena.trim().isEmpty()) {
            throw new CredencialesInvalidasExcepcion("Debe ingresar correo y contraseña obligatoriamente.");
        }

        Usuario usuario = puertoUsuario.buscarPorCorreo(correo.trim().toLowerCase())
                .orElseThrow(() -> new CredencialesInvalidasExcepcion("Credenciales inválidas. Por favor verifique el correo y la contraseña ingresada."));

        if (!puertoCifrador.verificar(contrasena, usuario.getContrasenaHash())) {
            throw new CredencialesInvalidasExcepcion("Credenciales inválidas. Por favor verifique el correo y la contraseña ingresada.");
        }

        return usuario;
    }
}
