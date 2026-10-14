package sigepac.hexagonal.adaptadores.entrada.web;

import sigepac.hexagonal.nucleo.puertos.entrada.ICasoUsoGestionarNegocio;
import sigepac.hexagonal.nucleo.dominio.modelos.Negocio;
import java.util.List;

public class ControladorNegocioHexagonal {
    private final ICasoUsoGestionarNegocio casoUsoNegocio;

    public ControladorNegocioHexagonal(ICasoUsoGestionarNegocio casoUsoNegocio) {
        this.casoUsoNegocio = casoUsoNegocio;
    }

    public Negocio crear(int idUsuario, String nombre, String localNumero) {
        return casoUsoNegocio.registrarNegocio(idUsuario, nombre, localNumero);
    }

    public boolean actualizar(int id, String nombre, String localNumero) {
        return casoUsoNegocio.actualizarNegocio(id, nombre, localNumero);
    }

    public boolean eliminar(int id) {
        return casoUsoNegocio.eliminarNegocio(id);
    }

    public List<Negocio> listar() {
        return casoUsoNegocio.listarNegocios();
    }

    public Negocio buscar(int id) {
        return casoUsoNegocio.buscarPorId(id);
    }
}
