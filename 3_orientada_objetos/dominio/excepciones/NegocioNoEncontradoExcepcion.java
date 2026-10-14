package sigepac.poo.dominio.excepciones;

public class NegocioNoEncontradoExcepcion extends RuntimeException {
    public NegocioNoEncontradoExcepcion(String mensaje) {
        super(mensaje);
    }
}
