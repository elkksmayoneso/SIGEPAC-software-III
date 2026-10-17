package sigepac.poo.dominio.entidades;

import sigepac.poo.dominio.objetos_valor.CorreoElectronico;
import sigepac.poo.dominio.excepciones.CredencialesInvalidasExcepcion;
import sigepac.poo.dominio.repositorios.IPuertoCifradorContrasena;

public class CuentaUsuario {
    private final Integer idCuenta;
    private String nombre;
    private String apellido;
    private final CorreoElectronico correo;
    private final String contrasenaHash;
    private final String rol;
    private final String telefono;

    public CuentaUsuario(Integer idCuenta, String nombre, String apellido, CorreoElectronico correo, String contrasenaHash, String rol, String telefono) {
        this.idCuenta = idCuenta;
        this.nombre = nombre;
        this.apellido = apellido;
        this.correo = correo;
        this.contrasenaHash = contrasenaHash;
        this.rol = rol;
        this.telefono = telefono;
    }

    // HU-01 & RNF3: Método rico de entidad para verificación de credenciales
    public void verificarContrasena(String contrasenaPlana, IPuertoCifradorContrasena cifrador) {
        if (!cifrador.verificar(contrasenaPlana, this.contrasenaHash)) {
            throw new CredencialesInvalidasExcepcion("Credenciales incorrectas. Verifique correo y contraseña ingresados.");
        }
    }

    public Integer getIdCuenta() { return idCuenta; }
    public String getNombre() { return nombre; }
    public String getApellido() { return apellido; }
    public String getNombreCompleto() { return nombre + " " + apellido; }
    public CorreoElectronico getCorreo() { return correo; }
    public String getContrasenaHash() { return contrasenaHash; }
    public String getRol() { return rol; }
    public String getTelefono() { return telefono; }
}
