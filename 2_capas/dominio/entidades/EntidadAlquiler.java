package sigepac.capas.dominio.entidades;

public class EntidadAlquiler {
    private Integer id;
    private int idNegocio;
    private String fechaInicio;
    private String fechaVencimiento;
    private double valorCuota;
    private double saldoActual;
    private String estado;
    private String negocioNombre;
    private String localCodigo;

    public EntidadAlquiler(Integer id, int idNegocio, String fechaInicio, String fechaVencimiento, double valorCuota, double saldoActual, String estado, String negocioNombre, String localCodigo) {
        this.id = id;
        this.idNegocio = idNegocio;
        this.fechaInicio = fechaInicio;
        this.fechaVencimiento = fechaVencimiento;
        this.valorCuota = valorCuota;
        this.saldoActual = saldoActual;
        this.estado = estado;
        this.negocioNombre = negocioNombre;
        this.localCodigo = localCodigo;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public int getIdNegocio() { return idNegocio; }
    public String getFechaInicio() { return fechaInicio; }
    public String getFechaVencimiento() { return fechaVencimiento; }
    public double getValorCuota() { return valorCuota; }
    public double getSaldoActual() { return saldoActual; }
    public void setSaldoActual(double saldoActual) { this.saldoActual = saldoActual; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public String getNegocioNombre() { return negocioNombre; }
    public String getLocalCodigo() { return localCodigo; }
}
