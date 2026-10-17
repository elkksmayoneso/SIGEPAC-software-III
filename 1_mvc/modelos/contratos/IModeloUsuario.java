package sigepac.mvc.modelos.contratos;
import java.util.*;
public interface IModeloUsuario { Map<String,Object> buscarPorCorreo(String correo); Map<String,Object> buscarPorId(int id); List<Map<String,Object>> obtenerTodos(); boolean crear(String nombre,String apellido,String correo,String contrasena,String rol,String telefono); boolean actualizar(int id,String nombre,String apellido,String telefono,String rol); boolean eliminar(int id); }
