package sigepac.hexagonal.nucleo.dominio.modelos;

import sigepac.hexagonal.nucleo.dominio.excepciones.ValidacionDatosExcepcion;

public class Negocio {
    private final Integer id;
    private final int propietarioId;
    private final String nombreComercial;
    private final String numeroLocal;
    private final String estado;
    private String nombrePropietario; // Atributo contextual de consulta

    public Negocio(Integer id, int propietarioId, String nombreComercial, String numeroLocal, String estado) {
        if (propietarioId <= 0) {
            throw new ValidacionDatosExcepcion("El propietario asignado al negocio no es válido.");
        }
        if (nombreComercial == null || nombreComercial.trim().isEmpty()) {
            throw new ValidacionDatosExcepcion("El nombre comercial del negocio es obligatorio.");
        }
        if (numeroLocal == null || numeroLocal.trim().isEmpty()) {
            throw new ValidacionDatosExcepcion("El número de local es obligatorio.");
        }

        this.id = id;
        this.propietarioId = propietarioId;
        this.nombreComercial = nombreComercial.trim();
        this.numeroLocal = numeroLocal.trim().toUpperCase();
        this.estado = (estado == null || estado.trim().isEmpty()) ? "ACTIVO" : estado.trim();
    }

    public Integer getId() { return id; }
    public int getPropietarioId() { return propietarioId; }
    public String getNombreComercial() { return nombreComercial; }
    public String getNumeroLocal() { return numeroLocal; }
    public String getEstado() { return estado; }
    public String getNombrePropietario() { return nombrePropietario != null ? nombrePropietario : "ID: " + propietarioId; }
    public void setNombrePropietario(String nombrePropietario) { this.nombrePropietario = nombrePropietario; }
}
