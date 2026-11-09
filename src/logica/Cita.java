package logica;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;

/**
 * Cita agendada en la barbería.
 *
 * <p>La cita solo guarda la hora de inicio; cuánto dura se toma del
 * {@link TipoServicio} asociado, dato que usa {@code control.Barberia} para detectar
 * citas que se solapan en el tiempo.</p>
 *
 * <p>El ID lo asigna la capa de control con un consecutivo propio y nunca se reutiliza.</p>
 */
public class Cita implements Serializable {
    private static final long serialVersionUID = 1L;

    /** Identificador único de la cita (por ejemplo "0", "1", "2"). */
    private String id;

    /** Día de la cita. */
    private LocalDate fecha;

    /** Hora de inicio en formato 24 h, como entero (9 = 9:00, 14 = 14:00). */
    private int hora;

    /** Indica si el cliente ya confirmó la cita (por defecto queda pendiente). */
    private boolean confirmada;

    /** Cliente que reservó la cita. */
    private Cliente cliente;

    /** Servicio contratado; de aquí sale la duración real de la cita. */
    private TipoServicio tipoServicio;

    /** Constructor vacío requerido por la serialización (guardado en disco). */
    public Cita() {
    }

    /**
     * Crea una cita pendiente de confirmar.
     *
     * @param id           identificador asignado por la capa de control
     * @param fecha        día de la cita
     * @param hora         hora de inicio (0 a 23)
     * @param cliente      cliente que reserva
     * @param tipoServicio servicio contratado
     */
    public Cita(String id, LocalDate fecha, int hora, Cliente cliente, TipoServicio tipoServicio) {
        this(id, fecha, hora, cliente, tipoServicio, false);
    }

    /**
     * Crea una cita indicando también su estado de confirmación.
     * Se usa al recuperar datos guardados o al construir ejemplos de prueba.
     *
     * @param id           identificador asignado por la capa de control
     * @param fecha        día de la cita
     * @param hora         hora de inicio (0 a 23)
     * @param cliente      cliente que reserva
     * @param tipoServicio servicio contratado
     * @param confirmada   {@code true} si la cita ya está confirmada
     */
    public Cita(String id, LocalDate fecha, int hora, Cliente cliente, TipoServicio tipoServicio, boolean confirmada) {
        this.id = id != null ? id.trim() : "";
        this.fecha = fecha;
        this.hora = hora;
        this.cliente = cliente;
        this.tipoServicio = tipoServicio;
        this.confirmada = confirmada;
    }

    /**
     * Devuelve el identificador de la cita.
     *
     * @return ID asignado por la capa de control
     */
    public String getId() {
        return id;
    }

    /**
     * Cambia el identificador de la cita.
     *
     * @param id nuevo identificador (se recortan los espacios)
     */
    public void setId(String id) {
        this.id = id != null ? id.trim() : "";
    }

    /**
     * Devuelve el día de la cita.
     *
     * @return fecha agendada
     */
    public LocalDate getFecha() {
        return fecha;
    }

    /**
     * Cambia el día de la cita.
     *
     * @param fecha nueva fecha
     */
    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    /**
     * Devuelve la hora de inicio.
     *
     * @return hora como entero de 0 a 23
     */
    public int getHora() {
        return hora;
    }

    /**
     * Cambia la hora de inicio.
     *
     * @param hora nueva hora (0 a 23)
     */
    public void setHora(int hora) {
        this.hora = hora;
    }

    /**
     * Indica si la cita ya fue confirmada.
     *
     * @return {@code true} si el cliente confirmó
     */
    public boolean isConfirmada() {
        return confirmada;
    }

    /**
     * Cambia el estado de confirmación de la cita.
     *
     * @param confirmada nuevo estado
     */
    public void setConfirmada(boolean confirmada) {
        this.confirmada = confirmada;
    }

    /**
     * Devuelve el cliente que reservó la cita.
     *
     * @return cliente asociado
     */
    public Cliente getCliente() {
        return cliente;
    }

    /**
     * Asigna el cliente de la cita.
     *
     * @param cliente cliente que reserva
     */
    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
    }

    /**
     * Devuelve el servicio contratado.
     *
     * @return servicio que define la duración
     */
    public TipoServicio getTipoServicio() {
        return tipoServicio;
    }

    /**
     * Asigna el servicio contratado.
     *
     * @param tipoServicio servicio de la cita
     */
    public void setTipoServicio(TipoServicio tipoServicio) {
        this.tipoServicio = tipoServicio;
    }

    /**
     * Marca la cita como confirmada. La capa de control llama a este método después de
     * enviar el correo de confirmación (o al pulsar "Confirmar Cita").
     */
    public void confirmar() {
        this.confirmada = true;
    }

    /**
     * Dos citas son la misma cuando comparten ID (el ID lo asigna Barberia).
     *
     * @param o objeto a comparar
     * @return {@code true} si los IDs coinciden
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Cita cita = (Cita) o;
        return Objects.equals(id, cita.id);
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
     * Texto usado en el diálogo "Cita encontrada" y en los registros.
     *
     * @return "[id] fecha a las hora:00 - cliente (servicio) [estado]"
     */
    @Override
    public String toString() {
        String cliStr = cliente != null ? cliente.getNombreCompleto() : "Sin cliente";
        String srvStr = tipoServicio != null ? tipoServicio.getNombre() : "Sin servicio";
        String estado = confirmada ? "Confirmada" : "Pendiente";
        return "[" + id + "] " + fecha + " a las " + hora + ":00 - " + cliStr + " (" + srvStr + ") [" + estado + "]";
    }
}
