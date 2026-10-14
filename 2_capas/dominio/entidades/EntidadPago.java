package sigepac.capas.dominio.entidades;

public class EntidadPago {
    private Integer id;
    private int idAlquiler;
    private double monto;
    private String fechaRegistro;
    private String formaPago;
    private String negocioNombre;

    public EntidadPago(Integer id, int idAlquiler, double monto, String fechaRegistro, String formaPago, String negocioNombre) {
        this.id = id;
        this.idAlquiler = idAlquiler;
        this.monto = monto;
        this.fechaRegistro = fechaRegistro;
        this.formaPago = formaPago;
        this.negocioNombre = negocioNombre;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public int getIdAlquiler() { return idAlquiler; }
    public double getMonto() { return monto; }
    public String getFechaRegistro() { return fechaRegistro; }
    public String getFormaPago() { return formaPago; }
    public String getNegocioNombre() { return negocioNombre; }
}
