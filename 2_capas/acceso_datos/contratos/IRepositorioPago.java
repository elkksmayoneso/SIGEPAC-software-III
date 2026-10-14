package sigepac.capas.acceso_datos.contratos;

import sigepac.capas.dominio.entidades.EntidadPago;
import java.util.List;

public interface IRepositorioPago {
    int guardar(EntidadPago pago);
    List<EntidadPago> obtenerTodos();
}
