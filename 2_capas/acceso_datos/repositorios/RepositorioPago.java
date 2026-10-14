package sigepac.capas.acceso_datos.repositorios;

import sigepac.capas.acceso_datos.contratos.IRepositorioPago;
import sigepac.capas.dominio.entidades.EntidadPago;
import sigepac.capas.infraestructura.BaseDatosCapas;
import java.util.*;

public class RepositorioPago implements IRepositorioPago {
    private final BaseDatosCapas bd = BaseDatosCapas.obtenerInstancia();

    @Override
    public int guardar(EntidadPago pago) {
        synchronized (bd) {
            int nuevoId = bd.siguienteIdPago();
            pago.setId(nuevoId);
            bd.tablaPagos.put(nuevoId, pago);
            return nuevoId;
        }
    }

    @Override
    public List<EntidadPago> obtenerTodos() {
        synchronized (bd) {
            return new ArrayList<>(bd.tablaPagos.values());
        }
    }
}
