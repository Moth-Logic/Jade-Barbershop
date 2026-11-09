package logica;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;

/** Cita agendada en la barbería. */
public class Cita implements Serializable {
    private static final long serialVersionUID = 1L;

    /** Identificador único de la cita. */
    private String id;

    /** Día de la cita. */
    private LocalDate fecha;

    /** Hora de inicio en formato de veinticuatro horas. */
    private int hora;

    /** Indica si el cliente ya confirmó la cita. */
    private boolean confirmada;

    /** Cliente que reservó la cita. */
    private Cliente cliente;

    /** Servicio contratado, que define la duración de la cita. */
    private TipoServicio tipoServicio;

    /** Constructor vacío que necesita la serialización. */
    public Cita() {
    }

    /** Crea una cita pendiente de confirmar. */
    public Cita(String id, LocalDate fecha, int hora, Cliente cliente, TipoServicio tipoServicio) {
        this(id, fecha, hora, cliente, tipoServicio, false);
    }

    /** Crea una cita con su estado de confirmación ya definido. */
    public Cita(String id, LocalDate fecha, int hora, Cliente cliente, TipoServicio tipoServicio, boolean confirmada) {
        this.id = id != null ? id.trim() : "";
        this.fecha = fecha;
        this.hora = hora;
        this.cliente = cliente;
        this.tipoServicio = tipoServicio;
        this.confirmada = confirmada;
    }

    /** Devuelve el identificador de la cita. */
    public String getId() {
        return id;
    }

    /** Cambia el identificador de la cita. */
    public void setId(String id) {
        this.id = id != null ? id.trim() : "";
    }

    /** Devuelve el día de la cita. */
    public LocalDate getFecha() {
        return fecha;
    }

    /** Cambia el día de la cita. */
    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    /** Devuelve la hora de inicio. */
    public int getHora() {
        return hora;
    }

    /** Cambia la hora de inicio. */
    public void setHora(int hora) {
        this.hora = hora;
    }

    /** Indica si la cita ya fue confirmada. */
    public boolean isConfirmada() {
        return confirmada;
    }

    /** Cambia el estado de confirmación de la cita. */
    public void setConfirmada(boolean confirmada) {
        this.confirmada = confirmada;
    }

    /** Devuelve el cliente que reservó la cita. */
    public Cliente getCliente() {
        return cliente;
    }

    /** Asigna el cliente de la cita. */
    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
    }

    /** Devuelve el servicio contratado. */
    public TipoServicio getTipoServicio() {
        return tipoServicio;
    }

    /** Asigna el servicio contratado. */
    public void setTipoServicio(TipoServicio tipoServicio) {
        this.tipoServicio = tipoServicio;
    }

    /** Marca la cita como confirmada. */
    public void confirmar() {
        this.confirmada = true;
    }

    /** Dos citas son iguales cuando tienen el mismo identificador. */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Cita cita = (Cita) o;
        return Objects.equals(id, cita.id);
    }

    /** Calcula el código hash a partir del identificador. */
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    /** Devuelve el texto que se muestra al encontrar una cita. */
    @Override
    public String toString() {
        String cliStr = cliente != null ? cliente.getNombreCompleto() : "Sin cliente";
        String srvStr = tipoServicio != null ? tipoServicio.getNombre() : "Sin servicio";
        String estado = confirmada ? "Confirmada" : "Pendiente";
        return "[" + id + "] " + fecha + " a las " + hora + ":00 - " + cliStr + " (" + srvStr + ") [" + estado + "]";
    }
}
