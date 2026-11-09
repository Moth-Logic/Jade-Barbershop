package logica;

import java.io.Serializable;
import java.util.Objects;

/** Servicio o corte ofrecido por la barbería. */
public class TipoServicio implements Serializable {
    private static final long serialVersionUID = 1L;

    /** Identificador único del servicio. */
    private String id;

    /** Nombre corto del servicio. */
    private String nombre;

    /** Descripción que se muestra en el catálogo. */
    private String descripcion;

    /** Precio en colones costarricenses. */
    private double precio;

    /** Tiempo que ocupa el servicio y que se usa al agendar citas. */
    private int duracionMinutos;

    /** Constructor vacío que necesita la serialización. */
    public TipoServicio() {
    }

    /** Crea un servicio quitando los espacios sobrantes de los textos. */
    public TipoServicio(String id, String nombre, String descripcion, double precio, int duracionMinutos) {
        this.id = id != null ? id.trim() : "";
        this.nombre = nombre != null ? nombre.trim() : "";
        this.descripcion = descripcion != null ? descripcion.trim() : "";
        this.precio = precio;
        this.duracionMinutos = duracionMinutos;
    }

    /** Devuelve el identificador del servicio. */
    public String getId() {
        return id;
    }

    /** Cambia el identificador del servicio. */
    public void setId(String id) {
        this.id = id != null ? id.trim() : "";
    }

    /** Devuelve el nombre del servicio. */
    public String getNombre() {
        return nombre;
    }

    /** Cambia el nombre del servicio. */
    public void setNombre(String nombre) {
        this.nombre = nombre != null ? nombre.trim() : "";
    }

    /** Devuelve la descripción del servicio. */
    public String getDescripcion() {
        return descripcion;
    }

    /** Cambia la descripción del servicio. */
    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion != null ? descripcion.trim() : "";
    }

    /** Devuelve el precio en colones costarricenses. */
    public double getPrecio() {
        return precio;
    }

    /** Cambia el precio del servicio. */
    public void setPrecio(double precio) {
        this.precio = precio;
    }

    /** Devuelve la duración en minutos. */
    public int getDuracionMinutos() {
        return duracionMinutos;
    }

    /** Cambia la duración del servicio. */
    public void setDuracionMinutos(int duracionMinutos) {
        this.duracionMinutos = duracionMinutos;
    }

    /** Dos servicios son iguales cuando tienen el mismo identificador. */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TipoServicio that = (TipoServicio) o;
        return Objects.equals(id, that.id);
    }

    /** Calcula el código hash a partir del identificador. */
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    /** Devuelve el texto que se usa en las tablas y en los correos. */
    @Override
    public String toString() {
        return nombre + " - ₡" + String.format("%.2f", precio) + " (" + duracionMinutos + " min)";
    }
}
