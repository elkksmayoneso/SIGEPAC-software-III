package sigepac.poo.dominio.objetos_valor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

public final class Dinero {
    private final BigDecimal valor;

    public Dinero(double cantidad) {
        if (cantidad < 0) {
            throw new IllegalArgumentException("El valor monetario no puede ser negativo.");
        }
        this.valor = BigDecimal.valueOf(cantidad).setScale(2, RoundingMode.HALF_UP);
    }

    public Dinero(BigDecimal cantidad) {
        if (cantidad.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("El valor monetario no puede ser negativo.");
        }
        this.valor = cantidad.setScale(2, RoundingMode.HALF_UP);
    }

    public double getCantidad() {
        return valor.doubleValue();
    }

    public Dinero restar(Dinero otro) {
        BigDecimal resultado = this.valor.subtract(otro.valor);
        if (resultado.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Operación inválida: El balance resultante no puede ser negativo.");
        }
        return new Dinero(resultado);
    }

    public Dinero sumar(Dinero otro) {
        return new Dinero(this.valor.add(otro.valor));
    }

    public boolean esCero() {
        return valor.compareTo(BigDecimal.ZERO) == 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Dinero dinero = (Dinero) o;
        return valor.compareTo(dinero.valor) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(valor);
    }

    @Override
    public String toString() {
        return "$" + valor.toString();
    }
}
