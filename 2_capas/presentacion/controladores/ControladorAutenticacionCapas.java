package sigepac.capas.presentacion.controladores;

import sigepac.capas.logica_negocio.contratos.IServicioAutenticacion;
import sigepac.capas.dominio.entidades.EntidadUsuario;

public class ControladorAutenticacionCapas {
    private final IServicioAutenticacion servicioAuth;

    public ControladorAutenticacionCapas(IServicioAutenticacion servicioAuth) {
        this.servicioAuth = servicioAuth;
    }

    public EntidadUsuario iniciarSesion(String correo, String contrasena) {
        return servicioAuth.autenticar(correo, contrasena);
    }
}
