package sigepac.poo.dominio.objetos_valor;

import java.util.Objects;
import java.util.regex.Pattern;

public final class CorreoElectronico {
    private static final Pattern PATRON = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");
    private final String direccion;

    public CorreoElectronico(String direccion) {
        if (direccion == null || !PATRON.matcher(direccion.trim()).matches()) {
            throw new IllegalArgumentException("Formato de correo electrónico inválido: '" + direccion + "'.");
        }
        this.direccion = direccion.trim().toLowerCase();
    }

    public String getValor() {
        return direccion;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CorreoElectronico that = (CorreoElectronico) o;
        return Objects.equals(direccion, that.direccion);
    }

    @Override
    public int hashCode() {
        return Objects.hash(direccion);
    }

    @Override
    public String toString() {
        return direccion;
    }
}
