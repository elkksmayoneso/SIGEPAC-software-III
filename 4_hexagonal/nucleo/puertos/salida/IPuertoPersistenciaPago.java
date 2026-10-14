package sigepac.hexagonal.nucleo.puertos.salida;

import sigepac.hexagonal.nucleo.dominio.modelos.Pago;
import java.util.List;
import java.util.Optional;

public interface IPuertoPersistenciaPago {
    Pago guardar(Pago pago);
    Optional<Pago> buscarPorId(int id);
    List<Pago> listarTodos();
    List<Pago> listarPorAlquiler(int alquilerId);
}
