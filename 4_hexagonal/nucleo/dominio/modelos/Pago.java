package sigepac.hexagonal.nucleo.dominio.modelos;

import sigepac.hexagonal.nucleo.dominio.excepciones.ValidacionDatosExcepcion;

public class Pago {
    private final Integer id;
    private final int alquilerId;
    private final double montoAbonado;
    private final String fechaAbono;
    private final String formaAbono;
    private String nombreNegocio; // Atributo contextual de consulta

    public Pago(Integer id, int alquilerId, double montoAbonado, String fechaAbono, String formaAbono) {
        if (alquilerId <= 0) {
            throw new ValidacionDatosExcepcion("El alquiler o deuda de referencia es obligatorio.");
        }
        if (montoAbonado <= 0) {
            throw new ValidacionDatosExcepcion("El monto abonado debe ser mayor a cero.");
        }
        if (fechaAbono == null || fechaAbono.trim().isEmpty()) {
            throw new ValidacionDatosExcepcion("La fecha del abono es requerida.");
        }

        this.id = id;
        this.alquilerId = alquilerId;
        this.montoAbonado = montoAbonado;
        this.fechaAbono = fechaAbono.trim();
        this.formaAbono = (formaAbono == null || formaAbono.trim().isEmpty()) ? "EFECTIVO" : formaAbono.trim().toUpperCase();
    }

    public Integer getId() { return id; }
    public int getAlquilerId() { return alquilerId; }
    public double getMontoAbonado() { return montoAbonado; }
    public String getFechaAbono() { return fechaAbono; }
    public String getFormaAbono() { return formaAbono; }
    public String getNombreNegocio() { return nombreNegocio != null ? nombreNegocio : "Alquiler #" + alquilerId; }
    public void setNombreNegocio(String nombreNegocio) { this.nombreNegocio = nombreNegocio; }
}
