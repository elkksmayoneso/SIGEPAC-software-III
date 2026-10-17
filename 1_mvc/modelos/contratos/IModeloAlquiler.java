package sigepac.mvc.modelos.contratos;
import java.util.*;
public interface IModeloAlquiler { boolean crear(int idNegocio,String fechaInicio,String fechaLimite,double valor); boolean actualizarDeuda(int id,double valor,String fechaLimite); List<Map<String,Object>> obtenerTodos(); List<Map<String,Object>> obtenerActivosPorNegocio(int idNegocio); }
