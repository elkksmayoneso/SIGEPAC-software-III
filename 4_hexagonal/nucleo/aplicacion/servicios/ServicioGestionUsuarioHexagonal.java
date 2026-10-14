package sigepac.hexagonal.nucleo.aplicacion.servicios;

import sigepac.hexagonal.nucleo.puertos.entrada.ICasoUsoGestionarUsuario;
import sigepac.hexagonal.nucleo.puertos.salida.IPuertoPersistenciaUsuario;
import sigepac.hexagonal.nucleo.puertos.salida.IPuertoCifradorContrasena;
import sigepac.hexagonal.nucleo.dominio.modelos.Usuario;
import sigepac.hexagonal.nucleo.dominio.excepciones.ValidacionDatosExcepcion;

import java.util.List;

public class ServicioGestionUsuarioHexagonal implements ICasoUsoGestionarUsuario {
    private final IPuertoPersistenciaUsuario puertoUsuario;
    private final IPuertoCifradorContrasena puertoCifrador;

    public ServicioGestionUsuarioHexagonal(IPuertoPersistenciaUsuario puertoUsuario, IPuertoCifradorContrasena puertoCifrador) {
        this.puertoUsuario = puertoUsuario;
        this.puertoCifrador = puertoCifrador;
    }

    @Override
    public Usuario registrarUsuario(String nombre, String apellido, String correo, String contrasena, String rol, String telefono) {
        if (contrasena == null || contrasena.trim().length() < 6) {
            throw new ValidacionDatosExcepcion("La contraseña debe tener al menos 6 caracteres.");
        }
        if (puertoUsuario.buscarPorCorreo(correo != null ? correo.trim().toLowerCase() : "").isPresent()) {
            throw new ValidacionDatosExcepcion("El correo electrónico ya se encuentra registrado en el sistema.");
        }

        String hash = puertoCifrador.cifrar(contrasena);
        Usuario nuevo = new Usuario(null, nombre, apellido, correo, hash, rol, telefono);
        return puertoUsuario.guardar(nuevo);
    }

    @Override
    public boolean actualizarUsuario(int id, String nombre, String apellido, String correo, String rol, String telefono) {
        Usuario actual = puertoUsuario.buscarPorId(id)
                .orElseThrow(() -> new ValidacionDatosExcepcion("El usuario a actualizar no existe."));

        Usuario actualizado = new Usuario(id, nombre, apellido, correo, actual.getContrasenaHash(), rol, telefono);
        return puertoUsuario.actualizar(actualizado);
    }

    @Override
    public boolean eliminarUsuario(int id) {
        return puertoUsuario.eliminar(id);
    }

    @Override
    public List<Usuario> listarUsuarios() {
        return puertoUsuario.listarTodos();
    }

    @Override
    public Usuario buscarPorId(int id) {
        return puertoUsuario.buscarPorId(id)
                .orElseThrow(() -> new ValidacionDatosExcepcion("Usuario no encontrado con ID: " + id));
    }
}
