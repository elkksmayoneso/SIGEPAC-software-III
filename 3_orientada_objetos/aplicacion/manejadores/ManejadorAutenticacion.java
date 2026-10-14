package sigepac.poo.aplicacion.manejadores;

import sigepac.poo.dominio.repositorios.IRepositorioCuentaUsuario;
import sigepac.poo.dominio.entidades.CuentaUsuario;
import sigepac.poo.dominio.objetos_valor.CorreoElectronico;
import sigepac.poo.dominio.excepciones.CredencialesInvalidasExcepcion;

public class ManejadorAutenticacion {
    private final IRepositorioCuentaUsuario repoCuenta;

    public ManejadorAutenticacion(IRepositorioCuentaUsuario repoCuenta) {
        this.repoCuenta = repoCuenta;
    }

    public CuentaUsuario ejecutar(String correo, String contrasena) {
        if (correo == null || correo.trim().isEmpty() || contrasena == null || contrasena.trim().isEmpty()) {
            throw new IllegalArgumentException("Todos los campos son obligatorios.");
        }

        CorreoElectronico voCorreo = new CorreoElectronico(correo);
        CuentaUsuario cuenta = repoCuenta.buscarPorCorreo(voCorreo);

        // HU-01 & RNF3: Delegación al método rico de la entidad de dominio
        if (cuenta == null) {
            throw new CredencialesInvalidasExcepcion("Credenciales incorrectas. Verifique correo y contraseña ingresados.");
        }

        cuenta.verificarContrasena(contrasena);
        return cuenta;
    }
}
