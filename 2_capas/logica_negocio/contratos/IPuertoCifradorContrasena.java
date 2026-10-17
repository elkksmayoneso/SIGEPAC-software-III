package sigepac.capas.logica_negocio.contratos;
public interface IPuertoCifradorContrasena { String cifrar(String textoPlano); boolean verificar(String textoPlano,String hash); }
