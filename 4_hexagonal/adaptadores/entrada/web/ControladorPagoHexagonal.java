package sigepac.hexagonal.adaptadores.entrada.web;

import sigepac.hexagonal.nucleo.puertos.entrada.ICasoUsoProcesarPago;
import sigepac.hexagonal.nucleo.dominio.modelos.Pago;
import java.util.List;

public class ControladorPagoHexagonal {
    private final ICasoUsoProcesarPago casoUsoPago;

    public ControladorPagoHexagonal(ICasoUsoProcesarPago casoUsoPago) {
        this.casoUsoPago = casoUsoPago;
    }

    public Pago procesar(int idNegocio, double monto, String fechaPago, String metodoPago) {
        return casoUsoPago.procesarPago(idNegocio, monto, fechaPago, metodoPago);
    }

    public List<Pago> listar() {
        return casoUsoPago.listarPagos();
    }
}
