package sigepac.hexagonal.nucleo.dominio.modelos;

import sigepac.hexagonal.nucleo.dominio.excepciones.ValidacionDatosExcepcion;

public class Usuario {
    private final Integer id;
    private final String nombre;
    private final String apellido;
    private final String correo;
    private final String contrasenaHash;
    private final String rol;
    private final String telefono;

    public Usuario(Integer id, String nombre, String apellido, String correo, String contrasenaHash, String rol, String telefono) {
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new ValidacionDatosExcepcion("El nombre del usuario es obligatorio.");
        }
        if (apellido == null || apellido.trim().isEmpty()) {
            throw new ValidacionDatosExcepcion("El apellido del usuario es obligatorio.");
        }
        if (correo == null || !correo.contains("@")) {
            throw new ValidacionDatosExcepcion("El correo electrónico no es válido.");
        }
        if (telefono == null || telefono.trim().isEmpty()) {
            throw new ValidacionDatosExcepcion("El teléfono del usuario es obligatorio.");
        }

        this.id = id;
        this.nombre = nombre.trim();
        this.apellido = apellido.trim();
        this.correo = correo.trim().toLowerCase();
        this.contrasenaHash = contrasenaHash;
        this.rol = (rol == null || rol.trim().isEmpty()) ? "PROPIETARIO" : rol.trim().toUpperCase();
        this.telefono = telefono.trim();
    }

    public Integer getId() { return id; }
    public String getNombre() { return nombre; }
    public String getApellido() { return apellido; }
    public String getNombreCompleto() { return nombre + " " + apellido; }
    public String getCorreo() { return correo; }
    public String getContrasenaHash() { return contrasenaHash; }
    public String getRol() { return rol; }
    public String getTelefono() { return telefono; }
}
