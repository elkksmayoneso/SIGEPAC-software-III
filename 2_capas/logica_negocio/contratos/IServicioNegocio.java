package sigepac.capas.logica_negocio.contratos;

import sigepac.capas.dominio.entidades.*;
import java.util.List;

public interface IServicioNegocio {
    List<EntidadNegocio> listarNegocios();
    int registrarNegocio(int idUsuario, String nombre, String localCodigo);
    boolean eliminarNegocio(int id);
}
