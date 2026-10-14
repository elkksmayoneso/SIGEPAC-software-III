package sigepac.poo.infraestructura;

import sigepac.poo.dominio.entidades.*;
import sigepac.poo.dominio.objetos_valor.CorreoElectronico;
import java.security.MessageDigest;
import java.nio.charset.StandardCharsets;
import java.util.*;

public final class BaseDatosPoo {
    private static BaseDatosPoo instancia;

    public final Map<Integer, CuentaUsuario> tablaCuentas = new LinkedHashMap<>();
    public final Map<Integer, NegocioComercial> tablaNegocios = new LinkedHashMap<>();
    public final Map<Integer, DeudaAlquiler> tablaDeudas = new LinkedHashMap<>();
    public final Map<Integer, TransaccionPago> tablaPagos = new LinkedHashMap<>();

    private int secuenciaCuenta = 1;
    private int secuenciaNegocio = 1;
    private int secuenciaDeuda = 1;
    private int secuenciaPago = 1;

    private BaseDatosPoo() {
        CuentaUsuario admin = new CuentaUsuario(
            secuenciaCuenta++,
            "Administradora",
            "Plaza Cívica",
            new CorreoElectronico("admin@sigepac.com"),
            cifrarContrasena("Admin123*"),
            "ADMINISTRADORA",
            "3101234567"
        );
        tablaCuentas.put(admin.getIdCuenta(), admin);
    }

    public static synchronized BaseDatosPoo obtenerInstancia() {
        if (instancia == null) {
            instancia = new BaseDatosPoo();
        }
        return instancia;
    }

    public static String cifrarContrasena(String contrasenaPlana) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            String conSal = "POO_SALT_2026_" + contrasenaPlana;
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

    public synchronized int siguienteIdCuenta() { return secuenciaCuenta++; }
    public synchronized int siguienteIdNegocio() { return secuenciaNegocio++; }
    public synchronized int siguienteIdDeuda() { return secuenciaDeuda++; }
    public synchronized int siguienteIdPago() { return secuenciaPago++; }
}
