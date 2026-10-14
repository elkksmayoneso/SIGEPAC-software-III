package sigepac.capas.logica_negocio.contratos;

import sigepac.capas.dominio.entidades.EntidadAlquiler;
import java.util.List;

public interface IServicioAlquiler {
    List<EntidadAlquiler> listarAlquileres();
    int registrarAlquiler(int idNegocio, String fechaInicio, String fechaVencimiento, double valorCuota);
}
