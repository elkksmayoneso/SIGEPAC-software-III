package sigepac.hexagonal.adaptadores.salida.seguridad;

import sigepac.hexagonal.nucleo.puertos.salida.IPuertoCifradorContrasena;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class AdaptadorCifradorSha256 implements IPuertoCifradorContrasena {
    private static final String SALT_ESTATICA = "SIGEPAC_HEXAGONAL_SECURE_SALT_2026";

    @Override
    public String cifrar(String textoPlano) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(SALT_ESTATICA.getBytes(StandardCharsets.UTF_8));
            byte[] digest = md.digest(textoPlano.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error en algoritmo de cifrado: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean verificar(String textoPlano, String hashEsperado) {
        if (textoPlano == null || hashEsperado == null) return false;
        String hashCalculado = cifrar(textoPlano);
        return MessageDigest.isEqual(hashCalculado.getBytes(StandardCharsets.UTF_8), hashEsperado.getBytes(StandardCharsets.UTF_8));
    }
}
