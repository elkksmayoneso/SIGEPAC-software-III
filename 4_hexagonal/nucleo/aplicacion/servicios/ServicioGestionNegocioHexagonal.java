package sigepac.hexagonal.nucleo.aplicacion.servicios;

import sigepac.hexagonal.nucleo.puertos.entrada.ICasoUsoGestionarNegocio;
import sigepac.hexagonal.nucleo.puertos.salida.IPuertoPersistenciaNegocio;
import sigepac.hexagonal.nucleo.puertos.salida.IPuertoPersistenciaUsuario;
import sigepac.hexagonal.nucleo.dominio.modelos.Negocio;
import sigepac.hexagonal.nucleo.dominio.modelos.Usuario;
import sigepac.hexagonal.nucleo.dominio.excepciones.ValidacionDatosExcepcion;
import sigepac.hexagonal.nucleo.dominio.excepciones.NegocioNoEncontradoExcepcion;

import java.util.List;
import java.util.Optional;

public class ServicioGestionNegocioHexagonal implements ICasoUsoGestionarNegocio {
    private final IPuertoPersistenciaNegocio puertoNegocio;
    private final IPuertoPersistenciaUsuario puertoUsuario;

    public ServicioGestionNegocioHexagonal(IPuertoPersistenciaNegocio puertoNegocio, IPuertoPersistenciaUsuario puertoUsuario) {
        this.puertoNegocio = puertoNegocio;
        this.puertoUsuario = puertoUsuario;
    }

    @Override
    public Negocio registrarNegocio(int propietarioId, String nombreComercial, String numeroLocal) {
        if (!puertoUsuario.buscarPorId(propietarioId).isPresent()) {
            throw new ValidacionDatosExcepcion("El propietario seleccionado no existe en el sistema.");
        }
        if (puertoNegocio.buscarPorLocal(numeroLocal != null ? numeroLocal.trim().toUpperCase() : "").isPresent()) {
            throw new ValidacionDatosExcepcion("El número de local " + numeroLocal + " ya está asignado a otro negocio.");
        }

        Negocio nuevo = new Negocio(null, propietarioId, nombreComercial, numeroLocal, "ACTIVO");
        return puertoNegocio.guardar(nuevo);
    }

    @Override
    public boolean actualizarNegocio(int id, String nombreComercial, String numeroLocal) {
        Negocio actual = puertoNegocio.buscarPorId(id)
                .orElseThrow(() -> new NegocioNoEncontradoExcepcion("El negocio con ID " + id + " no existe."));

        Negocio actualizado = new Negocio(id, actual.getPropietarioId(), nombreComercial, numeroLocal, actual.getEstado());
        return puertoNegocio.actualizar(actualizado);
    }

    @Override
    public boolean eliminarNegocio(int id) {
        return puertoNegocio.eliminar(id);
    }

    @Override
    public List<Negocio> listarNegocios() {
        List<Negocio> lista = puertoNegocio.listarTodos();
        for (Negocio n : lista) {
            Optional<Usuario> u = puertoUsuario.buscarPorId(n.getPropietarioId());
            u.ifPresent(usuario -> n.setNombrePropietario(usuario.getNombreCompleto()));
        }
        return lista;
    }

    @Override
    public Negocio buscarPorId(int id) {
        Negocio n = puertoNegocio.buscarPorId(id)
                .orElseThrow(() -> new NegocioNoEncontradoExcepcion("Negocio no encontrado con ID: " + id));
        Optional<Usuario> u = puertoUsuario.buscarPorId(n.getPropietarioId());
        u.ifPresent(usuario -> n.setNombrePropietario(usuario.getNombreCompleto()));
        return n;
    }
}
