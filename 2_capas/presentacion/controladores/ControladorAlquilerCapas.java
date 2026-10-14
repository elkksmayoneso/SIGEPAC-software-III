package sigepac.capas.presentacion.controladores;

import sigepac.capas.logica_negocio.contratos.IServicioAlquiler;
import sigepac.capas.dominio.entidades.EntidadAlquiler;
import java.util.List;

public class ControladorAlquilerCapas {
    private final IServicioAlquiler servicioAlquiler;

    public ControladorAlquilerCapas(IServicioAlquiler servicioAlquiler) {
        this.servicioAlquiler = servicioAlquiler;
    }

    public List<EntidadAlquiler> listar() {
        return servicioAlquiler.listarAlquileres();
    }

    public int crear(int idNegocio, String fechaInicio, String fechaVencimiento, double valorCuota) {
        return servicioAlquiler.registrarAlquiler(idNegocio, fechaInicio, fechaVencimiento, valorCuota);
    }
}
