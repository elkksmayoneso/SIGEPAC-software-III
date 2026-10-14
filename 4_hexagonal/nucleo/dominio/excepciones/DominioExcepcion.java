package sigepac.hexagonal.nucleo.dominio.excepciones;

public class DominioExcepcion extends RuntimeException {
    public DominioExcepcion(String mensaje) {
        super(mensaje);
    }
}
