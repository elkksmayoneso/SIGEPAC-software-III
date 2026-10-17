package sigepac.poo.infraestructura;
import sigepac.poo.dominio.repositorios.IPuertoCifradorContrasena;import java.nio.charset.StandardCharsets;import java.security.MessageDigest;
public final class AdaptadorCifradorPoo implements IPuertoCifradorContrasena {
 public String cifrar(String texto){try{byte[] b=MessageDigest.getInstance("SHA-256").digest(("POO_SALT_2026_"+texto).getBytes(StandardCharsets.UTF_8));StringBuilder s=new StringBuilder();for(byte x:b)s.append(String.format("%02x",x));return s.toString();}catch(Exception e){throw new IllegalStateException("No se pudo cifrar la contraseña",e);}}
 public boolean verificar(String texto,String hash){return texto!=null&&hash!=null&&MessageDigest.isEqual(cifrar(texto).getBytes(StandardCharsets.US_ASCII),hash.getBytes(StandardCharsets.US_ASCII));}
}
