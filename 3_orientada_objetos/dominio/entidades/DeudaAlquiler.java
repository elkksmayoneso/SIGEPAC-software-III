package sigepac.poo.dominio.entidades;

import sigepac.poo.dominio.objetos_valor.Dinero;
import sigepac.poo.dominio.excepciones.SobrepagoDeudaExcepcion;

public class DeudaAlquiler {
    private Integer idDeuda;
    private final int idNegocio;
    private final String fechaInicio;
    private final String fechaLimite;
    private final Dinero montoOriginal;
    private Dinero saldoActual;
    private String estado;
    private String nombreNegocio;
    private String localNumero;

    public DeudaAlquiler(Integer idDeuda, int idNegocio, String fechaInicio, String fechaLimite, Dinero montoOriginal, Dinero saldoActual, String estado, String nombreNegocio, String localNumero) {
        this.idDeuda = idDeuda;
        this.idNegocio = idNegocio;
        this.fechaInicio = fechaInicio;
        this.fechaLimite = fechaLimite;
        this.montoOriginal = montoOriginal;
        this.saldoActual = saldoActual;
        this.estado = estado;
        this.nombreNegocio = nombreNegocio;
        this.localNumero = localNumero;
    }

    // HU-05: El Aggregate Root controla las reglas de negocio de saldos
    public TransaccionPago registrarAbono(Dinero montoAbono, String metodoPago, String fechaPago) {
        if (montoAbono.getCantidad() <= 0) {
            throw new IllegalArgumentException("El monto del abono debe ser superior a cero.");
        }

        if (montoAbono.getCantidad() > this.saldoActual.getCantidad()) {
            throw new SobrepagoDeudaExcepcion("El abono ($" + montoAbono.getCantidad() + ") excede el saldo pendiente ($" + this.saldoActual.getCantidad() + ").");
        }

        // Modificación del estado encapsulado
        this.saldoActual = this.saldoActual.restar(montoAbono);

        if (this.saldoActual.esCero()) {
            this.estado = "AL_DIA";
        } else {
            this.estado = "PENDIENTE";
        }

        return new TransaccionPago(
            null,
            this.idDeuda != null ? this.idDeuda : 0,
            montoAbono,
            fechaPago,
            metodoPago,
            this.nombreNegocio
        );
    }

    public Integer getIdDeuda() { return idDeuda; }
    public void setIdDeuda(Integer idDeuda) { this.idDeuda = idDeuda; }
    public int getIdNegocio() { return idNegocio; }
    public String getFechaInicio() { return fechaInicio; }
    public String getFechaLimite() { return fechaLimite; }
    public Dinero getMontoOriginal() { return montoOriginal; }
    public Dinero getSaldoActual() { return saldoActual; }
    public String getEstado() { return estado; }
    public String getNombreNegocio() { return nombreNegocio; }
    public String getLocalNumero() { return localNumero; }
}
