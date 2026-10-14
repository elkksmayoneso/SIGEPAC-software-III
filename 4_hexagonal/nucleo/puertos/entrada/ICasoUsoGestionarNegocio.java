package sigepac.hexagonal.nucleo.puertos.entrada;

import sigepac.hexagonal.nucleo.dominio.modelos.Negocio;
import java.util.List;

public interface ICasoUsoGestionarNegocio {
    Negocio registrarNegocio(int propietarioId, String nombreComercial, String numeroLocal);
    boolean actualizarNegocio(int id, String nombreComercial, String numeroLocal);
    boolean eliminarNegocio(int id);
    List<Negocio> listarNegocios();
    Negocio buscarPorId(int id);
}
