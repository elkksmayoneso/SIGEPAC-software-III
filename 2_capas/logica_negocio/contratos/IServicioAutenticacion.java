package sigepac.capas.logica_negocio.contratos;

import sigepac.capas.dominio.entidades.*;
import java.util.List;

public interface IServicioAutenticacion {
    EntidadUsuario autenticar(String correo, String contrasena);
}
