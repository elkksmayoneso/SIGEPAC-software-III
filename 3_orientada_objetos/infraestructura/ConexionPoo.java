package sigepac.poo.infraestructura;
import java.sql.*;
/** Configuración JDBC y unidad de trabajo de la base exclusiva del dominio POO. */
public final class ConexionPoo {
 private static final ThreadLocal<Connection> TX=new ThreadLocal<>();private ConexionPoo(){}
 public static Connection abrir()throws SQLException{Connection c=TX.get();if(c!=null)return (Connection)java.lang.reflect.Proxy.newProxyInstance(Connection.class.getClassLoader(),new Class[]{Connection.class},(proxy,method,args)->{if(method.getName().equals("close"))return null;try{return method.invoke(c,args);}catch(java.lang.reflect.InvocationTargetException e){throw e.getCause();}});String url=System.getenv().getOrDefault("SIGEPAC_POO_JDBC_URL","jdbc:mysql://localhost:3306/sigepac_poo_java?useSSL=false&serverTimezone=UTC");return DriverManager.getConnection(url,System.getenv().getOrDefault("SIGEPAC_POO_DB_USER","root"),System.getenv().getOrDefault("SIGEPAC_POO_DB_PASSWORD",""));}
 public static <T>T transaccion(Operacion<T> op){if(TX.get()!=null){try{return op.ejecutar();}catch(Exception e){throw fallo(e);}}try(Connection c=abrir()){c.setAutoCommit(false);TX.set(c);try{T r=op.ejecutar();c.commit();return r;}catch(Exception e){c.rollback();throw fallo(e);}finally{TX.remove();}}catch(SQLException e){throw fallo(e);}}
 public static RuntimeException fallo(Exception e){return new IllegalStateException("Error JDBC en la base POO: "+e.getMessage(),e);}@FunctionalInterface public interface Operacion<T>{T ejecutar()throws Exception;}
}
