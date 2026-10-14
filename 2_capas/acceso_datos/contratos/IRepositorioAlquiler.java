package sigepac.capas.acceso_datos.contratos;

import sigepac.capas.dominio.entidades.EntidadAlquiler;
import java.util.List;

public interface IRepositorioAlquiler {
    List<EntidadAlquiler> obtenerTodos();
    List<EntidadAlquiler> obtenerActivosPorNegocio(int idNegocio);
    int guardar(EntidadAlquiler alquiler);
    boolean actualizarSaldoYEstado(int idAlquiler, double nuevoSaldo, String nuevoEstado);
}
