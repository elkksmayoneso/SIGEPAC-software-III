package sigepac.poo.infraestructura.persistencia;

import sigepac.poo.dominio.repositorios.IRepositorioDeudaAlquiler;
import sigepac.poo.dominio.entidades.DeudaAlquiler;
import sigepac.poo.dominio.entidades.TransaccionPago;
import sigepac.poo.infraestructura.BaseDatosPoo;
import java.util.*;

public class RepositorioDeudaAlquilerMemoria implements IRepositorioDeudaAlquiler {
    private final BaseDatosPoo bd = BaseDatosPoo.obtenerInstancia();

    @Override
    public List<DeudaAlquiler> obtenerTodos() {
        synchronized (bd) {
            return new ArrayList<>(bd.tablaDeudas.values());
        }
    }

    @Override
    public List<DeudaAlquiler> obtenerActivosPorNegocio(int idNegocio) {
        synchronized (bd) {
            List<DeudaAlquiler> lista = new ArrayList<>();
            for (DeudaAlquiler d : bd.tablaDeudas.values()) {
                if (d.getIdNegocio() == idNegocio && d.getSaldoActual().getCantidad() > 0) {
                    lista.add(d);
                }
            }
            return lista;
        }
    }

    @Override
    public int guardarDeuda(DeudaAlquiler deuda) {
        synchronized (bd) {
            int nuevoId = bd.siguienteIdDeuda();
            deuda.setIdDeuda(nuevoId);
            bd.tablaDeudas.put(nuevoId, deuda);
            return nuevoId;
        }
    }

    @Override
    public boolean actualizarDeuda(DeudaAlquiler deuda) {
        synchronized (bd) {
            if (deuda.getIdDeuda() == null || !bd.tablaDeudas.containsKey(deuda.getIdDeuda())) {
                return false;
            }
            bd.tablaDeudas.put(deuda.getIdDeuda(), deuda);
            return true;
        }
    }

    @Override
    public int registrarPago(TransaccionPago pago) {
        synchronized (bd) {
            int nuevoId = bd.siguienteIdPago();
            pago.setIdPago(nuevoId);
            bd.tablaPagos.put(nuevoId, pago);
            return nuevoId;
        }
    }

    @Override
    public List<TransaccionPago> obtenerTodosLosPagos() {
        synchronized (bd) {
            return new ArrayList<>(bd.tablaPagos.values());
        }
    }
}
