package sigepac.hexagonal.adaptadores.salida.persistencia;

import sigepac.hexagonal.nucleo.puertos.salida.IPuertoPersistenciaNegocio;
import sigepac.hexagonal.nucleo.dominio.modelos.Negocio;

import java.util.*;

public class AdaptadorPersistenciaNegocio implements IPuertoPersistenciaNegocio {
    private final BaseDatosHexagonal bd = BaseDatosHexagonal.getInstancia();

    @Override
    public Negocio guardar(Negocio negocio) {
        int id = bd.secuenciaNegocio.getAndIncrement();
        Negocio nuevo = new Negocio(
                id,
                negocio.getPropietarioId(),
                negocio.getNombreComercial(),
                negocio.getNumeroLocal(),
                negocio.getEstado()
        );
        bd.tablaNegocios.put(id, nuevo);
        return nuevo;
    }

    @Override
    public boolean actualizar(Negocio negocio) {
        if (negocio.getId() != null && bd.tablaNegocios.containsKey(negocio.getId())) {
            bd.tablaNegocios.put(negocio.getId(), negocio);
            return true;
        }
        return false;
    }

    @Override
    public boolean eliminar(int id) {
        return bd.tablaNegocios.remove(id) != null;
    }

    @Override
    public Optional<Negocio> buscarPorId(int id) {
        return Optional.ofNullable(bd.tablaNegocios.get(id));
    }

    @Override
    public Optional<Negocio> buscarPorLocal(String numeroLocal) {
        return bd.tablaNegocios.values().stream()
                .filter(n -> n.getNumeroLocal().equalsIgnoreCase(numeroLocal))
                .findFirst();
    }

    @Override
    public List<Negocio> listarTodos() {
        return new ArrayList<>(bd.tablaNegocios.values());
    }
}
