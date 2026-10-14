package sigepac.hexagonal.adaptadores.salida.persistencia;

import sigepac.hexagonal.nucleo.puertos.salida.IPuertoPersistenciaPago;
import sigepac.hexagonal.nucleo.dominio.modelos.Pago;

import java.util.*;
import java.util.stream.Collectors;

public class AdaptadorPersistenciaPago implements IPuertoPersistenciaPago {
    private final BaseDatosHexagonal bd = BaseDatosHexagonal.getInstancia();

    @Override
    public Pago guardar(Pago pago) {
        int id = bd.secuenciaPago.getAndIncrement();
        Pago nuevo = new Pago(
                id,
                pago.getAlquilerId(),
                pago.getMontoAbonado(),
                pago.getFechaAbono(),
                pago.getFormaAbono()
        );
        nuevo.setNombreNegocio(pago.getNombreNegocio());
        bd.tablaPagos.put(id, nuevo);
        return nuevo;
    }

    @Override
    public Optional<Pago> buscarPorId(int id) {
        return Optional.ofNullable(bd.tablaPagos.get(id));
    }

    @Override
    public List<Pago> listarTodos() {
        return new ArrayList<>(bd.tablaPagos.values());
    }

    @Override
    public List<Pago> listarPorAlquiler(int alquilerId) {
        return bd.tablaPagos.values().stream()
                .filter(p -> p.getAlquilerId() == alquilerId)
                .collect(Collectors.toList());
    }
}
