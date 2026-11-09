package logica;

import java.io.Serializable;
import java.util.Objects;

/**
 * Cliente de la barbería.
 *
 * <p>El correo electrónico es la llave primaria del cliente: no puede haber dos clientes
 * con el mismo correo (sin distinguir mayúsculas). Por eso {@link #equals(Object)} y
 * {@link #hashCode()} se basan en el email y no en el nombre ni en el teléfono.</p>
 *
 * <p>La clase es {@link Serializable} porque toda la barbería se guarda en disco con
 * serialización de Java (ver {@code control.Barberia#guardar()}).</p>
 */
public class Cliente implements Serializable, Comparable<Cliente> {
    private static final long serialVersionUID = 1L;

    /** Nombre y apellidos del cliente, tal como se muestran en la interfaz. */
    private String nombreCompleto;

    /** Teléfono de contacto; la capa de control exige exactamente 8 dígitos. */
    private String telefono;

    /** Correo electrónico. Es la llave primaria: identifica al cliente en todo el sistema. */
    private String email;

    /** Constructor vacío requerido por la serialización (guardado en disco). */
    public Cliente() {
    }

    /**
     * Crea un cliente quitando los espacios sobrantes de los tres campos.
     * Los valores {@code null} se convierten en cadena vacía para evitar errores posteriores.
     *
     * @param nombreCompleto nombre y apellidos
     * @param telefono       teléfono de contacto
     * @param email          correo electrónico que identificará al cliente
     */
    public Cliente(String nombreCompleto, String telefono, String email) {
        this.nombreCompleto = nombreCompleto != null ? nombreCompleto.trim() : "";
        this.telefono = telefono != null ? telefono.trim() : "";
        this.email = email != null ? email.trim() : "";
    }

    /**
     * Retorna el identificador del cliente. En este diseño el identificador es el email.
     *
     * @return correo electrónico del cliente
     */
    public String getId() {
        return this.email;
    }

    /**
     * Devuelve el nombre completo del cliente.
     *
     * @return nombre completo
     */
    public String getNombreCompleto() {
        return nombreCompleto;
    }

    /**
     * Cambia el nombre completo del cliente.
     *
     * @param nombreCompleto nuevo nombre (se recortan los espacios)
     */
    public void setNombreCompleto(String nombreCompleto) {
        this.nombreCompleto = nombreCompleto != null ? nombreCompleto.trim() : "";
    }

    /**
     * Devuelve el teléfono de contacto.
     *
     * @return teléfono registrado
     */
    public String getTelefono() {
        return telefono;
    }

    /**
     * Cambia el teléfono de contacto.
     *
     * @param telefono nuevo teléfono (se recortan los espacios)
     */
    public void setTelefono(String telefono) {
        this.telefono = telefono != null ? telefono.trim() : "";
    }

    /**
     * Devuelve el correo electrónico, que también es el identificador del cliente.
     *
     * @return correo registrado
     */
    public String getEmail() {
        return email;
    }

    /**
     * Cambia el correo del cliente. Hay que tener en cuenta que es su llave primaria:
     * la capa de control verifica que el nuevo correo no pertenezca a otro cliente.
     *
     * @param email nuevo correo electrónico (se recortan los espacios)
     */
    public void setEmail(String email) {
        this.email = email != null ? email.trim() : "";
    }

    /**
     * Dos clientes son iguales si tienen el mismo correo, sin importar mayúsculas.
     *
     * @param o objeto a comparar
     * @return {@code true} si ambos representan al mismo cliente
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Cliente cliente = (Cliente) o;
        return Objects.equals(email != null ? email.toLowerCase() : null,
                              cliente.email != null ? cliente.email.toLowerCase() : null);
    }

    /**
     * Hash coherente con {@link #equals(Object)}: se calcula sobre el email en minúsculas.
     *
     * @return código hash del cliente
     */
    @Override
    public int hashCode() {
        return Objects.hash(email != null ? email.toLowerCase() : null);
    }

    /**
     * Ordena por nombre y, si se repite, por correo. Se usa al ordenar la lista de espera.
     *
     * @param o cliente con el que se compara
     * @return negativo, cero o positivo según el orden alfabético
     */
    @Override
    public int compareTo(Cliente o) {
        if (o == null) return 1;
        int cmp = (this.nombreCompleto != null ? this.nombreCompleto : "")
                .compareToIgnoreCase(o.nombreCompleto != null ? o.nombreCompleto : "");
        if (cmp != 0) return cmp;
        return (this.email != null ? this.email : "")
                .compareToIgnoreCase(o.email != null ? o.email : "");
    }

    /**
     * Representación legible usada en mensajes y registros.
     *
     * @return "Nombre (email - teléfono)"
     */
    @Override
    public String toString() {
        return nombreCompleto + " (" + email + " - " + telefono + ")";
    }
}
