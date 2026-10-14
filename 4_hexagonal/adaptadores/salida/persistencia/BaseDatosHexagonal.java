package sigepac.hexagonal.adaptadores.salida.persistencia;

import sigepac.hexagonal.nucleo.dominio.modelos.*;
import sigepac.hexagonal.adaptadores.salida.seguridad.AdaptadorCifradorSha256;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class BaseDatosHexagonal {
    private static BaseDatosHexagonal instancia;

    public final Map<Integer, Usuario> tablaUsuarios = new ConcurrentHashMap<>();
    public final Map<Integer, Negocio> tablaNegocios = new ConcurrentHashMap<>();
    public final Map<Integer, Alquiler> tablaAlquileres = new ConcurrentHashMap<>();
    public final Map<Integer, Pago> tablaPagos = new ConcurrentHashMap<>();

    public final AtomicInteger secuenciaUsuario = new AtomicInteger(1);
    public final AtomicInteger secuenciaNegocio = new AtomicInteger(1);
    public final AtomicInteger secuenciaAlquiler = new AtomicInteger(1);
    public final AtomicInteger secuenciaPago = new AtomicInteger(1);

    private BaseDatosHexagonal() {
        sembrarDatosIniciales();
    }

    public static synchronized BaseDatosHexagonal getInstancia() {
        if (instancia == null) {
            instancia = new BaseDatosHexagonal();
        }
        return instancia;
    }

    private void sembrarDatosIniciales() {
        AdaptadorCifradorSha256 cifrador = new AdaptadorCifradorSha256();
        int idAdmin = secuenciaUsuario.getAndIncrement();
        Usuario admin = new Usuario(
                idAdmin,
                "Admin",
                "Hexagonal",
                "admin@sigepac.com",
                cifrador.cifrar("Admin123*"),
                "ADMINISTRADORA",
                "3200001122"
        );
        tablaUsuarios.put(idAdmin, admin);

        int idProp = secuenciaUsuario.getAndIncrement();
        Usuario propietario = new Usuario(
                idProp,
                "Carlos",
                "Rojas",
                "carlos.rojas@gmail.com",
                cifrador.cifrar("Propietario123*"),
                "PROPIETARIO",
                "3115556677"
        );
        tablaUsuarios.put(idProp, propietario);

        int idNeg = secuenciaNegocio.getAndIncrement();
        Negocio negocio = new Negocio(
                idNeg,
                idProp,
                "Cafetería El Buen Café",
                "LOC-401",
                "ACTIVO"
        );
        tablaNegocios.put(idNeg, negocio);

        int idAlq = secuenciaAlquiler.getAndIncrement();
        Alquiler alquiler = new Alquiler(
                idAlq,
                idNeg,
                "2026-10-01",
                "2026-10-31",
                450000.0,
                450000.0,
                "PENDIENTE"
        );
        tablaAlquileres.put(idAlq, alquiler);
    }
}
