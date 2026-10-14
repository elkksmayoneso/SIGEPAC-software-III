package sigepac.hexagonal.adaptadores.salida.persistencia;

import sigepac.hexagonal.nucleo.puertos.salida.IPuertoTransaccion;
import java.util.concurrent.Callable;

public class AdaptadorGestorTransaccion implements IPuertoTransaccion {
    private final Object bloqueoConcurrencia = new Object();

    @Override
    public <T> T ejecutarEnTransaccion(Callable<T> operacion) {
        synchronized (bloqueoConcurrencia) {
            try {
                return operacion.call();
            } catch (RuntimeException e) {
                throw e;
            } catch (Exception e) {
                throw new RuntimeException("Error en transacción de persistencia: " + e.getMessage(), e);
            }
        }
    }
}
