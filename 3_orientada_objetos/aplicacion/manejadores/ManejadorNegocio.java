package sigepac.poo.aplicacion.manejadores;

import sigepac.poo.dominio.repositorios.IRepositorioNegocioComercial;
import sigepac.poo.dominio.repositorios.IRepositorioCuentaUsuario;
import sigepac.poo.dominio.entidades.NegocioComercial;
import sigepac.poo.dominio.entidades.CuentaUsuario;
import java.util.List;

public class ManejadorNegocio {
    private final IRepositorioNegocioComercial repoNegocio;
    private final IRepositorioCuentaUsuario repoCuenta;

    public ManejadorNegocio(IRepositorioNegocioComercial repoNegocio, IRepositorioCuentaUsuario repoCuenta) {
        this.repoNegocio = repoNegocio;
        this.repoCuenta = repoCuenta;
    }

    public List<NegocioComercial> listar() {
        return repoNegocio.obtenerTodos();
    }

    public int registrar(int idPropietario, String nombre, String localNumero) {
        // HU-03: Exigir nombre, local y propietario
        if (idPropietario <= 0 || nombre == null || nombre.trim().isEmpty() || localNumero == null || localNumero.trim().isEmpty()) {
            throw new IllegalArgumentException("Debe ingresar nombre, número de local y asignar un propietario.");
        }

        CuentaUsuario propietario = repoCuenta.buscarPorId(idPropietario);
        if (propietario == null) {
            throw new IllegalArgumentException("El propietario seleccionado no existe en el sistema.");
        }

        NegocioComercial negocio = new NegocioComercial(
            null,
            idPropietario,
            nombre.trim(),
            localNumero.trim(),
            true,
            propietario.getNombreCompleto()
        );

        return repoNegocio.guardar(negocio);
    }

    public boolean eliminar(int id) {
        return repoNegocio.eliminar(id);
    }
}
