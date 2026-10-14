package sigepac.capas.presentacion.controladores;

import sigepac.capas.logica_negocio.contratos.IServicioPago;
import sigepac.capas.dominio.entidades.EntidadPago;
import java.util.List;

public class ControladorPagoCapas {
    private final IServicioPago servicioPago;

    public ControladorPagoCapas(IServicioPago servicioPago) {
        this.servicioPago = servicioPago;
    }

    public List<EntidadPago> listar() {
        return servicioPago.listarPagos();
    }

    public boolean procesar(int idNegocio, double monto, String fechaPago, String formaPago) throws Exception {
        return servicioPago.registrarPago(idNegocio, monto, fechaPago, formaPago);
    }
}
