package sigepac.hexagonal.servidor;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import sigepac.hexagonal.adaptadores.salida.persistencia.*;
import sigepac.hexagonal.adaptadores.salida.seguridad.*;
import sigepac.hexagonal.nucleo.aplicacion.servicios.*;
import sigepac.hexagonal.adaptadores.entrada.web.*;
import sigepac.hexagonal.nucleo.dominio.modelos.*;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class ServidorHexagonal {
    public static final int PUERTO = 8004;

    // Adaptadores Secundarios (Driven / Salida)
    private static final AdaptadorCifradorSha256 cifrador = new AdaptadorCifradorSha256();
    private static final AdaptadorPersistenciaUsuario repoUsuario = new AdaptadorPersistenciaUsuario();
    private static final AdaptadorPersistenciaNegocio repoNegocio = new AdaptadorPersistenciaNegocio();
    private static final AdaptadorPersistenciaAlquiler repoAlquiler = new AdaptadorPersistenciaAlquiler();
    private static final AdaptadorPersistenciaPago repoPago = new AdaptadorPersistenciaPago();
    private static final AdaptadorGestorTransaccion gestorTx = new AdaptadorGestorTransaccion();

    // Núcleo de Aplicación (Inyección de Puertos)
    private static final ServicioAutenticacionHexagonal srvAuth = new ServicioAutenticacionHexagonal(repoUsuario, cifrador);
    private static final ServicioGestionUsuarioHexagonal srvUsuario = new ServicioGestionUsuarioHexagonal(repoUsuario, cifrador);
    private static final ServicioGestionNegocioHexagonal srvNegocio = new ServicioGestionNegocioHexagonal(repoNegocio, repoUsuario);
    private static final ServicioRegistroDeudaHexagonal srvDeuda = new ServicioRegistroDeudaHexagonal(repoAlquiler, repoNegocio);
    private static final ServicioProcesarPagoHexagonal srvPago = new ServicioProcesarPagoHexagonal(repoNegocio, repoAlquiler, repoPago, gestorTx);

    // Adaptadores Primarios (Driving / Entrada Web)
    public static final ControladorAutenticacionHexagonal ctrlAuth = new ControladorAutenticacionHexagonal(srvAuth);
    public static final ControladorUsuarioHexagonal ctrlUsuario = new ControladorUsuarioHexagonal(srvUsuario);
    public static final ControladorNegocioHexagonal ctrlNegocio = new ControladorNegocioHexagonal(srvNegocio);
    public static final ControladorAlquilerHexagonal ctrlAlquiler = new ControladorAlquilerHexagonal(srvDeuda);
    public static final ControladorPagoHexagonal ctrlPago = new ControladorPagoHexagonal(srvPago);

    private static HttpServer servidorHttp;

    public static void main(String[] args) throws IOException {
        iniciarServidor();
    }

    public static synchronized void iniciarServidor() throws IOException {
        if (servidorHttp != null) return;
        servidorHttp = HttpServer.create(new InetSocketAddress(PUERTO), 0);
        servidorHttp.createContext("/", new ManejadorHexagonal());
        servidorHttp.setExecutor(null);
        System.out.println("====================================================================");
        System.out.println(" Servidor SIGEPAC (Hexagonal en Java) activo en http://localhost:" + PUERTO);
        System.out.println("====================================================================");
        servidorHttp.start();
    }

    public static synchronized void detenerServidor() {
        if (servidorHttp != null) {
            servidorHttp.stop(0);
            servidorHttp = null;
        }
    }

    static class ManejadorHexagonal implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String metodo = exchange.getRequestMethod();
            String query = exchange.getRequestURI().getQuery();
            Map<String, String> params = parsearParametros(query);
            String accion = params.getOrDefault("accion", "negocios");
            String msg = params.getOrDefault("msg", "");
            String err = params.getOrDefault("err", "");

            if ("POST".equalsIgnoreCase(metodo)) {
                InputStreamReader isr = new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8);
                BufferedReader br = new BufferedReader(isr);
                StringBuilder cuerpo = new StringBuilder();
                String l;
                while ((l = br.readLine()) != null) cuerpo.append(l);
                Map<String, String> post = parsearParametros(cuerpo.toString());

                if ("login_enviar".equals(accion)) {
                    try {
                        ctrlAuth.iniciarSesion(post.get("correo"), post.get("contrasena"));
                        redirigir(exchange, "/?accion=negocios&msg=" + urlEncode("Bienvenido a SIGEPAC (Hexagonal)."));
                    } catch (Exception e) {
                        redirigir(exchange, "/?accion=login&err=" + urlEncode(e.getMessage()));
                    }
                    return;
                } else if ("crear_usuario".equals(accion)) {
                    try {
                        ctrlUsuario.crear(post.get("nombre"), post.get("apellido"), post.get("correo"), post.get("contrasena"), post.get("rol"), post.get("telefono"));
                        redirigir(exchange, "/?accion=usuarios&msg=" + urlEncode("Usuario creado con éxito en Hexagonal."));
                    } catch (Exception e) {
                        redirigir(exchange, "/?accion=usuarios&err=" + urlEncode(e.getMessage()));
                    }
                    return;
                } else if ("crear_negocio".equals(accion)) {
                    try {
                        int idU = Integer.parseInt(post.getOrDefault("id_usuario", "0"));
                        ctrlNegocio.crear(idU, post.get("nombre"), post.get("local_numero"));
                        redirigir(exchange, "/?accion=negocios&msg=" + urlEncode("Negocio registrado en Hexagonal."));
                    } catch (Exception e) {
                        redirigir(exchange, "/?accion=negocios&err=" + urlEncode(e.getMessage()));
                    }
                    return;
                } else if ("crear_alquiler".equals(accion)) {
                    try {
                        int idN = Integer.parseInt(post.getOrDefault("id_negocio", "0"));
                        double val = Double.parseDouble(post.getOrDefault("valor", "0"));
                        ctrlAlquiler.crear(idN, post.get("fecha_inicio"), post.get("fecha_limite"), val);
                        redirigir(exchange, "/?accion=alquileres&msg=" + urlEncode("Alquiler asignado en Hexagonal."));
                    } catch (Exception e) {
                        redirigir(exchange, "/?accion=alquileres&err=" + urlEncode(e.getMessage()));
                    }
                    return;
                } else if ("crear_pago".equals(accion)) {
                    try {
                        int idN = Integer.parseInt(post.getOrDefault("id_negocio", "0"));
                        double monto = Double.parseDouble(post.getOrDefault("monto", "0"));
                        ctrlPago.procesar(idN, monto, post.get("fecha_pago"), post.get("metodo_pago"));
                        redirigir(exchange, "/?accion=pagos&msg=" + urlEncode("Pago asentado y saldo actualizado."));
                    } catch (Exception e) {
                        redirigir(exchange, "/?accion=pagos&err=" + urlEncode(e.getMessage()));
                    }
                    return;
                }
            }

            if ("eliminar_usuario".equals(accion)) {
                int id = Integer.parseInt(params.getOrDefault("id", "0"));
                ctrlUsuario.eliminar(id);
                redirigir(exchange, "/?accion=usuarios&msg=" + urlEncode("Usuario eliminado."));
                return;
            } else if ("eliminar_negocio".equals(accion)) {
                int id = Integer.parseInt(params.getOrDefault("id", "0"));
                ctrlNegocio.eliminar(id);
                redirigir(exchange, "/?accion=negocios&msg=" + urlEncode("Negocio eliminado."));
                return;
            }

            String html = generarHtml(accion, msg, err);
            byte[] bytes = html.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
            exchange.sendResponseHeaders(200, bytes.length);
            try (OutputStream os = exchange.getResponseBody()) { os.write(bytes); }
        }

        private static void redirigir(HttpExchange ex, String dest) throws IOException {
            ex.getResponseHeaders().set("Location", dest);
            ex.sendResponseHeaders(302, -1);
        }

        private static String urlEncode(String val) {
            return java.net.URLEncoder.encode(val, StandardCharsets.UTF_8);
        }

        private static Map<String, String> parsearParametros(String q) {
            Map<String, String> r = new HashMap<>();
            if (q == null || q.isEmpty()) return r;
            for (String p : q.split("&")) {
                String[] part = p.split("=");
                if (part.length == 2) r.put(URLDecoder.decode(part[0], StandardCharsets.UTF_8), URLDecoder.decode(part[1], StandardCharsets.UTF_8));
                else if (part.length == 1) r.put(URLDecoder.decode(part[0], StandardCharsets.UTF_8), "");
            }
            return r;
        }

        private String generarHtml(String accion, String msg, String err) {
            StringBuilder sb = new StringBuilder();
            sb.append("<!DOCTYPE html><html lang='es'><head><meta charset='UTF-8'>");
            sb.append("<title>SIGEPAC - Arquitectura Hexagonal (Java)</title>");
            sb.append("<style>");
            sb.append("body { font-family: system-ui, sans-serif; background: #faf5ff; color: #1e1b4b; margin: 0; }");
            sb.append(".navbar { background: #6b21a8; color: white; padding: 1rem 2rem; display: flex; justify-content: space-between; align-items: center; }");
            sb.append(".navbar a { color: #f3e8ff; text-decoration: none; margin-left: 1rem; font-weight: 500; }");
            sb.append(".container { max-width: 1100px; margin: 2rem auto; padding: 0 1rem; }");
            sb.append(".badge { background: #f3e8ff; color: #6b21a8; padding: 0.2rem 0.5rem; border-radius: 4px; font-weight: bold; }");
            sb.append(".alert-success { background: #dcfce7; color: #166534; padding: 0.75rem; border-radius: 6px; margin-bottom: 1rem; }");
            sb.append(".alert-danger { background: #fee2e2; color: #991b1b; padding: 0.75rem; border-radius: 6px; margin-bottom: 1rem; }");
            sb.append(".grid { display: grid; grid-template-columns: 1fr 1.6fr; gap: 1.5rem; }");
            sb.append(".card { background: white; padding: 1.5rem; border-radius: 8px; border: 1px solid #e9d5ff; box-shadow: 0 1px 3px rgba(0,0,0,0.05); }");
            sb.append(".form-group { margin-bottom: 1rem; }");
            sb.append(".form-group label { display: block; font-size: 0.85rem; font-weight: 600; margin-bottom: 0.3rem; }");
            sb.append(".form-control { width: 100%; box-sizing: border-box; padding: 0.5rem; border: 1px solid #d8b4fe; border-radius: 4px; }");
            sb.append(".btn { background: #6b21a8; color: white; padding: 0.6rem 1.2rem; border: none; border-radius: 4px; cursor: pointer; font-weight: 600; text-decoration: none; display: inline-block; }");
            sb.append(".btn-danger { background: #dc2626; color: white; padding: 0.2rem 0.5rem; border-radius: 4px; font-size: 0.8rem; }");
            sb.append("table { width: 100%; border-collapse: collapse; margin-top: 1rem; font-size: 0.9rem; }");
            sb.append("th, td { padding: 0.6rem; border-bottom: 1px solid #e9d5ff; text-align: left; }");
            sb.append("th { background: #f3e8ff; color: #4c1d95; }");
            sb.append("</style></head><body>");

            sb.append("<div class='navbar'>");
            sb.append("<div><span class='badge'>HEXAGONAL JAVA</span> <strong>SIGEPAC</strong> - Plaza Cívica de Pitalito</div>");
            sb.append("<div>");
            sb.append("<a href='/?accion=negocios'>Negocios</a>");
            sb.append("<a href='/?accion=alquileres'>Alquileres</a>");
            sb.append("<a href='/?accion=pagos'>Pagos</a>");
            sb.append("<a href='/?accion=usuarios'>Usuarios</a>");
            sb.append("<a href='/?accion=login'>Cerrar Sesión</a>");
            sb.append("</div></div>");

            sb.append("<div class='container'>");
            if (!msg.isEmpty()) sb.append("<div class='alert-success'>").append(msg).append("</div>");
            if (!err.isEmpty()) sb.append("<div class='alert-danger'>").append(err).append("</div>");

            if ("login".equals(accion)) {
                sb.append("<div style='max-width:400px; margin:2rem auto;' class='card'>");
                sb.append("<h2>Inicio de Sesión (Hexagonal)</h2>");
                sb.append("<form method='POST' action='/?accion=login_enviar'>");
                sb.append("<div class='form-group'><label>Correo:</label><input class='form-control' type='email' name='correo' value='admin@sigepac.com' required></div>");
                sb.append("<div class='form-group'><label>Contraseña:</label><input class='form-control' type='password' name='contrasena' value='Admin123*' required></div>");
                sb.append("<button class='btn' type='submit'>Ingresar</button>");
                sb.append("</form></div>");
            } else if ("usuarios".equals(accion)) {
                sb.append("<h2>Gestión de Usuarios (Puertos y Adaptadores)</h2><div class='grid'>");
                sb.append("<div class='card'><h3>Nuevo Usuario</h3><form method='POST' action='/?accion=crear_usuario'>");
                sb.append("<div class='form-group'><label>Nombre:*</label><input class='form-control' name='nombre' required></div>");
                sb.append("<div class='form-group'><label>Apellido:*</label><input class='form-control' name='apellido' required></div>");
                sb.append("<div class='form-group'><label>Correo:*</label><input class='form-control' type='email' name='correo' required></div>");
                sb.append("<div class='form-group'><label>Contraseña:*</label><input class='form-control' type='password' name='contrasena' required></div>");
                sb.append("<div class='form-group'><label>Teléfono:*</label><input class='form-control' name='telefono' required></div>");
                sb.append("<div class='form-group'><label>Rol:*</label><select class='form-control' name='rol'><option value='PROPIETARIO'>Propietario</option><option value='ADMINISTRADORA'>Administradora</option></select></div>");
                sb.append("<button class='btn' type='submit'>Guardar</button></form></div>");
                sb.append("<div class='card'><h3>Usuarios</h3><table><tr><th>Nombre</th><th>Correo</th><th>Rol</th><th>Acciones</th></tr>");
                for (Usuario u : ctrlUsuario.listar()) {
                    sb.append("<tr><td>").append(u.getNombreCompleto()).append("</td>");
                    sb.append("<td>").append(u.getCorreo()).append("</td><td>").append(u.getRol()).append("</td>");
                    sb.append("<td><a class='btn-danger' href='/?accion=eliminar_usuario&id=").append(u.getId()).append("'>Eliminar</a></td></tr>");
                }
                sb.append("</table></div></div>");
            } else if ("negocios".equals(accion)) {
                sb.append("<h2>Locales Comerciales (Hexagonal)</h2><div class='grid'>");
                sb.append("<div class='card'><h3>Nuevo Negocio</h3><form method='POST' action='/?accion=crear_negocio'>");
                sb.append("<div class='form-group'><label>Nombre:*</label><input class='form-control' name='nombre' required></div>");
                sb.append("<div class='form-group'><label>Código Local:*</label><input class='form-control' name='local_numero' required></div>");
                sb.append("<div class='form-group'><label>Propietario:*</label><select class='form-control' name='id_usuario'>");
                for (Usuario u : ctrlUsuario.listar()) {
                    sb.append("<option value='").append(u.getId()).append("'>").append(u.getNombreCompleto()).append("</option>");
                }
                sb.append("</select></div><button class='btn' type='submit'>Guardar</button></form></div>");
                sb.append("<div class='card'><h3>Locales Activos</h3><table><tr><th>Local</th><th>Negocio</th><th>Propietario</th><th>Acciones</th></tr>");
                for (Negocio n : ctrlNegocio.listar()) {
                    sb.append("<tr><td><strong>").append(n.getNumeroLocal()).append("</strong></td>");
                    sb.append("<td>").append(n.getNombreComercial()).append("</td><td>").append(n.getNombrePropietario()).append("</td>");
                    sb.append("<td><a class='btn-danger' href='/?accion=eliminar_negocio&id=").append(n.getId()).append("'>Eliminar</a></td></tr>");
                }
                sb.append("</table></div></div>");
            } else if ("alquileres".equals(accion)) {
                sb.append("<h2>Alquileres y Deudas (Hexagonal)</h2><div class='grid'>");
                sb.append("<div class='card'><h3>Asignar Alquiler</h3><form method='POST' action='/?accion=crear_alquiler'>");
                sb.append("<div class='form-group'><label>Negocio:*</label><select class='form-control' name='id_negocio'>");
                for (Negocio n : ctrlNegocio.listar()) {
                    sb.append("<option value='").append(n.getId()).append("'>").append(n.getNombreComercial()).append(" (").append(n.getNumeroLocal()).append(")</option>");
                }
                sb.append("</select></div>");
                sb.append("<div class='form-group'><label>Fecha Inicio:*</label><input class='form-control' type='date' name='fecha_inicio' value='2026-10-14' required></div>");
                sb.append("<div class='form-group'><label>Fecha Vencimiento:*</label><input class='form-control' type='date' name='fecha_limite' value='2026-11-14' required></div>");
                sb.append("<div class='form-group'><label>Valor:*</label><input class='form-control' type='number' step='0.01' name='valor' value='350000' required></div>");
                sb.append("<button class='btn' type='submit'>Registrar</button></form></div>");
                sb.append("<div class='card'><h3>Alquileres</h3><table><tr><th>Negocio</th><th>Vence</th><th>Valor</th><th>Saldo Actual</th><th>Estado</th></tr>");
                for (Alquiler a : ctrlAlquiler.listar()) {
                    sb.append("<tr><td><strong>").append(a.getNombreNegocio()).append("</strong></td>");
                    sb.append("<td>").append(a.getFechaCorte()).append("</td>");
                    sb.append("<td>$").append(a.getCanonMensual()).append("</td>");
                    sb.append("<td style='color:red; font-weight:bold;'>$").append(a.getSaldoPorPagar()).append("</td>");
                    sb.append("<td>").append(a.getEstadoDeuda()).append("</td></tr>");
                }
                sb.append("</table></div></div>");
            } else if ("pagos".equals(accion)) {
                sb.append("<h2>Registro de Pagos (Hexagonal)</h2><div class='grid'>");
                sb.append("<div class='card'><h3>Nuevo Pago</h3><form method='POST' action='/?accion=crear_pago'>");
                sb.append("<div class='form-group'><label>Negocio:*</label><select class='form-control' name='id_negocio'>");
                for (Negocio n : ctrlNegocio.listar()) {
                    sb.append("<option value='").append(n.getId()).append("'>").append(n.getNombreComercial()).append(" - ").append(n.getNumeroLocal()).append("</option>");
                }
                sb.append("</select></div>");
                sb.append("<div class='form-group'><label>Monto:*</label><input class='form-control' type='number' step='0.01' name='monto' value='120000' required></div>");
                sb.append("<div class='form-group'><label>Fecha:*</label><input class='form-control' type='date' name='fecha_pago' value='2026-10-14' required></div>");
                sb.append("<div class='form-group'><label>Forma Pago:*</label><select class='form-control' name='metodo_pago'><option value='EFECTIVO'>Efectivo</option><option value='TRANSFERENCIA'>Transferencia</option></select></div>");
                sb.append("<button class='btn' type='submit'>Procesar Abono</button></form></div>");
                sb.append("<div class='card'><h3>Historial de Pagos</h3><table><tr><th>Fecha</th><th>Negocio</th><th>Monto</th><th>Forma</th></tr>");
                for (Pago p : ctrlPago.listar()) {
                    sb.append("<tr><td>").append(p.getFechaAbono()).append("</td>");
                    sb.append("<td>").append(p.getNombreNegocio()).append("</td>");
                    sb.append("<td style='color:green; font-weight:bold;'>$").append(p.getMontoAbonado()).append("</td>");
                    sb.append("<td>").append(p.getFormaAbono()).append("</td></tr>");
                }
                sb.append("</table></div></div>");
            }

            sb.append("</div></body></html>");
            return sb.toString();
        }
    }
}
