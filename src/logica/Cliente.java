package logica;

import java.io.Serializable;
import java.util.Objects;

/**
 * Representa a un cliente de la barbería.
 */
public class Cliente implements Serializable {
    private static final long serialVersionUID = 1L;

    private String nombreCompleto;
    private String telefono;
    private String email; // Llave primaria / identificador único

    public Cliente() {
    }

    public Cliente(String nombreCompleto, String telefono, String email) {
        this.nombreCompleto = nombreCompleto != null ? nombreCompleto.trim() : "";
        this.telefono = telefono != null ? telefono.trim() : "";
        this.email = email != null ? email.trim() : "";
    }

    /**
     * Retorna el identificador del cliente (en este diseño corresponde al email).
     */
    public String getId() {
        return this.email;
    }

    public String getNombreCompleto() {
        return nombreCompleto;
    }

    public void setNombreCompleto(String nombreCompleto) {
        this.nombreCompleto = nombreCompleto != null ? nombreCompleto.trim() : "";
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono != null ? telefono.trim() : "";
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email != null ? email.trim() : "";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Cliente cliente = (Cliente) o;
        return Objects.equals(email != null ? email.toLowerCase() : null,
                              cliente.email != null ? cliente.email.toLowerCase() : null);
    }

    @Override
    public int hashCode() {
        return Objects.hash(email != null ? email.toLowerCase() : null);
    }

    @Override
    public String toString() {
        return nombreCompleto + " (" + email + " - " + telefono + ")";
    }
}
