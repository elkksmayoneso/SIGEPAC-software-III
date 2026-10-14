package sigepac.capas.dominio.entidades;

public class EntidadNegocio {
    private Integer id;
    private int idUsuario;
    private String nombre;
    private String localCodigo;
    private boolean activo;
    private String propietarioNombre;

    public EntidadNegocio(Integer id, int idUsuario, String nombre, String localCodigo, boolean activo, String propietarioNombre) {
        this.id = id;
        this.idUsuario = idUsuario;
        this.nombre = nombre;
        this.localCodigo = localCodigo;
        this.activo = activo;
        this.propietarioNombre = propietarioNombre;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public int getIdUsuario() { return idUsuario; }
    public String getNombre() { return nombre; }
    public String getLocalCodigo() { return localCodigo; }
    public boolean isActivo() { return activo; }
    public String getPropietarioNombre() { return propietarioNombre; }
}
