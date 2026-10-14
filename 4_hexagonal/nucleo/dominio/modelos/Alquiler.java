package sigepac.hexagonal.nucleo.dominio.modelos;

import sigepac.hexagonal.nucleo.dominio.excepciones.ValidacionDatosExcepcion;

public class Alquiler {
    private final Integer id;
    private final int negocioId;
    private final String fechaInicio;
    private final String fechaCorte;
    private final double canonMensual;
    private double saldoPorPagar;
    private String estadoDeuda;
    private String nombreNegocio; // Atributo contextual de consulta

    public Alquiler(Integer id, int negocioId, String fechaInicio, String fechaCorte, double canonMensual, double saldoPorPagar, String estadoDeuda) {
        if (negocioId <= 0) {
            throw new ValidacionDatosExcepcion("El negocio asociado a la deuda/alquiler es obligatorio.");
        }
        if (canonMensual <= 0) {
            throw new ValidacionDatosExcepcion("El canon mensual de alquiler debe ser mayor a cero.");
        }
        if (saldoPorPagar < 0) {
            throw new ValidacionDatosExcepcion("El saldo por pagar no puede ser negativo.");
        }

        this.id = id;
        this.negocioId = negocioId;
        this.fechaInicio = fechaInicio != null ? fechaInicio : "";
        this.fechaCorte = fechaCorte != null ? fechaCorte : "";
        this.canonMensual = canonMensual;
        this.saldoPorPagar = saldoPorPagar;
        this.estadoDeuda = (estadoDeuda == null || estadoDeuda.trim().isEmpty()) ? "PENDIENTE" : estadoDeuda.trim();
    }

    public synchronized void abonar(double monto) {
        if (monto <= 0) {
            throw new ValidacionDatosExcepcion("El valor a abonar debe ser estrictamente positivo.");
        }
        if (monto > this.saldoPorPagar) {
            throw new ValidacionDatosExcepcion("El monto abonado ($" + monto + ") supera el saldo pendiente ($" + this.saldoPorPagar + ").");
        }
        this.saldoPorPagar -= monto;
        if (this.saldoPorPagar <= 0.0001) {
            this.saldoPorPagar = 0.0;
            this.estadoDeuda = "PAGADO";
        } else {
            this.estadoDeuda = "ABONADO";
        }
    }

    public Integer getId() { return id; }
    public int getNegocioId() { return negocioId; }
    public String getFechaInicio() { return fechaInicio; }
    public String getFechaCorte() { return fechaCorte; }
    public double getCanonMensual() { return canonMensual; }
    public double getSaldoPorPagar() { return saldoPorPagar; }
    public String getEstadoDeuda() { return estadoDeuda; }
    public String getNombreNegocio() { return nombreNegocio != null ? nombreNegocio : "Negocio #" + negocioId; }
    public void setNombreNegocio(String nombreNegocio) { this.nombreNegocio = nombreNegocio; }
}
