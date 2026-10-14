package sigepac.hexagonal.nucleo.puertos.salida;

public interface IPuertoCifradorContrasena {
    String cifrar(String textoPlano);
    boolean verificar(String textoPlano, String hashEsperado);
}
