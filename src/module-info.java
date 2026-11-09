/**
 * Módulo de la aplicación de escritorio Jade Barbershop.
 *
 * <p>Organiza el sistema en tres capas:</p>
 * <ul>
 *   <li>{@code logica}: modelo de dominio (cliente, cita, servicio y horario).</li>
 *   <li>{@code control}: reglas de negocio, persistencia y envío de correos.</li>
 *   <li>{@code interfaz}: ventana Swing que el usuario utiliza.</li>
 * </ul>
 *
 * <p>El único módulo del JDK que se necesita es {@code java.desktop}, que aporta Swing.
 * Las conexiones de red y el cifrado TLS usan clases de {@code java.base}, por lo que no
 * hace falta declarar más dependencias.</p>
 */
module jadebarbershop {
    requires java.desktop;

    exports logica;
    exports control;
    exports interfaz;
}
