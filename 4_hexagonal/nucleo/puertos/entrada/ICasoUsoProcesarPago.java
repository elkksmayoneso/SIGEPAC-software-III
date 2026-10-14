package sigepac.hexagonal.nucleo.puertos.entrada;

import sigepac.hexagonal.nucleo.dominio.modelos.Pago;
import java.util.List;

public interface ICasoUsoProcesarPago {
    Pago procesarPago(int negocioId, double monto, String fechaAbono, String formaAbono);
    List<Pago> listarPagos();
}
