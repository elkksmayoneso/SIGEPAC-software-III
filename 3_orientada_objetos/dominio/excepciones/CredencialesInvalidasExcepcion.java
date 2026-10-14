package sigepac.poo.dominio.excepciones;

public class CredencialesInvalidasExcepcion extends RuntimeException {
    public CredencialesInvalidasExcepcion(String mensaje) {
        super(mensaje);
    }
}
