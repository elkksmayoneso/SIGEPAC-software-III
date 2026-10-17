package sigepac.hexagonal.adaptadores.salida.persistencia;
import java.sql.*;
/** Driver JDBC y transacción compartida por adaptadores secundarios durante un caso de uso. */
public final class ConexionHexagonal {
 private static final ThreadLocal<Connection> TX=new ThreadLocal<>();private ConexionHexagonal(){}
 public static Connection abrir()throws SQLException{Connection c=TX.get();if(c!=null)return (Connection)java.lang.reflect.Proxy.newProxyInstance(Connection.class.getClassLoader(),new Class[]{Connection.class},(proxy,method,args)->{if(method.getName().equals("close"))return null;try{return method.invoke(c,args);}catch(java.lang.reflect.InvocationTargetException e){throw e.getCause();}});String url=System.getenv().getOrDefault("SIGEPAC_HEX_JDBC_URL","jdbc:mysql://localhost:3306/sigepac_hexagonal_java?useSSL=false&serverTimezone=UTC");return DriverManager.getConnection(url,System.getenv().getOrDefault("SIGEPAC_HEX_DB_USER","root"),System.getenv().getOrDefault("SIGEPAC_HEX_DB_PASSWORD",""));}
 public static <T>T transaccion(java.util.concurrent.Callable<T> op){if(TX.get()!=null){try{return op.call();}catch(Exception e){throw fallo(e);}}try(Connection c=abrir()){c.setAutoCommit(false);TX.set(c);try{T r=op.call();c.commit();return r;}catch(Exception e){c.rollback();if(e instanceof RuntimeException re)throw re;throw fallo(e);}finally{TX.remove();}}catch(SQLException e){throw fallo(e);}}
 public static RuntimeException fallo(Exception e){return new IllegalStateException("Error JDBC en la base Hexagonal: "+e.getMessage(),e);}
}
