package sigepac.poo.aplicacion.manejadores;

import sigepac.poo.dominio.repositorios.IRepositorioDeudaAlquiler;
import sigepac.poo.dominio.repositorios.IRepositorioNegocioComercial;
import sigepac.poo.dominio.entidades.DeudaAlquiler;
import sigepac.poo.dominio.entidades.NegocioComercial;
import sigepac.poo.dominio.objetos_valor.Dinero;
import java.util.List;

public class ManejadorDeuda {
    private final IRepositorioDeudaAlquiler repoDeuda;
    private final IRepositorioNegocioComercial repoNegocio;

    public ManejadorDeuda(IRepositorioDeudaAlquiler repoDeuda, IRepositorioNegocioComercial repoNegocio) {
        this.repoDeuda = repoDeuda;
        this.repoNegocio = repoNegocio;
    }

    public List<DeudaAlquiler> listar() {
        return repoDeuda.obtenerTodos();
    }

    public int registrarDeuda(int idNegocio, String fechaInicio, String fechaLimite, double valor) {
        // HU-04: Asociar deuda a un negocio existente
        NegocioComercial negocio = repoNegocio.buscarPorId(idNegocio);
        if (negocio == null) {
            throw new IllegalArgumentException("El negocio seleccionado no existe. Registre el negocio primero.");
        }

        Dinero dinero = new Dinero(valor);
        DeudaAlquiler deuda = new DeudaAlquiler(
            null,
            idNegocio,
            fechaInicio,
            fechaLimite,
            dinero,
            dinero,
            "PENDIENTE",
            negocio.getNombreComercial(),
            negocio.getNumeroLocal()
        );

        return repoDeuda.guardarDeuda(deuda);
    }
}
