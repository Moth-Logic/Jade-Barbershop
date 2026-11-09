package logica;

import java.io.Serializable;
import java.util.Objects;

/**
 * Servicio o corte ofrecido por la barbería.
 *
 * <p>La duración en minutos no es solo informativa: {@code control.Barberia} la usa para
 * calcular cuánto tiempo ocupa una cita y así evitar solapamientos con otras citas del
 * mismo día.</p>
 *
 * <p>El ID lo asigna la capa de control con un consecutivo propio.</p>
 */
public class TipoServicio implements Serializable {
    private static final long serialVersionUID = 1L;

    /** Identificador único del servicio (por ejemplo "0", "1", "2"). */
    private String id;

    /** Nombre corto del servicio (por ejemplo "Corte clásico"). */
    private String nombre;

    /** Descripción para el catálogo. */
    private String descripcion;

    /** Precio en colones costarricenses. */
    private double precio;

    /** Tiempo que ocupa el servicio; se usa al agendar citas y detectar solapamientos. */
    private int duracionMinutos;

    /** Constructor vacío requerido por la serialización (guardado en disco). */
    public TipoServicio() {
    }

    /**
     * Crea un servicio recortando los espacios de los campos de texto.
     *
     * @param id              identificador asignado por la capa de control
     * @param nombre          nombre del servicio
     * @param descripcion     descripción para el catálogo
     * @param precio          precio en colones (debe ser mayor que 0)
     * @param duracionMinutos duración en minutos (debe ser mayor que 0)
     */
    public TipoServicio(String id, String nombre, String descripcion, double precio, int duracionMinutos) {
        this.id = id != null ? id.trim() : "";
        this.nombre = nombre != null ? nombre.trim() : "";
        this.descripcion = descripcion != null ? descripcion.trim() : "";
        this.precio = precio;
        this.duracionMinutos = duracionMinutos;
    }

    /**
     * Devuelve el identificador del servicio.
     *
     * @return ID asignado por la capa de control
     */
    public String getId() {
        return id;
    }

    /**
     * Cambia el identificador del servicio.
     *
     * @param id nuevo identificador (se recortan los espacios)
     */
    public void setId(String id) {
        this.id = id != null ? id.trim() : "";
    }

    /**
     * Devuelve el nombre del servicio.
     *
     * @return nombre del catálogo
     */
    public String getNombre() {
        return nombre;
    }

    /**
     * Cambia el nombre del servicio.
     *
     * @param nombre nuevo nombre (se recortan los espacios)
     */
    public void setNombre(String nombre) {
        this.nombre = nombre != null ? nombre.trim() : "";
    }

    /**
     * Devuelve la descripción del servicio.
     *
     * @return descripción para el catálogo
     */
    public String getDescripcion() {
        return descripcion;
    }

    /**
     * Cambia la descripción del servicio.
     *
     * @param descripcion nueva descripción (se recortan los espacios)
     */
    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion != null ? descripcion.trim() : "";
    }

    /**
     * Devuelve el precio en colones costarricenses.
     *
     * @return precio del servicio
     */
    public double getPrecio() {
        return precio;
    }

    /**
     * Cambia el precio del servicio.
     *
     * @param precio nuevo precio en colones (mayor que 0)
     */
    public void setPrecio(double precio) {
        this.precio = precio;
    }

    /**
     * Devuelve la duración en minutos.
     *
     * @return duración usada para calcular solapamientos
     */
    public int getDuracionMinutos() {
        return duracionMinutos;
    }

    /**
     * Cambia la duración del servicio.
     *
     * @param duracionMinutos nueva duración (mayor que 0)
     */
    public void setDuracionMinutos(int duracionMinutos) {
        this.duracionMinutos = duracionMinutos;
    }

    /**
     * Dos servicios son iguales cuando comparten ID.
     *
     * @param o objeto a comparar
     * @return {@code true} si los IDs coinciden
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TipoServicio that = (TipoServicio) o;
        return Objects.equals(id, that.id);
    }

    /**
     * Hash coherente con {@link #equals(Object)}.
     *
     * @return código hash basado en el ID
     */
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    /**
     * Texto usado en las tablas y en los correos.
     *
     * @return "Nombre - ₡precio (duración min)"
     */
    @Override
    public String toString() {
        return nombre + " - ₡" + String.format("%.2f", precio) + " (" + duracionMinutos + " min)";
    }
}
