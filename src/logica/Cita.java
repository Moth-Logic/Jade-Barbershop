package logica;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;

/**
 * Representa una cita agendada en la barbería.
 */
public class Cita implements Serializable {
    private static final long serialVersionUID = 1L;

    private String id; // Identificador único de la cita
    private LocalDate fecha;
    private int hora; // Hora en formato 24h (ej. 9 para 9:00, 14 para 14:00)
    private boolean confirmada;
    private Cliente cliente;
    private TipoServicio tipoServicio;

    public Cita() {
    }

    public Cita(String id, LocalDate fecha, int hora, Cliente cliente, TipoServicio tipoServicio) {
        this(id, fecha, hora, cliente, tipoServicio, false);
    }

    public Cita(String id, LocalDate fecha, int hora, Cliente cliente, TipoServicio tipoServicio, boolean confirmada) {
        this.id = id != null ? id.trim() : "";
        this.fecha = fecha;
        this.hora = hora;
        this.cliente = cliente;
        this.tipoServicio = tipoServicio;
        this.confirmada = confirmada;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id != null ? id.trim() : "";
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public int getHora() {
        return hora;
    }

    public void setHora(int hora) {
        this.hora = hora;
    }

    public boolean isConfirmada() {
        return confirmada;
    }

    public void setConfirmada(boolean confirmada) {
        this.confirmada = confirmada;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
    }

    public TipoServicio getTipoServicio() {
        return tipoServicio;
    }

    public void setTipoServicio(TipoServicio tipoServicio) {
        this.tipoServicio = tipoServicio;
    }

    /**
     * Marca la cita como confirmada.
     */
    public void confirmar() {
        this.confirmada = true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Cita cita = (Cita) o;
        return Objects.equals(id, cita.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        String cliStr = cliente != null ? cliente.getNombreCompleto() : "Sin cliente";
        String srvStr = tipoServicio != null ? tipoServicio.getNombre() : "Sin servicio";
        String estado = confirmada ? "Confirmada" : "Pendiente";
        return "[" + id + "] " + fecha + " a las " + hora + ":00 - " + cliStr + " (" + srvStr + ") [" + estado + "]";
    }
}
