package sigepac.hexagonal.adaptadores.salida.persistencia;

import sigepac.hexagonal.nucleo.puertos.salida.IPuertoTransaccion;
import java.util.concurrent.Callable;

public class AdaptadorGestorTransaccion implements IPuertoTransaccion {
    @Override
    public <T> T ejecutarEnTransaccion(Callable<T> operacion) {
        return ConexionHexagonal.transaccion(operacion);
    }
}
