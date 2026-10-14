package sigepac.capas.dominio.entidades;

public class EntidadUsuario {
    private Integer id;
    private String nombre;
    private String apellido;
    private String correo;
    private String contrasenaHash;
    private String rol;
    private String telefono;

    public EntidadUsuario(Integer id, String nombre, String apellido, String correo, String contrasenaHash, String rol, String telefono) {
        this.id = id;
        this.nombre = nombre;
        this.apellido = apellido;
        this.correo = correo;
        this.contrasenaHash = contrasenaHash;
        this.rol = rol;
        this.telefono = telefono;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getNombre() { return nombre; }
    public String getApellido() { return apellido; }
    public String getNombreCompleto() { return nombre + " " + apellido; }
    public String getCorreo() { return correo; }
    public String getContrasenaHash() { return contrasenaHash; }
    public String getRol() { return rol; }
    public String getTelefono() { return telefono; }
}
