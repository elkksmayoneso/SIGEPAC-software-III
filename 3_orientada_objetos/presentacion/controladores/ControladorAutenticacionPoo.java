package sigepac.poo.presentacion.controladores;

import sigepac.poo.aplicacion.manejadores.ManejadorAutenticacion;
import sigepac.poo.dominio.entidades.CuentaUsuario;

public class ControladorAutenticacionPoo {
    private final ManejadorAutenticacion manejadorAuth;

    public ControladorAutenticacionPoo(ManejadorAutenticacion manejadorAuth) {
        this.manejadorAuth = manejadorAuth;
    }

    public CuentaUsuario iniciarSesion(String correo, String contrasena) {
        return manejadorAuth.ejecutar(correo, contrasena);
    }
}
