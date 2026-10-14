package sigepac.poo.dominio.entidades;

public class NegocioComercial {
    private final Integer idNegocio;
    private final int idPropietario;
    private String nombreComercial;
    private String numeroLocal;
    private boolean estaActivo;
    private String nombrePropietario;

    public NegocioComercial(Integer idNegocio, int idPropietario, String nombreComercial, String numeroLocal, boolean estaActivo, String nombrePropietario) {
        this.idNegocio = idNegocio;
        this.idPropietario = idPropietario;
        this.nombreComercial = nombreComercial;
        this.numeroLocal = numeroLocal;
        this.estaActivo = estaActivo;
        this.nombrePropietario = nombrePropietario;
    }

    public Integer getIdNegocio() { return idNegocio; }
    public int getIdPropietario() { return idPropietario; }
    public String getNombreComercial() { return nombreComercial; }
    public String getNumeroLocal() { return numeroLocal; }
    public boolean isEstaActivo() { return estaActivo; }
    public String getNombrePropietario() { return nombrePropietario; }
}
