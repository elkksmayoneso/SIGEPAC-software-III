package sigepac.poo.dominio.repositorios;

import sigepac.poo.dominio.entidades.CuentaUsuario;
import sigepac.poo.dominio.objetos_valor.CorreoElectronico;
import java.util.List;

public interface IRepositorioCuentaUsuario {
    CuentaUsuario buscarPorCorreo(CorreoElectronico correo);
    CuentaUsuario buscarPorId(int id);
    List<CuentaUsuario> obtenerTodos();
    int guardar(CuentaUsuario cuenta);
    boolean eliminar(int id);
}
