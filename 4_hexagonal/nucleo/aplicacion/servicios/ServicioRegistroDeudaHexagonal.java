package sigepac.hexagonal.nucleo.aplicacion.servicios;

import sigepac.hexagonal.nucleo.puertos.entrada.ICasoUsoRegistrarDeuda;
import sigepac.hexagonal.nucleo.puertos.salida.IPuertoPersistenciaAlquiler;
import sigepac.hexagonal.nucleo.puertos.salida.IPuertoPersistenciaNegocio;
import sigepac.hexagonal.nucleo.dominio.modelos.Alquiler;
import sigepac.hexagonal.nucleo.dominio.modelos.Negocio;
import sigepac.hexagonal.nucleo.dominio.excepciones.NegocioNoEncontradoExcepcion;
import sigepac.hexagonal.nucleo.dominio.excepciones.ValidacionDatosExcepcion;

import java.util.List;
import java.util.Optional;

public class ServicioRegistroDeudaHexagonal implements ICasoUsoRegistrarDeuda {
    private final IPuertoPersistenciaAlquiler puertoAlquiler;
    private final IPuertoPersistenciaNegocio puertoNegocio;

    public ServicioRegistroDeudaHexagonal(IPuertoPersistenciaAlquiler puertoAlquiler, IPuertoPersistenciaNegocio puertoNegocio) {
        this.puertoAlquiler = puertoAlquiler;
        this.puertoNegocio = puertoNegocio;
    }

    @Override
    public Alquiler registrarDeudaAlquiler(int negocioId, String fechaInicio, String fechaCorte, double canonMensual) {
        if (!puertoNegocio.buscarPorId(negocioId).isPresent()) {
            throw new NegocioNoEncontradoExcepcion("No se puede registrar deuda. El negocio con ID " + negocioId + " no existe.");
        }

        Alquiler alquiler = new Alquiler(null, negocioId, fechaInicio, fechaCorte, canonMensual, canonMensual, "PENDIENTE");
        return puertoAlquiler.guardar(alquiler);
    }

    @Override
    public List<Alquiler> listarAlquileres() {
        List<Alquiler> lista = puertoAlquiler.listarTodos();
        for (Alquiler a : lista) {
            Optional<Negocio> n = puertoNegocio.buscarPorId(a.getNegocioId());
            n.ifPresent(negocio -> a.setNombreNegocio(negocio.getNombreComercial()));
        }
        return lista;
    }

    @Override
    public List<Alquiler> listarPorNegocio(int negocioId) {
        return puertoAlquiler.listarPorNegocio(negocioId);
    }

    @Override
    public Alquiler buscarPorId(int id) {
        Alquiler a = puertoAlquiler.buscarPorId(id)
                .orElseThrow(() -> new ValidacionDatosExcepcion("Registro de deuda/alquiler no encontrado con ID: " + id));
        Optional<Negocio> n = puertoNegocio.buscarPorId(a.getNegocioId());
        n.ifPresent(negocio -> a.setNombreNegocio(negocio.getNombreComercial()));
        return a;
    }
}
