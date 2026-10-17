package sigepac.capas.acceso_datos.repositorios;
import sigepac.capas.acceso_datos.contratos.IRepositorioNegocio;
import sigepac.capas.dominio.entidades.EntidadNegocio;
import sigepac.capas.infraestructura.ConexionCapas;
import java.sql.*; import java.util.*;
public class RepositorioNegocio implements IRepositorioNegocio {
 private EntidadNegocio map(ResultSet r)throws SQLException{return new EntidadNegocio(r.getInt("neg_id"),r.getInt("usr_id"),r.getString("neg_nombre"),r.getString("neg_local_codigo"),r.getBoolean("neg_activo"),r.getString("propietario"));}
 public EntidadNegocio buscarPorId(int id){String q="SELECT n.*,CONCAT(u.usr_nombre,' ',u.usr_apellido) propietario FROM tbl_negocios n JOIN tbl_usuarios u ON u.usr_id=n.usr_id WHERE n.neg_id=?";try(Connection c=ConexionCapas.abrir();PreparedStatement p=c.prepareStatement(q)){p.setInt(1,id);try(ResultSet r=p.executeQuery()){return r.next()?map(r):null;}}catch(SQLException e){throw ConexionCapas.fallo(e);}}
 public boolean existe(int id){try(Connection c=ConexionCapas.abrir();PreparedStatement p=c.prepareStatement("SELECT 1 FROM tbl_negocios WHERE neg_id=? AND neg_activo=1 FOR UPDATE")){p.setInt(1,id);try(ResultSet r=p.executeQuery()){return r.next();}}catch(SQLException e){throw ConexionCapas.fallo(e);}}
 public List<EntidadNegocio> obtenerTodos(){List<EntidadNegocio> a=new ArrayList<>();String q="SELECT n.*,CONCAT(u.usr_nombre,' ',u.usr_apellido) propietario FROM tbl_negocios n JOIN tbl_usuarios u ON u.usr_id=n.usr_id ORDER BY n.neg_id";try(Connection c=ConexionCapas.abrir();Statement s=c.createStatement();ResultSet r=s.executeQuery(q)){while(r.next())a.add(map(r));return a;}catch(SQLException e){throw ConexionCapas.fallo(e);}}
 public int guardar(EntidadNegocio n){String q="INSERT INTO tbl_negocios(usr_id,neg_nombre,neg_local_codigo,neg_activo) VALUES(?,?,?,1)";try(Connection c=ConexionCapas.abrir();PreparedStatement p=c.prepareStatement(q,Statement.RETURN_GENERATED_KEYS)){p.setInt(1,n.getIdUsuario());p.setString(2,n.getNombre());p.setString(3,n.getLocalCodigo());p.executeUpdate();try(ResultSet r=p.getGeneratedKeys()){if(r.next())return r.getInt(1);throw new SQLException("Falta id generado");}}catch(SQLException e){throw ConexionCapas.fallo(e);}}
 public boolean eliminar(int id){try(Connection c=ConexionCapas.abrir();PreparedStatement p=c.prepareStatement("UPDATE tbl_negocios SET neg_activo=0 WHERE neg_id=?")){p.setInt(1,id);return p.executeUpdate()>0;}catch(SQLException e){throw ConexionCapas.fallo(e);}}
}
