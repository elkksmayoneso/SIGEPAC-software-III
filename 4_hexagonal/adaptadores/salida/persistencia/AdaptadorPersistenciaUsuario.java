package sigepac.hexagonal.adaptadores.salida.persistencia;

import sigepac.hexagonal.nucleo.puertos.salida.IPuertoPersistenciaUsuario;
import sigepac.hexagonal.nucleo.dominio.modelos.Usuario;

import java.util.*;

public class AdaptadorPersistenciaUsuario implements IPuertoPersistenciaUsuario {
    private final BaseDatosHexagonal bd = BaseDatosHexagonal.getInstancia();

    @Override
    public Usuario guardar(Usuario usuario) {
        int id = bd.secuenciaUsuario.getAndIncrement();
        Usuario nuevo = new Usuario(
                id,
                usuario.getNombre(),
                usuario.getApellido(),
                usuario.getCorreo(),
                usuario.getContrasenaHash(),
                usuario.getRol(),
                usuario.getTelefono()
        );
        bd.tablaUsuarios.put(id, nuevo);
        return nuevo;
    }

    @Override
    public boolean actualizar(Usuario usuario) {
        if (usuario.getId() != null && bd.tablaUsuarios.containsKey(usuario.getId())) {
            bd.tablaUsuarios.put(usuario.getId(), usuario);
            return true;
        }
        return false;
    }

    @Override
    public boolean eliminar(int id) {
        return bd.tablaUsuarios.remove(id) != null;
    }

    @Override
    public Optional<Usuario> buscarPorId(int id) {
        return Optional.ofNullable(bd.tablaUsuarios.get(id));
    }

    @Override
    public Optional<Usuario> buscarPorCorreo(String correo) {
        return bd.tablaUsuarios.values().stream()
                .filter(u -> u.getCorreo().equalsIgnoreCase(correo))
                .findFirst();
    }

    @Override
    public List<Usuario> listarTodos() {
        return new ArrayList<>(bd.tablaUsuarios.values());
    }
}
