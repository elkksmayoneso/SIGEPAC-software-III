package sigepac.poo.presentacion.controladores;

import sigepac.poo.aplicacion.manejadores.ManejadorPago;
import sigepac.poo.dominio.entidades.TransaccionPago;
import java.util.List;

public class ControladorPagoPoo {
    private final ManejadorPago manejadorPago;

    public ControladorPagoPoo(ManejadorPago manejadorPago) {
        this.manejadorPago = manejadorPago;
    }

    public List<TransaccionPago> listar() {
        return manejadorPago.listarPagos();
    }

    public boolean procesar(int idNegocio, double monto, String fechaPago, String metodo) {
        return manejadorPago.procesarPago(idNegocio, monto, fechaPago, metodo);
    }
}
