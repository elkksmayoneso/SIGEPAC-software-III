package sigepac.poo.infraestructura.persistencia;

import sigepac.poo.dominio.repositorios.IRepositorioCuentaUsuario;
import sigepac.poo.dominio.entidades.CuentaUsuario;
import sigepac.poo.dominio.objetos_valor.CorreoElectronico;
import sigepac.poo.infraestructura.BaseDatosPoo;
import java.util.*;

public class RepositorioCuentaUsuarioMemoria implements IRepositorioCuentaUsuario {
    private final BaseDatosPoo bd = BaseDatosPoo.obtenerInstancia();

    @Override
    public CuentaUsuario buscarPorCorreo(CorreoElectronico correo) {
        synchronized (bd) {
            for (CuentaUsuario c : bd.tablaCuentas.values()) {
                if (c.getCorreo().equals(correo)) {
                    return c;
                }
            }
            return null;
        }
    }

    @Override
    public CuentaUsuario buscarPorId(int id) {
        synchronized (bd) {
            return bd.tablaCuentas.get(id);
        }
    }

    @Override
    public List<CuentaUsuario> obtenerTodos() {
        synchronized (bd) {
            return new ArrayList<>(bd.tablaCuentas.values());
        }
    }

    @Override
    public int guardar(CuentaUsuario cuenta) {
        synchronized (bd) {
            int nuevoId = bd.siguienteIdCuenta();
            CuentaUsuario entidadConId = new CuentaUsuario(
                nuevoId,
                cuenta.getNombre(),
                cuenta.getApellido(),
                cuenta.getCorreo(),
                cuenta.getContrasenaHash(),
                cuenta.getRol(),
                cuenta.getTelefono()
            );
            bd.tablaCuentas.put(nuevoId, entidadConId);
            return nuevoId;
        }
    }

    @Override
    public boolean eliminar(int id) {
        synchronized (bd) {
            return bd.tablaCuentas.remove(id) != null;
        }
    }
}
