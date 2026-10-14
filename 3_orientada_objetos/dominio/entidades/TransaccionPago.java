package sigepac.poo.dominio.entidades;

import sigepac.poo.dominio.objetos_valor.Dinero;

public class TransaccionPago {
    private Integer idPago;
    private final int idDeuda;
    private final Dinero monto;
    private final String fechaPago;
    private final String metodoPago;
    private String nombreNegocio;

    public TransaccionPago(Integer idPago, int idDeuda, Dinero monto, String fechaPago, String metodoPago, String nombreNegocio) {
        this.idPago = idPago;
        this.idDeuda = idDeuda;
        this.monto = monto;
        this.fechaPago = fechaPago;
        this.metodoPago = metodoPago;
        this.nombreNegocio = nombreNegocio;
    }

    public Integer getIdPago() { return idPago; }
    public void setIdPago(Integer idPago) { this.idPago = idPago; }
    public int getIdDeuda() { return idDeuda; }
    public Dinero getMonto() { return monto; }
    public String getFechaPago() { return fechaPago; }
    public String getMetodoPago() { return metodoPago; }
    public String getNombreNegocio() { return nombreNegocio; }
}
