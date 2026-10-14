package sigepac.hexagonal.nucleo.puertos.entrada;

import sigepac.hexagonal.nucleo.dominio.modelos.Usuario;

public interface ICasoUsoIniciarSesion {
    Usuario ejecutar(String correo, String contrasena);
}
