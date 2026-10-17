package sigepac.poo.aplicacion.manejadores;

import sigepac.poo.dominio.repositorios.IRepositorioCuentaUsuario;
import sigepac.poo.dominio.entidades.CuentaUsuario;
import sigepac.poo.dominio.objetos_valor.CorreoElectronico;
import sigepac.poo.dominio.repositorios.IPuertoCifradorContrasena;
import java.util.List;

public class ManejadorUsuario {
    private final IRepositorioCuentaUsuario repoCuenta;
    private final IPuertoCifradorContrasena cifrador;

    public ManejadorUsuario(IRepositorioCuentaUsuario repoCuenta, IPuertoCifradorContrasena cifrador) {
        this.repoCuenta = repoCuenta;
        this.cifrador = cifrador;
    }

    public List<CuentaUsuario> listar() {
        return repoCuenta.obtenerTodos();
    }

    public int registrar(String nombre, String apellido, String correo, String contrasena, String rol, String telefono) {
        // HU-02: Exigir campos obligatorios
        if (nombre == null || nombre.trim().isEmpty() ||
            apellido == null || apellido.trim().isEmpty() ||
            correo == null || correo.trim().isEmpty() ||
            contrasena == null || contrasena.trim().isEmpty() ||
            telefono == null || telefono.trim().isEmpty()) {
            throw new IllegalArgumentException("Todos los campos son obligatorios. Complete la información faltante antes de guardar.");
        }

        CorreoElectronico voCorreo = new CorreoElectronico(correo);
        if (repoCuenta.buscarPorCorreo(voCorreo) != null) {
            throw new IllegalStateException("El correo ingresado ya existe en el sistema.");
        }

        CuentaUsuario cuenta = new CuentaUsuario(
            null,
            nombre.trim(),
            apellido.trim(),
            voCorreo,
            cifrador.cifrar(contrasena.trim()),
            rol != null ? rol : "PROPIETARIO",
            telefono.trim()
        );

        return repoCuenta.guardar(cuenta);
    }

    public boolean eliminar(int id) {
        return repoCuenta.eliminar(id);
    }
}
