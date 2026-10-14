package sigepac.poo.dominio.repositorios;

import sigepac.poo.dominio.entidades.NegocioComercial;
import java.util.List;

public interface IRepositorioNegocioComercial {
    NegocioComercial buscarPorId(int id);
    boolean existe(int id);
    List<NegocioComercial> obtenerTodos();
    int guardar(NegocioComercial negocio);
    boolean eliminar(int id);
}
