package sigepac.hexagonal.adaptadores.entrada.web;

import sigepac.hexagonal.nucleo.puertos.entrada.ICasoUsoIniciarSesion;
import sigepac.hexagonal.nucleo.dominio.modelos.Usuario;

public class ControladorAutenticacionHexagonal {
    private final ICasoUsoIniciarSesion casoUsoIniciarSesion;

    public ControladorAutenticacionHexagonal(ICasoUsoIniciarSesion casoUsoIniciarSesion) {
        this.casoUsoIniciarSesion = casoUsoIniciarSesion;
    }

    public Usuario iniciarSesion(String correo, String contrasena) {
        return casoUsoIniciarSesion.ejecutar(correo, contrasena);
    }
}
