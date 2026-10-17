package sigepac.mvc.configuracion;
public interface IPuertoCifradorContrasena { String cifrar(String texto); boolean verificar(String texto,String hash); }
