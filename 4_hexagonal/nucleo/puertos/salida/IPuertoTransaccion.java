package sigepac.hexagonal.nucleo.puertos.salida;

public interface IPuertoTransaccion {
    <T> T ejecutarEnTransaccion(java.util.concurrent.Callable<T> operacion);
}
