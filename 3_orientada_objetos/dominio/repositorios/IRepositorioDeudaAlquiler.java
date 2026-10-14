package sigepac.poo.dominio.repositorios;

import sigepac.poo.dominio.entidades.DeudaAlquiler;
import sigepac.poo.dominio.entidades.TransaccionPago;
import java.util.List;

public interface IRepositorioDeudaAlquiler {
    List<DeudaAlquiler> obtenerTodos();
    List<DeudaAlquiler> obtenerActivosPorNegocio(int idNegocio);
    int guardarDeuda(DeudaAlquiler deuda);
    boolean actualizarDeuda(DeudaAlquiler deuda);
    int registrarPago(TransaccionPago pago);
    List<TransaccionPago> obtenerTodosLosPagos();
}
