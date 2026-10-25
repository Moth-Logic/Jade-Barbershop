package logica;

import java.io.Serializable;
import java.util.Objects;

/**
 * Representa un tipo de servicio o corte ofrecido por la barbería.
 */
public class TipoServicio implements Serializable {
    private static final long serialVersionUID = 1L;

    private String id;
    private String nombre;
    private String descripcion;
    private double precio;
    private int duracionMinutos;

    public TipoServicio() {
    }

    public TipoServicio(String id, String nombre, String descripcion, double precio, int duracionMinutos) {
        this.id = id != null ? id.trim() : "";
        this.nombre = nombre != null ? nombre.trim() : "";
        this.descripcion = descripcion != null ? descripcion.trim() : "";
        this.precio = precio;
        this.duracionMinutos = duracionMinutos;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id != null ? id.trim() : "";
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre != null ? nombre.trim() : "";
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion != null ? descripcion.trim() : "";
    }

    public double getPrecio() {
        return precio;
    }

    public void setPrecio(double precio) {
        this.precio = precio;
    }

    public int getDuracionMinutos() {
        return duracionMinutos;
    }

    public void setDuracionMinutos(int duracionMinutos) {
        this.duracionMinutos = duracionMinutos;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TipoServicio that = (TipoServicio) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return nombre + " - ₡" + String.format("%.2f", precio) + " (" + duracionMinutos + " min)";
    }
}
