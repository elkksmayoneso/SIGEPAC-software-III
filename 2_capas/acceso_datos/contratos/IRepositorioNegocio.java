package sigepac.capas.acceso_datos.contratos;

import sigepac.capas.dominio.entidades.EntidadNegocio;
import java.util.List;

public interface IRepositorioNegocio {
    EntidadNegocio buscarPorId(int id);
    boolean existe(int id);
    List<EntidadNegocio> obtenerTodos();
    int guardar(EntidadNegocio negocio);
    boolean eliminar(int id);
}
