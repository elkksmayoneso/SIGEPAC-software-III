package sigepac.capas.logica_negocio.servicios;

import sigepac.capas.logica_negocio.contratos.IServicioUsuario;
import sigepac.capas.acceso_datos.contratos.IRepositorioUsuario;
import sigepac.capas.dominio.entidades.EntidadUsuario;
import sigepac.capas.infraestructura.BaseDatosCapas;
import java.util.List;

public class ServicioUsuario implements IServicioUsuario {
    private final IRepositorioUsuario repoUsuario;

    public ServicioUsuario(IRepositorioUsuario repoUsuario) {
        this.repoUsuario = repoUsuario;
    }

    @Override
    public List<EntidadUsuario> listarUsuarios() {
        return repoUsuario.obtenerTodos();
    }

    @Override
    public int registrarUsuario(String nombre, String apellido, String correo, String contrasena, String rol, String telefono) {
        // HU-02: Exigir campos obligatorios
        if (nombre == null || nombre.trim().isEmpty() ||
            apellido == null || apellido.trim().isEmpty() ||
            correo == null || correo.trim().isEmpty() ||
            contrasena == null || contrasena.trim().isEmpty() ||
            telefono == null || telefono.trim().isEmpty()) {
            throw new IllegalArgumentException("Todos los campos son obligatorios. Complete la información faltante antes de guardar.");
        }

        if (repoUsuario.buscarPorCorreo(correo.trim()) != null) {
            throw new IllegalStateException("El correo ingresado ya existe.");
        }

        EntidadUsuario nuevo = new EntidadUsuario(
            null,
            nombre.trim(),
            apellido.trim(),
            correo.trim(),
            BaseDatosCapas.cifrarContrasena(contrasena.trim()),
            rol != null ? rol : "PROPIETARIO",
            telefono.trim()
        );

        return repoUsuario.guardar(nuevo);
    }

    @Override
    public boolean eliminarUsuario(int id) {
        return repoUsuario.eliminar(id);
    }
}
