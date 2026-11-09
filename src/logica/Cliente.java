package logica;

import java.io.Serializable;
import java.util.Objects;

/** Cliente de la barbería. */
public class Cliente implements Serializable, Comparable<Cliente> {
    private static final long serialVersionUID = 1L;

    /** Nombre y apellidos del cliente. */
    private String nombreCompleto;

    /** Teléfono de contacto de ocho dígitos. */
    private String telefono;

    /** Correo electrónico, que identifica al cliente en todo el sistema. */
    private String email;

    /** Constructor vacío que necesita la serialización. */
    public Cliente() {
    }

    /** Crea un cliente quitando los espacios sobrantes de los tres campos. */
    public Cliente(String nombreCompleto, String telefono, String email) {
        this.nombreCompleto = nombreCompleto != null ? nombreCompleto.trim() : "";
        this.telefono = telefono != null ? telefono.trim() : "";
        this.email = email != null ? email.trim() : "";
    }

    /** Devuelve el correo, que es el identificador del cliente. */
    public String getId() {
        return this.email;
    }

    /** Devuelve el nombre completo del cliente. */
    public String getNombreCompleto() {
        return nombreCompleto;
    }

    /** Cambia el nombre completo del cliente. */
    public void setNombreCompleto(String nombreCompleto) {
        this.nombreCompleto = nombreCompleto != null ? nombreCompleto.trim() : "";
    }

    /** Devuelve el teléfono de contacto. */
    public String getTelefono() {
        return telefono;
    }

    /** Cambia el teléfono de contacto. */
    public void setTelefono(String telefono) {
        this.telefono = telefono != null ? telefono.trim() : "";
    }

    /** Devuelve el correo del cliente. */
    public String getEmail() {
        return email;
    }

    /** Cambia el correo del cliente, que es su llave primaria. */
    public void setEmail(String email) {
        this.email = email != null ? email.trim() : "";
    }

    /** Dos clientes son iguales si tienen el mismo correo, sin importar las mayúsculas. */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Cliente cliente = (Cliente) o;
        return Objects.equals(email != null ? email.toLowerCase() : null,
                              cliente.email != null ? cliente.email.toLowerCase() : null);
    }

    /** Calcula el código hash con el correo en minúsculas. */
    @Override
    public int hashCode() {
        return Objects.hash(email != null ? email.toLowerCase() : null);
    }

    /** Ordena los clientes por nombre y, si se repite, por correo. */
    @Override
    public int compareTo(Cliente o) {
        if (o == null) return 1;
        int cmp = (this.nombreCompleto != null ? this.nombreCompleto : "")
                .compareToIgnoreCase(o.nombreCompleto != null ? o.nombreCompleto : "");
        if (cmp != 0) return cmp;
        return (this.email != null ? this.email : "")
                .compareToIgnoreCase(o.email != null ? o.email : "");
    }

    /** Devuelve el texto que se usa en los mensajes y en los registros. */
    @Override
    public String toString() {
        return nombreCompleto + " (" + email + " - " + telefono + ")";
    }
}
