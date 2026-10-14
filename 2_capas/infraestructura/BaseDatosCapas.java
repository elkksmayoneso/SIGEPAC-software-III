package sigepac.capas.infraestructura;

import sigepac.capas.dominio.entidades.*;
import java.security.MessageDigest;
import java.nio.charset.StandardCharsets;
import java.util.*;

public final class BaseDatosCapas {
    private static BaseDatosCapas instancia;

    public final Map<Integer, EntidadUsuario> tablaUsuarios = new LinkedHashMap<>();
    public final Map<Integer, EntidadNegocio> tablaNegocios = new LinkedHashMap<>();
    public final Map<Integer, EntidadAlquiler> tablaAlquileres = new LinkedHashMap<>();
    public final Map<Integer, EntidadPago> tablaPagos = new LinkedHashMap<>();

    private int secuenciaUsuario = 1;
    private int secuenciaNegocio = 1;
    private int secuenciaAlquiler = 1;
    private int secuenciaPago = 1;

    private BaseDatosCapas() {
        EntidadUsuario admin = new EntidadUsuario(
            secuenciaUsuario++,
            "Admin",
            "Capas",
            "admin@sigepac.com",
            cifrarContrasena("Admin123*"),
            "ADMINISTRADORA",
            "3009998877"
        );
        tablaUsuarios.put(admin.getId(), admin);
    }

    public static synchronized BaseDatosCapas obtenerInstancia() {
        if (instancia == null) {
            instancia = new BaseDatosCapas();
        }
        return instancia;
    }

    public static String cifrarContrasena(String contrasenaPlana) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            String conSal = "CAPAS_SALT_2026_" + contrasenaPlana;
            byte[] hash = digest.digest(conSal.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            throw new RuntimeException("Error en cifrado", e);
        }
    }

    public static boolean verificarContrasena(String contrasenaPlana, String hashAlmacenado) {
        return cifrarContrasena(contrasenaPlana).equals(hashAlmacenado);
    }

    public synchronized int siguienteIdUsuario() { return secuenciaUsuario++; }
    public synchronized int siguienteIdNegocio() { return secuenciaNegocio++; }
    public synchronized int siguienteIdAlquiler() { return secuenciaAlquiler++; }
    public synchronized int siguienteIdPago() { return secuenciaPago++; }
}
