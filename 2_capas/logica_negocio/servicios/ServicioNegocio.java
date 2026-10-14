package sigepac.capas.logica_negocio.servicios;

import sigepac.capas.logica_negocio.contratos.IServicioNegocio;
import sigepac.capas.acceso_datos.contratos.IRepositorioNegocio;
import sigepac.capas.acceso_datos.contratos.IRepositorioUsuario;
import sigepac.capas.dominio.entidades.EntidadNegocio;
import sigepac.capas.dominio.entidades.EntidadUsuario;
import java.util.List;

public class ServicioNegocio implements IServicioNegocio {
    private final IRepositorioNegocio repoNegocio;
    private final IRepositorioUsuario repoUsuario;

    public ServicioNegocio(IRepositorioNegocio repoNegocio, IRepositorioUsuario repoUsuario) {
        this.repoNegocio = repoNegocio;
        this.repoUsuario = repoUsuario;
    }

    @Override
    public List<EntidadNegocio> listarNegocios() {
        return repoNegocio.obtenerTodos();
    }

    @Override
    public int registrarNegocio(int idUsuario, String nombre, String localCodigo) {
        // HU-03: Exigir nombre, local y propietario
        if (idUsuario <= 0 || nombre == null || nombre.trim().isEmpty() || localCodigo == null || localCodigo.trim().isEmpty()) {
            throw new IllegalArgumentException("Debe ingresar el nombre del negocio, local y propietario.");
        }

        EntidadUsuario propietario = repoUsuario.buscarPorId(idUsuario);
        if (propietario == null) {
            throw new IllegalArgumentException("El propietario seleccionado no existe en el sistema.");
        }

        EntidadNegocio nuevo = new EntidadNegocio(
            null,
            idUsuario,
            nombre.trim(),
            localCodigo.trim(),
            true,
            propietario.getNombreCompleto()
        );

        return repoNegocio.guardar(nuevo);
    }

    @Override
    public boolean eliminarNegocio(int id) {
        return repoNegocio.eliminar(id);
    }
}
