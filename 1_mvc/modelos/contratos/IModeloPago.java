package sigepac.mvc.modelos.contratos;
import java.util.*;
public interface IModeloPago { boolean registrarPago(int idNegocio,double monto,String fechaPago,String metodo); List<Map<String,Object>> obtenerTodos(); }
