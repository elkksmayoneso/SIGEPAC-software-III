package sigepac.poo.dominio.excepciones;

public class SobrepagoDeudaExcepcion extends RuntimeException {
    public SobrepagoDeudaExcepcion(String mensaje) {
        super(mensaje);
    }
}
