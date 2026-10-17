package sigepac.mvc.modelos.contratos;
import java.util.*;
public interface IModeloNegocio { Map<String,Object> buscarPorId(int id); List<Map<String,Object>> obtenerTodos(); boolean crear(int idUsuario,String nombre,String local); boolean actualizar(int id,String nombre,String local,String estado); boolean eliminar(int id); }
