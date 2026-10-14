package sigepac.poo.presentacion.controladores;

import sigepac.poo.aplicacion.manejadores.ManejadorDeuda;
import sigepac.poo.dominio.entidades.DeudaAlquiler;
import java.util.List;

public class ControladorDeudaPoo {
    private final ManejadorDeuda manejadorDeuda;

    public ControladorDeudaPoo(ManejadorDeuda manejadorDeuda) {
        this.manejadorDeuda = manejadorDeuda;
    }

    public List<DeudaAlquiler> listar() {
        return manejadorDeuda.listar();
    }

    public int crear(int idNegocio, String fechaInicio, String fechaLimite, double valor) {
        return manejadorDeuda.registrarDeuda(idNegocio, fechaInicio, fechaLimite, valor);
    }
}
