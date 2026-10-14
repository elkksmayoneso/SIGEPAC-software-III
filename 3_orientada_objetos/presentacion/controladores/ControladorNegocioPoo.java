package sigepac.poo.presentacion.controladores;

import sigepac.poo.aplicacion.manejadores.ManejadorNegocio;
import sigepac.poo.dominio.entidades.NegocioComercial;
import java.util.List;

public class ControladorNegocioPoo {
    private final ManejadorNegocio manejadorNegocio;

    public ControladorNegocioPoo(ManejadorNegocio manejadorNegocio) {
        this.manejadorNegocio = manejadorNegocio;
    }

    public List<NegocioComercial> listar() {
        return manejadorNegocio.listar();
    }

    public int crear(int idPropietario, String nombre, String localNumero) {
        return manejadorNegocio.registrar(idPropietario, nombre, localNumero);
    }

    public boolean eliminar(int id) {
        return manejadorNegocio.eliminar(id);
    }
}
