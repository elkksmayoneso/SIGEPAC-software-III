package sigepac.mvc.controladores;

import sigepac.mvc.modelos.ModeloNegocio;
import java.util.*;

public class ControladorNegocio {
    private final ModeloNegocio modeloNegocio = new ModeloNegocio();

    public List<Map<String, Object>> listarNegocios() {
        return modeloNegocio.obtenerTodos();
    }

    public boolean guardarNegocio(int idUsuario, String nombre, String localNumero) {
        // HU-03: Exigir nombre, local y propietario
        if (idUsuario <= 0 || nombre == null || nombre.trim().isEmpty() || localNumero == null || localNumero.trim().isEmpty()) {
            throw new IllegalArgumentException("Debe ingresar el nombre del negocio, número de local y asignar un propietario.");
        }

        boolean exito = modeloNegocio.crear(idUsuario, nombre.trim(), localNumero.trim());
        if (!exito) {
            throw new IllegalStateException("No se pudo registrar el negocio. Verifique los datos o si el propietario existe.");
        }
        return true;
    }

    public boolean eliminarNegocio(int id) {
        return modeloNegocio.eliminar(id);
    }
}
