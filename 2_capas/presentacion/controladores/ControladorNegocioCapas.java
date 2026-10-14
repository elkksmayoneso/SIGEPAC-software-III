package sigepac.capas.presentacion.controladores;

import sigepac.capas.logica_negocio.contratos.IServicioNegocio;
import sigepac.capas.dominio.entidades.EntidadNegocio;
import java.util.List;

public class ControladorNegocioCapas {
    private final IServicioNegocio servicioNegocio;

    public ControladorNegocioCapas(IServicioNegocio servicioNegocio) {
        this.servicioNegocio = servicioNegocio;
    }

    public List<EntidadNegocio> listar() {
        return servicioNegocio.listarNegocios();
    }

    public int crear(int idUsuario, String nombre, String localCodigo) {
        return servicioNegocio.registrarNegocio(idUsuario, nombre, localCodigo);
    }

    public boolean eliminar(int id) {
        return servicioNegocio.eliminarNegocio(id);
    }
}
