package sigepac.mvc.servidor;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import sigepac.mvc.controladores.*;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class ServidorMvc {
    private static final int PUERTO = 8001;

    private static final ControladorAutenticacion ctrlAuth = new ControladorAutenticacion();
    private static final ControladorUsuario ctrlUsuario = new ControladorUsuario();
    private static final ControladorNegocio ctrlNegocio = new ControladorNegocio();
    private static final ControladorAlquiler ctrlAlquiler = new ControladorAlquiler();
    private static final ControladorPago ctrlPago = new ControladorPago();

    public static void main(String[] args) throws IOException {
        HttpServer servidor = HttpServer.create(new InetSocketAddress(PUERTO), 0);
        
        servidor.createContext("/", new ManejadorPrincipal());
        servidor.setExecutor(null);
        System.out.println("====================================================================");
        System.out.println(" Servidor SIGEPAC (MVC en Java) activo en http://localhost:" + PUERTO);
        System.out.println("====================================================================");
        servidor.start();
    }

    static class ManejadorPrincipal implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String metodo = exchange.getRequestMethod();
            String ruta = exchange.getRequestURI().getPath();
            String query = exchange.getRequestURI().getQuery();
            Map<String, String> parametros = parsearParametros(query);

            String accion = parametros.getOrDefault("accion", "negocios");
            String mensaje = parametros.getOrDefault("msg", "");
            String error = parametros.getOrDefault("err", "");

            if ("POST".equalsIgnoreCase(metodo)) {
                InputStreamReader isr = new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8);
                BufferedReader br = new BufferedReader(isr);
                StringBuilder cuerpo = new StringBuilder();
                String linea;
                while ((linea = br.readLine()) != null) {
                    cuerpo.append(linea);
                }
                Map<String, String> datosPost = parsearParametros(cuerpo.toString());

                if ("/login".equals(ruta) || "login_enviar".equals(accion)) {
                    try {
                        ctrlAuth.iniciarSesion(datosPost.get("correo"), datosPost.get("contrasena"));
                        redirigir(exchange, "/?accion=negocios&msg=" + urlEncode("Inicio de sesión exitoso."));
                    } catch (Exception e) {
                        redirigir(exchange, "/?accion=login&err=" + urlEncode(e.getMessage()));
                    }
                    return;
                } else if ("crear_usuario".equals(accion)) {
                    try {
                        ctrlUsuario.guardarUsuario(
                            datosPost.get("nombre"),
                            datosPost.get("apellido"),
                            datosPost.get("correo"),
                            datosPost.get("contrasena"),
                            datosPost.get("rol"),
                            datosPost.get("telefono")
                        );
                        redirigir(exchange, "/?accion=usuarios&msg=" + urlEncode("Usuario creado exitosamente."));
                    } catch (Exception e) {
                        redirigir(exchange, "/?accion=usuarios&err=" + urlEncode(e.getMessage()));
                    }
                    return;
                } else if ("crear_negocio".equals(accion)) {
                    try {
                        int idUsuario = Integer.parseInt(datosPost.getOrDefault("id_usuario", "0"));
                        ctrlNegocio.guardarNegocio(idUsuario, datosPost.get("nombre"), datosPost.get("local_numero"));
                        redirigir(exchange, "/?accion=negocios&msg=" + urlEncode("Negocio registrado exitosamente."));
                    } catch (Exception e) {
                        redirigir(exchange, "/?accion=negocios&err=" + urlEncode(e.getMessage()));
                    }
                    return;
                } else if ("crear_alquiler".equals(accion)) {
                    try {
                        int idNegocio = Integer.parseInt(datosPost.getOrDefault("id_negocio", "0"));
                        double valor = Double.parseDouble(datosPost.getOrDefault("valor", "0"));
                        ctrlAlquiler.registrarAlquiler(idNegocio, datosPost.get("fecha_inicio"), datosPost.get("fecha_limite"), valor);
                        redirigir(exchange, "/?accion=alquileres&msg=" + urlEncode("Alquiler asignado exitosamente."));
                    } catch (Exception e) {
                        redirigir(exchange, "/?accion=alquileres&err=" + urlEncode(e.getMessage()));
                    }
                    return;
                } else if ("crear_pago".equals(accion)) {
                    try {
                        int idNegocio = Integer.parseInt(datosPost.getOrDefault("id_negocio", "0"));
                        double monto = Double.parseDouble(datosPost.getOrDefault("monto", "0"));
                        ctrlPago.procesarPago(idNegocio, monto, datosPost.get("fecha_pago"), datosPost.get("metodo_pago"));
                        redirigir(exchange, "/?accion=pagos&msg=" + urlEncode("Pago procesado y saldo actualizado inmediatamente."));
                    } catch (Exception e) {
                        redirigir(exchange, "/?accion=pagos&err=" + urlEncode(e.getMessage()));
                    }
                    return;
                }
            }

            // Manejo de peticiones GET
            if ("eliminar_usuario".equals(accion)) {
                int id = Integer.parseInt(parametros.getOrDefault("id", "0"));
                ctrlUsuario.eliminarUsuario(id);
                redirigir(exchange, "/?accion=usuarios&msg=" + urlEncode("Usuario eliminado correctamente."));
                return;
            } else if ("eliminar_negocio".equals(accion)) {
                int id = Integer.parseInt(parametros.getOrDefault("id", "0"));
                ctrlNegocio.eliminarNegocio(id);
                redirigir(exchange, "/?accion=negocios&msg=" + urlEncode("Negocio eliminado correctamente."));
                return;
            }

            String html = generarHtml(accion, mensaje, error);
            byte[] bytes = html.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
            exchange.sendResponseHeaders(200, bytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }
        }

        private static void redirigir(HttpExchange exchange, String destino) throws IOException {
            exchange.getResponseHeaders().set("Location", destino);
            exchange.sendResponseHeaders(302, -1);
        }

        private static String urlEncode(String valor) {
            return java.net.URLEncoder.encode(valor, StandardCharsets.UTF_8);
        }

        private static Map<String, String> parsearParametros(String query) {
            Map<String, String> resultado = new HashMap<>();
            if (query == null || query.isEmpty()) return resultado;
            for (String par : query.split("&")) {
                String[] partes = par.split("=");
                if (partes.length == 2) {
                    resultado.put(URLDecoder.decode(partes[0], StandardCharsets.UTF_8),
                                  URLDecoder.decode(partes[1], StandardCharsets.UTF_8));
                } else if (partes.length == 1) {
                    resultado.put(URLDecoder.decode(partes[0], StandardCharsets.UTF_8), "");
                }
            }
            return resultado;
        }

        private String generarHtml(String accion, String mensaje, String error) {
            StringBuilder sb = new StringBuilder();
            sb.append("<!DOCTYPE html><html lang='es'><head><meta charset='UTF-8'>");
            sb.append("<title>SIGEPAC - MVC (Java)</title>");
            sb.append("<style>");
            sb.append("body { font-family: system-ui, sans-serif; background: #f8fafc; color: #0f172a; margin: 0; }");
            sb.append(".navbar { background: #1e3a8a; color: white; padding: 1rem 2rem; display: flex; justify-content: space-between; align-items: center; }");
            sb.append(".navbar a { color: #e2e8f0; text-decoration: none; margin-left: 1rem; font-weight: 500; }");
            sb.append(".container { max-width: 1100px; margin: 2rem auto; padding: 0 1rem; }");
            sb.append(".badge { background: #dbeafe; color: #1e3a8a; padding: 0.2rem 0.5rem; border-radius: 4px; font-weight: bold; }");
            sb.append(".alert-success { background: #dcfce7; color: #166534; padding: 0.75rem; border-radius: 6px; margin-bottom: 1rem; }");
            sb.append(".alert-danger { background: #fee2e2; color: #991b1b; padding: 0.75rem; border-radius: 6px; margin-bottom: 1rem; }");
            sb.append(".grid { display: grid; grid-template-columns: 1fr 1.6fr; gap: 1.5rem; }");
            sb.append(".card { background: white; padding: 1.5rem; border-radius: 8px; border: 1px solid #e2e8f0; box-shadow: 0 1px 3px rgba(0,0,0,0.05); }");
            sb.append(".form-group { margin-bottom: 1rem; }");
            sb.append(".form-group label { display: block; font-size: 0.85rem; font-weight: 600; margin-bottom: 0.3rem; }");
            sb.append(".form-control { width: 100%; box-sizing: border-box; padding: 0.5rem; border: 1px solid #cbd5e1; border-radius: 4px; }");
            sb.append(".btn { background: #1e3a8a; color: white; padding: 0.6rem 1.2rem; border: none; border-radius: 4px; cursor: pointer; font-weight: 600; text-decoration: none; display: inline-block; }");
            sb.append(".btn-danger { background: #dc2626; color: white; padding: 0.2rem 0.5rem; border-radius: 4px; font-size: 0.8rem; }");
            sb.append("table { width: 100%; border-collapse: collapse; margin-top: 1rem; font-size: 0.9rem; }");
            sb.append("th, td { padding: 0.6rem; border-bottom: 1px solid #e2e8f0; text-align: left; }");
            sb.append("th { background: #f1f5f9; }");
            sb.append("</style></head><body>");

            sb.append("<div class='navbar'>");
            sb.append("<div><span class='badge'>MVC JAVA</span> <strong>SIGEPAC</strong> - Plaza Cívica de Pitalito</div>");
            sb.append("<div>");
            sb.append("<a href='/?accion=negocios'>Negocios</a>");
            sb.append("<a href='/?accion=alquileres'>Alquileres</a>");
            sb.append("<a href='/?accion=pagos'>Pagos</a>");
            sb.append("<a href='/?accion=usuarios'>Usuarios</a>");
            sb.append("<a href='/?accion=login'>Cerrar Sesión</a>");
            sb.append("</div></div>");

            sb.append("<div class='container'>");
            if (!mensaje.isEmpty()) sb.append("<div class='alert-success'>").append(mensaje).append("</div>");
            if (!error.isEmpty()) sb.append("<div class='alert-danger'>").append(error).append("</div>");

            if ("login".equals(accion)) {
                sb.append("<div style='max-width:400px; margin:2rem auto;' class='card'>");
                sb.append("<h2>Inicio de Sesión (HU-01)</h2>");
                sb.append("<form method='POST' action='/?accion=login_enviar'>");
                sb.append("<div class='form-group'><label>Correo:</label><input class='form-control' type='email' name='correo' value='admin@sigepac.com' required></div>");
                sb.append("<div class='form-group'><label>Contraseña:</label><input class='form-control' type='password' name='contrasena' value='Admin123*' required></div>");
                sb.append("<button class='btn' type='submit'>Ingresar</button>");
                sb.append("</form></div>");
            } else if ("usuarios".equals(accion)) {
                sb.append("<h2>Gestión de Usuarios (HU-02)</h2><div class='grid'>");
                sb.append("<div class='card'><h3>Nuevo Usuario</h3><form method='POST' action='/?accion=crear_usuario'>");
                sb.append("<div class='form-group'><label>Nombre:*</label><input class='form-control' name='nombre' required></div>");
                sb.append("<div class='form-group'><label>Apellido:*</label><input class='form-control' name='apellido' required></div>");
                sb.append("<div class='form-group'><label>Correo:*</label><input class='form-control' type='email' name='correo' required></div>");
                sb.append("<div class='form-group'><label>Contraseña:*</label><input class='form-control' type='password' name='contrasena' required></div>");
                sb.append("<div class='form-group'><label>Teléfono:*</label><input class='form-control' name='telefono' required></div>");
                sb.append("<div class='form-group'><label>Rol:*</label><select class='form-control' name='rol'><option value='PROPIETARIO'>Propietario</option><option value='ADMINISTRADORA'>Administradora</option></select></div>");
                sb.append("<button class='btn' type='submit'>Guardar</button></form></div>");
                sb.append("<div class='card'><h3>Usuarios Registrados</h3><table><tr><th>Nombre</th><th>Correo</th><th>Rol</th><th>Acciones</th></tr>");
                for (Map<String, Object> u : ctrlUsuario.listarUsuarios()) {
                    sb.append("<tr><td>").append(u.get("nombre")).append(" ").append(u.get("apellido")).append("</td>");
                    sb.append("<td>").append(u.get("correo")).append("</td><td>").append(u.get("rol")).append("</td>");
                    sb.append("<td><a class='btn-danger' href='/?accion=eliminar_usuario&id=").append(u.get("id_usuario")).append("'>Eliminar</a></td></tr>");
                }
                sb.append("</table></div></div>");
            } else if ("negocios".equals(accion)) {
                sb.append("<h2>Registro de Negocios (HU-03)</h2><div class='grid'>");
                sb.append("<div class='card'><h3>Nuevo Negocio</h3><form method='POST' action='/?accion=crear_negocio'>");
                sb.append("<div class='form-group'><label>Nombre:*</label><input class='form-control' name='nombre' required></div>");
                sb.append("<div class='form-group'><label>Local:*</label><input class='form-control' name='local_numero' required></div>");
                sb.append("<div class='form-group'><label>Propietario:*</label><select class='form-control' name='id_usuario'>");
                for (Map<String, Object> u : ctrlUsuario.listarUsuarios()) {
                    sb.append("<option value='").append(u.get("id_usuario")).append("'>").append(u.get("nombre")).append(" ").append(u.get("apellido")).append("</option>");
                }
                sb.append("</select></div><button class='btn' type='submit'>Guardar</button></form></div>");
                sb.append("<div class='card'><h3>Locales Activos</h3><table><tr><th>Local</th><th>Negocio</th><th>Propietario</th><th>Acciones</th></tr>");
                for (Map<String, Object> n : ctrlNegocio.listarNegocios()) {
                    sb.append("<tr><td><strong>").append(n.get("local_numero")).append("</strong></td>");
                    sb.append("<td>").append(n.get("nombre")).append("</td><td>").append(n.get("propietario")).append("</td>");
                    sb.append("<td><a class='btn-danger' href='/?accion=eliminar_negocio&id=").append(n.get("id_negocio")).append("'>Eliminar</a></td></tr>");
                }
                sb.append("</table></div></div>");
            } else if ("alquileres".equals(accion)) {
                sb.append("<h2>Control de Alquileres / Deudas (HU-04)</h2><div class='grid'>");
                sb.append("<div class='card'><h3>Asignar Cuota</h3><form method='POST' action='/?accion=crear_alquiler'>");
                sb.append("<div class='form-group'><label>Negocio:*</label><select class='form-control' name='id_negocio'>");
                for (Map<String, Object> n : ctrlNegocio.listarNegocios()) {
                    sb.append("<option value='").append(n.get("id_negocio")).append("'>").append(n.get("nombre")).append(" (").append(n.get("local_numero")).append(")</option>");
                }
                sb.append("</select></div>");
                sb.append("<div class='form-group'><label>Fecha Inicio:*</label><input class='form-control' type='date' name='fecha_inicio' value='2026-10-14' required></div>");
                sb.append("<div class='form-group'><label>Fecha Límite:*</label><input class='form-control' type='date' name='fecha_limite' value='2026-11-14' required></div>");
                sb.append("<div class='form-group'><label>Valor:*</label><input class='form-control' type='number' step='0.01' name='valor' value='250000' required></div>");
                sb.append("<button class='btn' type='submit'>Registrar Alquiler</button></form></div>");
                sb.append("<div class='card'><h3>Alquileres Registrados</h3><table><tr><th>Negocio</th><th>Vence</th><th>Valor</th><th>Saldo Pendiente</th><th>Estado</th></tr>");
                for (Map<String, Object> a : ctrlAlquiler.listarAlquileres()) {
                    sb.append("<tr><td><strong>").append(a.get("nombre_negocio")).append("</strong> (").append(a.get("local_numero")).append(")</td>");
                    sb.append("<td>").append(a.get("fecha_limite")).append("</td>");
                    sb.append("<td>$").append(a.get("valor_total")).append("</td>");
                    sb.append("<td style='color:red; font-weight:bold;'>$").append(a.get("saldo_pendiente")).append("</td>");
                    sb.append("<td>").append(a.get("estado")).append("</td></tr>");
                }
                sb.append("</table></div></div>");
            } else if ("pagos".equals(accion)) {
                sb.append("<h2>Registro de Pagos y Actualización de Saldo (HU-05)</h2><div class='grid'>");
                sb.append("<div class='card'><h3>Registrar Abono</h3><form method='POST' action='/?accion=crear_pago'>");
                sb.append("<div class='form-group'><label>Negocio:*</label><select class='form-control' name='id_negocio'>");
                for (Map<String, Object> n : ctrlNegocio.listarNegocios()) {
                    sb.append("<option value='").append(n.get("id_negocio")).append("'>").append(n.get("nombre")).append(" - ").append(n.get("local_numero")).append("</option>");
                }
                sb.append("</select></div>");
                sb.append("<div class='form-group'><label>Monto a Pagar:*</label><input class='form-control' type='number' step='0.01' name='monto' value='100000' required></div>");
                sb.append("<div class='form-group'><label>Fecha:*</label><input class='form-control' type='date' name='fecha_pago' value='2026-10-14' required></div>");
                sb.append("<div class='form-group'><label>Método:*</label><select class='form-control' name='metodo_pago'><option value='EFECTIVO'>Efectivo</option><option value='TRANSFERENCIA'>Transferencia</option></select></div>");
                sb.append("<button class='btn' type='submit'>Procesar Pago</button></form></div>");
                sb.append("<div class='card'><h3>Historial de Pagos</h3><table><tr><th>Fecha</th><th>Negocio</th><th>Monto</th><th>Método</th></tr>");
                for (Map<String, Object> p : ctrlPago.listarPagos()) {
                    sb.append("<tr><td>").append(p.get("fecha_pago")).append("</td>");
                    sb.append("<td>").append(p.get("nombre_negocio")).append("</td>");
                    sb.append("<td style='color:green; font-weight:bold;'>$").append(p.get("monto")).append("</td>");
                    sb.append("<td>").append(p.get("metodo_pago")).append("</td></tr>");
                }
                sb.append("</table></div></div>");
            }

            sb.append("</div></body></html>");
            return sb.toString();
        }
    }
}
