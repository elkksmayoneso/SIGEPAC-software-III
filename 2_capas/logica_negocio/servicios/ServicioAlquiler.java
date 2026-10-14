package sigepac.capas.logica_negocio.servicios;

import sigepac.capas.logica_negocio.contratos.IServicioAlquiler;
import sigepac.capas.acceso_datos.contratos.IRepositorioAlquiler;
import sigepac.capas.acceso_datos.contratos.IRepositorioNegocio;
import sigepac.capas.dominio.entidades.EntidadAlquiler;
import sigepac.capas.dominio.entidades.EntidadNegocio;
import java.util.List;

public class ServicioAlquiler implements IServicioAlquiler {
    private final IRepositorioAlquiler repoAlquiler;
    private final IRepositorioNegocio repoNegocio;

    public ServicioAlquiler(IRepositorioAlquiler repoAlquiler, IRepositorioNegocio repoNegocio) {
        this.repoAlquiler = repoAlquiler;
        this.repoNegocio = repoNegocio;
    }

    @Override
    public List<EntidadAlquiler> listarAlquileres() {
        return repoAlquiler.obtenerTodos();
    }

    @Override
    public int registrarAlquiler(int idNegocio, String fechaInicio, String fechaVencimiento, double valorCuota) {
        // HU-04: Asociar deuda a un negocio existente
        EntidadNegocio negocio = repoNegocio.buscarPorId(idNegocio);
        if (negocio == null) {
            throw new IllegalArgumentException("El negocio especificado no existe. Registre el negocio primero.");
        }

        if (valorCuota <= 0) {
            throw new IllegalArgumentException("El valor de la cuota debe ser mayor a cero.");
        }

        EntidadAlquiler nuevo = new EntidadAlquiler(
            null,
            idNegocio,
            fechaInicio,
            fechaVencimiento,
            valorCuota,
            valorCuota,
            "PENDIENTE",
            negocio.getNombre(),
            negocio.getLocalCodigo()
        );

        return repoAlquiler.guardar(nuevo);
    }
}
