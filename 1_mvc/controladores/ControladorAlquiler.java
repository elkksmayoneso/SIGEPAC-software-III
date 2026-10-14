package sigepac.mvc.controladores;

import sigepac.mvc.modelos.ModeloAlquiler;
import java.util.*;

public class ControladorAlquiler {
    private final ModeloAlquiler modeloAlquiler = new ModeloAlquiler();

    public List<Map<String, Object>> listarAlquileres() {
        return modeloAlquiler.obtenerTodos();
    }

    public boolean registrarAlquiler(int idNegocio, String fechaInicio, String fechaLimite, double valor) {
        // HU-04: Asociar deuda a un negocio existente
        if (idNegocio <= 0 || valor <= 0) {
            throw new IllegalArgumentException("Debe seleccionar un negocio válido y asignar un valor mayor a cero.");
        }

        boolean exito = modeloAlquiler.crear(idNegocio, fechaInicio, fechaLimite, valor);
        if (!exito) {
            throw new IllegalStateException("El negocio seleccionado no existe en la base de datos.");
        }
        return true;
    }
}
