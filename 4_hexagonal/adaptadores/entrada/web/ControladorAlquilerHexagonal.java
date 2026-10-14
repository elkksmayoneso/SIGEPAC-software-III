package sigepac.hexagonal.adaptadores.entrada.web;

import sigepac.hexagonal.nucleo.puertos.entrada.ICasoUsoRegistrarDeuda;
import sigepac.hexagonal.nucleo.dominio.modelos.Alquiler;
import java.util.List;

public class ControladorAlquilerHexagonal {
    private final ICasoUsoRegistrarDeuda casoUsoAlquiler;

    public ControladorAlquilerHexagonal(ICasoUsoRegistrarDeuda casoUsoAlquiler) {
        this.casoUsoAlquiler = casoUsoAlquiler;
    }

    public Alquiler crear(int idNegocio, String fechaInicio, String fechaLimite, double valor) {
        return casoUsoAlquiler.registrarDeudaAlquiler(idNegocio, fechaInicio, fechaLimite, valor);
    }

    public List<Alquiler> listar() {
        return casoUsoAlquiler.listarAlquileres();
    }

    public List<Alquiler> listarPorNegocio(int idNegocio) {
        return casoUsoAlquiler.listarPorNegocio(idNegocio);
    }
}
