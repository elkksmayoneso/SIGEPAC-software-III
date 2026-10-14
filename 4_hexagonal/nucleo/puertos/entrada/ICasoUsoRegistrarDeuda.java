package sigepac.hexagonal.nucleo.puertos.entrada;

import sigepac.hexagonal.nucleo.dominio.modelos.Alquiler;
import java.util.List;

public interface ICasoUsoRegistrarDeuda {
    Alquiler registrarDeudaAlquiler(int negocioId, String fechaInicio, String fechaCorte, double canonMensual);
    List<Alquiler> listarAlquileres();
    List<Alquiler> listarPorNegocio(int negocioId);
    Alquiler buscarPorId(int id);
}
