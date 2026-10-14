package sigepac.capas.logica_negocio.contratos;

import sigepac.capas.dominio.entidades.EntidadPago;
import java.util.List;

public interface IServicioPago {
    List<EntidadPago> listarPagos();
    boolean registrarPago(int idNegocio, double monto, String fechaPago, String formaPago) throws Exception;
}
