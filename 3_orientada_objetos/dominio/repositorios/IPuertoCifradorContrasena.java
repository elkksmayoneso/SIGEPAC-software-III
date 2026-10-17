package sigepac.poo.dominio.repositorios;
/** Contrato de seguridad, definido en el núcleo y realizado por infraestructura. */
public interface IPuertoCifradorContrasena { String cifrar(String textoPlano); boolean verificar(String textoPlano,String hash); }
