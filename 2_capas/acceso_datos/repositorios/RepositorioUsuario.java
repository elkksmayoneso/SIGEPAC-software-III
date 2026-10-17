package sigepac.capas.acceso_datos.repositorios;

import sigepac.capas.acceso_datos.contratos.IRepositorioUsuario;
import sigepac.capas.dominio.entidades.EntidadUsuario;
import sigepac.capas.infraestructura.ConexionCapas;
import java.sql.*;
import java.util.*;

public class RepositorioUsuario implements IRepositorioUsuario {
    private EntidadUsuario mapear(ResultSet r) throws SQLException { return new EntidadUsuario(r.getInt("usr_id"), r.getString("usr_nombre"), r.getString("usr_apellido"), r.getString("usr_correo"), r.getString("usr_contrasena_hash"), r.getString("usr_rol"), r.getString("usr_telefono")); }
    @Override public EntidadUsuario buscarPorCorreo(String correo) { String sql="SELECT * FROM tbl_usuarios WHERE usr_correo=?"; try(Connection c=ConexionCapas.abrir(); PreparedStatement p=c.prepareStatement(sql)){p.setString(1,correo);try(ResultSet r=p.executeQuery()){return r.next()?mapear(r):null;}}catch(SQLException e){throw ConexionCapas.fallo(e);} }
    @Override public EntidadUsuario buscarPorId(int id) { try(Connection c=ConexionCapas.abrir();PreparedStatement p=c.prepareStatement("SELECT * FROM tbl_usuarios WHERE usr_id=?")){p.setInt(1,id);try(ResultSet r=p.executeQuery()){return r.next()?mapear(r):null;}}catch(SQLException e){throw ConexionCapas.fallo(e);} }
    @Override public List<EntidadUsuario> obtenerTodos(){List<EntidadUsuario> out=new ArrayList<>();try(Connection c=ConexionCapas.abrir();Statement s=c.createStatement();ResultSet r=s.executeQuery("SELECT * FROM tbl_usuarios ORDER BY usr_id")){while(r.next())out.add(mapear(r));return out;}catch(SQLException e){throw ConexionCapas.fallo(e);}}
    @Override public int guardar(EntidadUsuario u){String sql="INSERT INTO tbl_usuarios(usr_nombre,usr_apellido,usr_correo,usr_contrasena_hash,usr_rol,usr_telefono) VALUES(?,?,?,?,?,?)";try(Connection c=ConexionCapas.abrir();PreparedStatement p=c.prepareStatement(sql,Statement.RETURN_GENERATED_KEYS)){p.setString(1,u.getNombre());p.setString(2,u.getApellido());p.setString(3,u.getCorreo());p.setString(4,u.getContrasenaHash());p.setString(5,u.getRol());p.setString(6,u.getTelefono());p.executeUpdate();try(ResultSet k=p.getGeneratedKeys()){if(k.next())return k.getInt(1);throw new SQLException("MySQL no devolvió el id del usuario");}}catch(SQLException e){throw ConexionCapas.fallo(e);}}
    @Override public boolean eliminar(int id){try(Connection c=ConexionCapas.abrir();PreparedStatement p=c.prepareStatement("DELETE FROM tbl_usuarios WHERE usr_id=?")){p.setInt(1,id);return p.executeUpdate()>0;}catch(SQLException e){throw ConexionCapas.fallo(e);}}
}
