package control;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.Properties;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

/** Cliente SMTP básico que envía los correos del sistema. */
public final class Correo {

    /** Archivo de configuración opcional que se busca en el directorio de trabajo. */
    private static final String ARCHIVO_CONFIG = "smtp.properties";

    /** Tiempo máximo de espera (ms) para conectar y para cada respuesta del servidor. */
    private static final int TIEMPO_ESPERA_MS = 15000;

    /** SMTP exige CRLF como fin de línea, incluso si el sistema operativo es Windows. */
    private static final String FIN_DE_LINEA = "\r\n";

    /** Evita que se creen instancias de esta clase. */
    private Correo() {
    }

    /** Envía un correo de texto plano al destinatario indicado. */
    public static void enviar(String destinatario, String asunto, String cuerpo) throws Exception {
        String para = destinatario == null ? "" : destinatario.trim();
        if (!para.matches("^[\\w.+-]+@[\\w.-]+\\.[a-zA-Z]{2,}$"))
            throw new Exception("El correo del destinatario no es válido: " + para);

        if (asunto == null || asunto.trim().isEmpty())
            throw new Exception("El asunto del correo no puede estar vacío.");
        if (asunto.contains("\r") || asunto.contains("\n"))
            throw new Exception("El asunto del correo no puede contener saltos de línea.");
        if (cuerpo == null)
            cuerpo = "";

        Config config = cargarConfiguracion();
        if (config.host == null || config.host.isEmpty()) {
            throw new Exception("No hay servidor SMTP configurado. Defina la variable SMTP_HOST "
                    + "o cree un archivo smtp.properties (vea smtp.properties.ejemplo).");
        }
        if (config.remitente == null || config.remitente.isEmpty())
            throw new Exception("No hay remitente configurado. Defina SMTP_FROM o smtp.remitente.");

        enviarPorRed(config, para, asunto.trim(), cuerpo);
    }

    // Pasos del protocolo SMTP.

    /** Abre la conexión, se autentica y entrega el mensaje. */
    private static void enviarPorRed(Config config, String destinatario, String asunto, String cuerpo)
            throws Exception {

        Socket conexion = null;
        try {
            conexion = abrirConexion(config);
            conexion.setSoTimeout(TIEMPO_ESPERA_MS);

            BufferedReader entrada = lector(conexion);
            BufferedWriter salida = escritor(conexion);

            // El servidor saluda al conectarse.
            esperar(leer(entrada), "saludo inicial del servidor", 220);

            // El cliente se presenta ante el servidor.
            Respuesta saludo = comando(salida, entrada, "EHLO " + dominioLocal(config));
            if (saludo.codigo != 250) {
                saludo = comando(salida, entrada, "HELO " + dominioLocal(config));
            }
            esperar(saludo, "EHLO", 250);

            // Se cifra la conexión antes de enviar la contraseña.
            if (!config.sslInmediato && config.startTls) {
                esperar(comando(salida, entrada, "STARTTLS"), "STARTTLS", 220);

                SSLSocketFactory fabrica = (SSLSocketFactory) SSLSocketFactory.getDefault();
                SSLSocket seguro = (SSLSocket) fabrica.createSocket(
                        conexion, config.host, config.puerto, true);
                seguro.startHandshake();

                conexion = seguro; // La conexión cifrada pasa a ser la activa.
                entrada = lector(conexion);
                salida = escritor(conexion);

                // Tras cifrar hay que volver a presentarse.
                esperar(comando(salida, entrada, "EHLO " + dominioLocal(config)), "EHLO tras STARTTLS", 250);
            }

            // Se autentica el usuario con el método que acepte el servidor.
            if (config.usuario != null && !config.usuario.isEmpty()) {
                String credenciales = base64("\u0000" + config.usuario + "\u0000" + config.clave);
                Respuesta autenticacion = comando(salida, entrada, "AUTH PLAIN " + credenciales);

                if (autenticacion.codigo == 500 || autenticacion.codigo == 504) {
                    esperar(comando(salida, entrada, "AUTH LOGIN"), "AUTH LOGIN", 334);
                    esperar(comando(salida, entrada, base64(config.usuario)), "usuario SMTP", 334);
                    autenticacion = comando(salida, entrada, base64(config.clave));
                }
                esperar(autenticacion, "autenticación", 235);
            }

            // Se indica quién envía y quién recibe el mensaje.
            esperar(comando(salida, entrada, "MAIL FROM:<" + config.remitente + ">"), "MAIL FROM", 250);
            esperar(comando(salida, entrada, "RCPT TO:<" + destinatario + ">"), "RCPT TO", 250, 251);

            // Se envía el cuerpo del mensaje.
            esperar(comando(salida, entrada, "DATA"), "DATA", 354);
            salida.write(construirMensaje(config, destinatario, asunto, cuerpo));
            salida.write(FIN_DE_LINEA + "." + FIN_DE_LINEA);
            salida.flush();
            esperar(leer(entrada), "envío del mensaje", 250);

            // Se cierra la sesión, aunque el correo ya fue aceptado.
            try {
                comando(salida, entrada, "QUIT");
            } catch (IOException ignorada) {
                // El servidor puede cerrar la conexión después de aceptar el mensaje.
            }
        } finally {
            if (conexion != null) {
                try {
                    conexion.close();
                } catch (IOException ignorada) {
                    // Nada que hacer: la conexión ya no se usa.
                }
            }
        }
    }

    /** Crea el socket, cifrado desde el primer byte (puerto 465) o en texto plano (587). */
    private static Socket abrirConexion(Config config) throws IOException {
        InetSocketAddress destino = new InetSocketAddress(config.host, config.puerto);
        if (config.sslInmediato) {
            SSLSocketFactory fabrica = (SSLSocketFactory) SSLSocketFactory.getDefault();
            SSLSocket socket = (SSLSocket) fabrica.createSocket();
            socket.connect(destino, TIEMPO_ESPERA_MS);
            socket.startHandshake();
            return socket;
        }
        Socket socket = new Socket();
        socket.connect(destino, TIEMPO_ESPERA_MS);
        return socket;
    }

    private static BufferedReader lector(Socket socket) throws IOException {
        // Las respuestas del protocolo son ASCII, así que este juego de caracteres es seguro.
        return new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.ISO_8859_1));
    }

    private static BufferedWriter escritor(Socket socket) throws IOException {
        return new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.ISO_8859_1));
    }

    /** Envía una orden SMTP y devuelve la respuesta del servidor. */
    private static Respuesta comando(BufferedWriter salida, BufferedReader entrada, String orden)
            throws IOException {
        salida.write(orden + FIN_DE_LINEA);
        salida.flush();
        return leer(entrada);
    }

    /** Lee una respuesta SMTP completa, aunque ocupe varias líneas. */
    private static Respuesta leer(BufferedReader entrada) throws IOException {
        StringBuilder texto = new StringBuilder();
        int codigo;
        String linea;

        do {
            linea = entrada.readLine();
            if (linea == null)
                throw new IOException("El servidor SMTP cerró la conexión inesperadamente.");
            if (linea.length() < 3)
                throw new IOException("Respuesta SMTP inválida: " + linea);

            try {
                codigo = Integer.parseInt(linea.substring(0, 3));
            } catch (NumberFormatException e) {
                throw new IOException("Respuesta SMTP inválida: " + linea);
            }

            texto.append(linea.length() > 4 ? linea.substring(4) : "").append('\n');
        } while (linea.length() > 3 && linea.charAt(3) == '-');

        return new Respuesta(codigo, texto.toString().trim());
    }

    /** Comprueba que el código de respuesta sea el esperado y lanza el error si no lo es. */
    private static void esperar(Respuesta respuesta, String etapa, int... codigosEsperados) throws Exception {
        for (int codigo : codigosEsperados) {
            if (respuesta.codigo == codigo)
                return;
        }
        throw new Exception("El servidor SMTP rechazó " + etapa + " (" + respuesta.codigo + "): "
                + respuesta.texto);
    }

    // Construcción del mensaje.

    /** Arma las cabeceras y el cuerpo del mensaje en base64. */
    private static String construirMensaje(Config config, String destinatario, String asunto, String cuerpo) {
        String cuerpoCodificado = Base64.getMimeEncoder(76, FIN_DE_LINEA.getBytes(StandardCharsets.US_ASCII))
                .encodeToString(cuerpo.getBytes(StandardCharsets.UTF_8));

        StringBuilder mensaje = new StringBuilder();
        mensaje.append("From: ").append(remitenteConNombre(config)).append(FIN_DE_LINEA);
        mensaje.append("To: <").append(destinatario).append('>').append(FIN_DE_LINEA);
        mensaje.append("Subject: ").append(codificarAsunto(asunto)).append(FIN_DE_LINEA);
        mensaje.append("Date: ")
                .append(DateTimeFormatter.RFC_1123_DATE_TIME.format(ZonedDateTime.now()))
                .append(FIN_DE_LINEA);
        mensaje.append("MIME-Version: 1.0").append(FIN_DE_LINEA);
        mensaje.append("Content-Type: text/plain; charset=UTF-8").append(FIN_DE_LINEA);
        mensaje.append("Content-Transfer-Encoding: base64").append(FIN_DE_LINEA);
        mensaje.append(FIN_DE_LINEA);
        mensaje.append(cuerpoCodificado);
        return mensaje.toString();
    }

    /** Devuelve el remitente con su nombre y sin saltos de línea. */
    private static String remitenteConNombre(Config config) {
        String correo = limpiarCabecera(config.remitente);
        String nombre = limpiarCabecera(config.nombreRemitente);
        if (nombre.isEmpty())
            return "<" + correo + ">";
        return "\"" + nombre + "\" <" + correo + ">";
    }

    /** Codifica el asunto cuando tiene acentos o símbolos. */
    private static String codificarAsunto(String asunto) throws IllegalArgumentException {
        boolean necesitaCodificar = asunto.chars().anyMatch(c -> c > 127);
        if (!necesitaCodificar)
            return asunto;
        return "=?UTF-8?B?" + base64(asunto) + "?=";
    }

    private static String base64(String texto) {
        return Base64.getEncoder().encodeToString(texto.getBytes(StandardCharsets.UTF_8));
    }

    /** Quita los saltos de línea para evitar que se inyecten cabeceras. */
    private static String limpiarCabecera(String valor) {
        if (valor == null)
            return "";
        return valor.replace("\r", " ").replace("\n", " ").trim();
    }

    /** Devuelve el dominio con el que el cliente se presenta ante el servidor. */
    private static String dominioLocal(Config config) {
        String correo = config.remitente == null ? "" : config.remitente;
        int arroba = correo.indexOf('@');
        if (arroba >= 0 && arroba < correo.length() - 1)
            return correo.substring(arroba + 1);
        return "localhost";
    }

    // Lectura de la configuración.

    /** Datos necesarios para conectarse al servidor SMTP. */
    private static final class Config {
        String host;
        int puerto = 587;
        String usuario;
        String clave = "";
        String remitente;
        String nombreRemitente = "Jade Barbershop";
        boolean sslInmediato;   // Indica si el cifrado empieza desde el primer byte.
        boolean startTls = true; // Indica si el cifrado empieza después de conectar.
    }

    /** Respuesta del servidor con su código y su texto. */
    private static final class Respuesta {
        final int codigo;
        final String texto;

        Respuesta(int codigo, String texto) {
            this.codigo = codigo;
            this.texto = texto;
        }
    }

    /** Lee la configuración del entorno y del archivo smtp.properties. */
    private static Config cargarConfiguracion() throws Exception {
        Properties propiedades = new Properties();
        File archivo = new File(ARCHIVO_CONFIG);
        if (archivo.isFile()) {
            try (FileInputStream entrada = new FileInputStream(archivo)) {
                propiedades.load(new InputStreamReader(entrada, StandardCharsets.UTF_8));
            } catch (IOException e) {
                throw new Exception("No se pudo leer " + ARCHIVO_CONFIG + ": " + e.getMessage());
            }
        }

        Config config = new Config();
        config.host = valor("SMTP_HOST", propiedades, "smtp.host", null);
        config.puerto = entero(valor("SMTP_PORT", propiedades, "smtp.puerto", null), 587);
        config.usuario = valor("SMTP_USER", propiedades, "smtp.usuario", null);
        config.clave = valor("SMTP_PASS", propiedades, "smtp.clave", "");

        String remitente = valor("SMTP_FROM", propiedades, "smtp.remitente", null);
        config.remitente = remitente != null ? remitente : config.usuario;

        String nombre = valor("SMTP_NOMBRE", propiedades, "smtp.nombre", null);
        if (nombre != null)
            config.nombreRemitente = nombre;

        config.sslInmediato = booleano(valor("SMTP_SSL", propiedades, "smtp.ssl", "false"));
        config.startTls = booleano(valor("SMTP_STARTTLS", propiedades, "smtp.starttls", "true"));
        return config;
    }

    /** Busca un valor en el entorno, luego en el archivo y por último usa el valor por defecto. */
    private static String valor(String variableEntorno, Properties propiedades, String clave, String porDefecto) {
        String valor = System.getenv(variableEntorno);
        if (valor == null || valor.trim().isEmpty())
            valor = propiedades.getProperty(clave);
        if (valor == null || valor.trim().isEmpty())
            return porDefecto;
        return valor.trim();
    }

    private static int entero(String valor, int porDefecto) {
        if (valor == null)
            return porDefecto;
        try {
            return Integer.parseInt(valor.trim());
        } catch (NumberFormatException e) {
            return porDefecto;
        }
    }

    private static boolean booleano(String valor) {
        return valor != null && (valor.equalsIgnoreCase("true") || valor.equals("1")
                || valor.equalsIgnoreCase("si") || valor.equalsIgnoreCase("sí"));
    }
}
