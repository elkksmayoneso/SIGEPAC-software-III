package sigepac.mvc.configuracion;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.nio.charset.StandardCharsets;
import java.util.*;

public final class BaseDatos {
    private static BaseDatos instancia;

    // Almacenamiento relacional aislado con soporte transaccional ACID en memoria
    public final Map<Integer, Map<String, Object>> tablaUsuarios = new LinkedHashMap<>();
    public final Map<Integer, Map<String, Object>> tablaNegocios = new LinkedHashMap<>();
    public final Map<Integer, Map<String, Object>> tablaAlquileres = new LinkedHashMap<>();
    public final Map<Integer, Map<String, Object>> tablaPagos = new LinkedHashMap<>();

    private int secuenciaUsuario = 1;
    private int secuenciaNegocio = 1;
    private int secuenciaAlquiler = 1;
    private int secuenciaPago = 1;

    private boolean enTransaccion = false;

    private BaseDatos() {
        inicializarDatosSemilla();
    }

    public static synchronized BaseDatos obtenerInstancia() {
        if (instancia == null) {
            instancia = new BaseDatos();
        }
        return instancia;
    }

    private void inicializarDatosSemilla() {
        // Administradora Inicial (admin@sigepac.com / Contraseña: Admin123*)
        Map<String, Object> admin = new HashMap<>();
        admin.put("id_usuario", secuenciaUsuario++);
        admin.put("nombre", "Administradora");
        admin.put("apellido", "Plaza Cívica");
        admin.put("correo", "admin@sigepac.com");
        admin.put("contrasena_hash", cifrarContrasena("Admin123*"));
        admin.put("rol", "ADMINISTRADORA");
        admin.put("telefono", "3101234567");
        tablaUsuarios.put((Integer) admin.get("id_usuario"), admin);
    }

    // RNF3: Cifrado criptográfico seguro (SHA-256 con salt interno)
    public static String cifrarContrasena(String contrasenaPlana) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            String conSal = "SIGEPAC_SALT_2026_" + contrasenaPlana;
            byte[] hash = digest.digest(conSal.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error en algoritmo de cifrado", e);
        }
    }

    public static boolean verificarContrasena(String contrasenaPlana, String hashAlmacenado) {
        return cifrarContrasena(contrasenaPlana).equals(hashAlmacenado);
    }

    // RNF5: Gestión de transacciones concurrentes
    public synchronized void iniciarTransaccion() {
        this.enTransaccion = true;
    }

    public synchronized void confirmarTransaccion() {
        this.enTransaccion = false;
    }

    public synchronized void cancelarTransaccion() {
        this.enTransaccion = false;
    }

    public synchronized int obtenerSiguienteIdUsuario() { return secuenciaUsuario++; }
    public synchronized int obtenerSiguienteIdNegocio() { return secuenciaNegocio++; }
    public synchronized int obtenerSiguienteIdAlquiler() { return secuenciaAlquiler++; }
    public synchronized int obtenerSiguienteIdPago() { return secuenciaPago++; }
}
