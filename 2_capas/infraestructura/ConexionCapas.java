package sigepac.capas.infraestructura;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/** Configuración JDBC y unidad de trabajo de la base exclusiva de la arquitectura por capas. */
public final class ConexionCapas {
    private static final ThreadLocal<Connection> TRANSACCION = new ThreadLocal<>();
    private ConexionCapas() {}

    public static Connection abrir() throws SQLException {
        Connection actual = TRANSACCION.get();
        if (actual != null) return (Connection) java.lang.reflect.Proxy.newProxyInstance(Connection.class.getClassLoader(), new Class[]{Connection.class}, (proxy, method, args) -> {
            if (method.getName().equals("close")) return null;
            try { return method.invoke(actual, args); } catch (java.lang.reflect.InvocationTargetException e) { throw e.getCause(); }
        });
        String url = System.getenv().getOrDefault("SIGEPAC_CAPAS_JDBC_URL", "jdbc:mysql://localhost:3306/sigepac_capas_java?useSSL=false&serverTimezone=UTC");
        String usuario = System.getenv().getOrDefault("SIGEPAC_CAPAS_DB_USER", "root");
        String clave = System.getenv().getOrDefault("SIGEPAC_CAPAS_DB_PASSWORD", "");
        return DriverManager.getConnection(url, usuario, clave);
    }

    public static <T> T transaccion(Operacion<T> operacion) {
        if (TRANSACCION.get() != null) {
            try { return operacion.ejecutar(); } catch (Exception e) { throw fallo(e); }
        }
        try (Connection c = abrir()) {
            c.setAutoCommit(false);
            TRANSACCION.set(c);
            try { T resultado = operacion.ejecutar(); c.commit(); return resultado; }
            catch (Exception e) { c.rollback(); if (e instanceof RuntimeException re) throw re; throw fallo(e); }
            finally { TRANSACCION.remove(); }
        } catch (SQLException e) { throw fallo(e); }
    }

    public static RuntimeException fallo(Exception e) {
        return new IllegalStateException("Error de persistencia en la base MySQL/MariaDB de Capas: " + e.getMessage(), e);
    }
    @FunctionalInterface public interface Operacion<T> { T ejecutar() throws Exception; }
}
