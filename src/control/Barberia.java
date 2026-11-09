package control;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import logica.Cita;
import logica.Cliente;
import logica.HorarioAtencion;
import logica.TipoServicio;	/** Núcleo del sistema con las listas de datos y las reglas del negocio. */
public class Barberia implements Serializable {

	/** Versión de la clase usada por la serialización; cambiarla invalida archivos viejos. */
	private static final long serialVersionUID = 1L;

	/** Archivo donde se guarda el estado completo (serialización binaria de Java). */
	private static final String ARCHIVO_DAT = "barberia.dat";

	/** Ruta alternativa que se usa cuando existe la carpeta state. */
	private static final String ARCHIVO_STATE = "state" + File.separator + "barberia.dat";

	/** Formato de fecha usado en los correos que se envían a los clientes. */
	private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

	/** Consecutivo para generar el ID de la próxima cita. */
	private int consecutivoCita = 0;

	/** Consecutivo para generar el ID del próximo tipo de servicio. */
	private int consecutivoTipoServicio = 0;

	/** Nombre comercial de la barbería, usado en la ventana y en los correos. */
	private String nombre;

	/** Horario semanal de atención. */
	private HorarioAtencion horario;

	/** Clientes registrados; cada uno se identifica por su correo. */
	private List<Cliente> clientes;

	/** Clientes que esperan un espacio disponible, ordenados por nombre. */
	private List<Cliente> listaEspera;

	/** Citas agendadas, confirmadas o pendientes. */
	private List<Cita> citas;

	/** Catálogo de servicios con precio y duración. */
	private List<TipoServicio> tiposServicio;

	/** Último error del guardado automático, o nulo si todo salió bien. */
	private String errorGuardado;

	/** Crea una barbería vacía con su horario por defecto. */
	public Barberia(String nombre) {
		this.nombre = nombre;
		this.horario = new HorarioAtencion();
		this.clientes = new ArrayList<>();
		this.listaEspera = new ArrayList<>();
		this.citas = new ArrayList<>();
		this.tiposServicio = new ArrayList<>();
	}

	/** Comprueba que el nombre, el teléfono y el correo de un cliente sean válidos. */
	public void validarCliente(String nombre, String tel, String email) throws Exception {
		if (nombre == null || nombre.trim().isEmpty())
			throw new Exception("El nombre no puede estar vacío.");

		if (tel == null || !tel.trim().matches("^[0-9]{8}$"))
			throw new Exception("El teléfono debe contener exactamente 8 dígitos numéricos.");

		if (email == null || !email.trim().matches("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$"))
			throw new Exception("El formato del correo electrónico es inválido.");
	}

	/** Registra un cliente nuevo sin repetir el correo. */
	public void crearCliente(String nombreCompleto, String tel, String email) throws Exception {
		validarCliente(nombreCompleto, tel, email);

		String emailLimpio = email.trim();
		for (Cliente c : clientes) {
			if (c.getEmail().equalsIgnoreCase(emailLimpio)) {
				throw new Exception("Ya existe un cliente registrado con el correo: " + emailLimpio);
			}
		}

		Cliente cliente = new Cliente(nombreCompleto.trim(), tel.trim(), emailLimpio);
		this.clientes.add(cliente);
		autoguardar();
	}

	/** Modifica un cliente buscándolo por su correo actual. */
	public void modificarCliente(String email, String nombreCompleto, String tel, String emailNuevo) throws Exception {
		if (clientes.isEmpty())
			throw new Exception("No hay clientes registrados actualmente.");

		Cliente cliente = consultarCliente(email);
		validarCliente(nombreCompleto, tel, emailNuevo);

		String emailNuevoLimpio = emailNuevo.trim();
		if (!emailNuevoLimpio.equalsIgnoreCase(email)) {
			for (Cliente c : clientes) {
				if (!c.getEmail().equalsIgnoreCase(email) && c.getEmail().equalsIgnoreCase(emailNuevoLimpio)) {
					throw new Exception("El correo " + emailNuevoLimpio + " ya está registrado para otro cliente.");
				}
			}
		}

		cliente.setNombreCompleto(nombreCompleto.trim());
		cliente.setTelefono(tel.trim());
		cliente.setEmail(emailNuevoLimpio);
		autoguardar();
	}

	/** Borra un cliente solo si no tiene citas futuras ni está en la lista de espera. */
	public void borrarCliente(String email) throws Exception {
		if (clientes.isEmpty())
			throw new Exception("No hay clientes registrados actualmente.");

		if (email == null || !email.trim().matches("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$"))
			throw new Exception("Formato de correo inválido.");

		String emailLimpio = email.trim();
		for (int i = 0; i < clientes.size(); i++) {
			if (clientes.get(i).getEmail().equalsIgnoreCase(emailLimpio)) {

				for (int j = 0; j < citas.size(); j++) {
					Cita c = citas.get(j);
					if (c.getCliente() != null
						&& c.getCliente().getEmail().equalsIgnoreCase(emailLimpio)
						&& !c.getFecha().isBefore(LocalDate.now())) {
						throw new Exception("No se puede borrar el cliente porque tiene citas programadas a futuro.");
					}
				}

				for (int j = 0; j < listaEspera.size(); j++) {
					if (listaEspera.get(j).getEmail().equalsIgnoreCase(emailLimpio)) {
						throw new Exception("No se puede borrar el cliente porque se encuentra en la lista de espera.");
					}
				}

				clientes.remove(i);
				autoguardar();
				return;
			}
		}
		throw new Exception("Cliente no encontrado.");
	}

	/** Busca un cliente por su correo. */
	public Cliente consultarCliente(String email) throws Exception {
		if (email == null)
			throw new Exception("El correo no puede ser nulo.");

		String emailLimpio = email.trim();
		for (int i = 0; i < clientes.size(); i++) {
			if (clientes.get(i).getEmail().equalsIgnoreCase(emailLimpio)) {
				return clientes.get(i);
			}
		}
		throw new Exception("Cliente no encontrado con el correo: " + email);
	}

	/** Devuelve los clientes en filas listas para la tabla. */
	public ArrayList<String[]> listarClientes() throws Exception {
		ArrayList<String[]> clientela = new ArrayList<>();
		for (int i = 0; i < clientes.size(); i++) {
			String[] datos = {
				clientes.get(i).getNombreCompleto(),
				clientes.get(i).getTelefono(),
				clientes.get(i).getEmail()
			};
			clientela.add(datos);
		}
		return clientela;
	}

	/** Comprueba que la fecha, la hora, el cliente y el servicio de una cita sean válidos. */
	private void validarCita(String id, LocalDate fecha, int hora, String emailCliente, String idTipoServicio) throws Exception {
		if (id != null && !id.isEmpty() && !id.equals("0") && !id.matches("^[0-9]+$"))
			throw new Exception("ID de cita inválido.");

		if (fecha == null || fecha.isBefore(LocalDate.now()))
			throw new Exception("La fecha no puede ser nula ni anterior a hoy.");

		if (hora < 0 || hora > 23)
			throw new Exception("La hora debe estar entre 0 y 23.");

		if (horario == null) {
			horario = new HorarioAtencion();
		}

		if (!horario.estaDisponible(fecha, hora))
			throw new Exception("La barbería está cerrada o fuera de horario de atención para esa fecha y hora.");

		if (emailCliente == null || !emailCliente.trim().matches("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$"))
			throw new Exception("Correo del cliente inválido.");

		if (idTipoServicio == null || idTipoServicio.trim().isEmpty() || !idTipoServicio.trim().matches("^[0-9]+$"))
			throw new Exception("ID de servicio inválido.");
	}

	/** Comprueba que la cita nueva no se cruce con otra del mismo día. */
	private void validarSolapamiento(LocalDate fecha, int hora, TipoServicio servicio, String idExcluido)
			throws Exception {
		// Se trabaja en minutos desde la medianoche.
		int inicioNueva = hora * 60;
		int duracionNueva = servicio != null ? Math.max(1, servicio.getDuracionMinutos()) : 60;
		int finNueva = inicioNueva + duracionNueva;

		for (Cita existente : citas) {
			if (idExcluido != null && existente.getId().equals(idExcluido))
				continue;
			if (existente.getFecha() == null || !existente.getFecha().equals(fecha))
				continue;

			int inicioExistente = existente.getHora() * 60;
			int duracionExistente = existente.getTipoServicio() != null
					? Math.max(1, existente.getTipoServicio().getDuracionMinutos())
					: 60;
			int finExistente = inicioExistente + duracionExistente;

			// Dos rangos se cruzan cuando cada uno empieza antes de que termine el otro.
			if (inicioNueva < finExistente && inicioExistente < finNueva) {
				throw new Exception("La cita se cruza con otra cita el " + fecha + " de "
						+ formatearHora(inicioExistente) + " a " + formatearHora(finExistente) + " ("
						+ (existente.getCliente() != null ? existente.getCliente().getNombreCompleto()
								: "sin cliente")
						+ ").");
			}
		}
	}

	/** Convierte minutos desde la medianoche a HH:mm para los mensajes de error. */
	private static String formatearHora(int minutos) {
		return String.format("%02d:%02d", (minutos / 60) % 24, minutos % 60);
	}

	/** Agenda una cita nueva y guarda el cambio en disco. */
	public void crearCita(LocalDate fecha, int hora, String emailCliente, String idTipoServicio) throws Exception {
		validarCita("0", fecha, hora, emailCliente, idTipoServicio);

		// El servicio define cuánto dura la cita, así que debe existir antes de revisar choques.
		Cliente cliente = consultarCliente(emailCliente);
		TipoServicio tipoServicio = consultarTipoServicio(idTipoServicio);

		validarSolapamiento(fecha, hora, tipoServicio, null);

		// El identificador sale de un consecutivo propio y no se reutiliza.
		String id = String.valueOf(consecutivoCita++);
		Cita cita = new Cita(id, fecha, hora, cliente, tipoServicio);
		this.citas.add(cita);
		autoguardar();
	}

	/** Cambia los datos de una cita existente y vuelve a revisar el solapamiento. */
	public void modificarCita(String id, LocalDate fecha, int hora, String emailCliente, String idTipoServicio) throws Exception {
		if (citas.isEmpty())
			throw new Exception("No hay citas registradas actualmente.");

		validarCita(id, fecha, hora, emailCliente, idTipoServicio);

		Cita cita = consultarCita(id);
		Cliente cliente = consultarCliente(emailCliente);
		TipoServicio tipoServicio = consultarTipoServicio(idTipoServicio);

		validarSolapamiento(fecha, hora, tipoServicio, id);

		cita.setFecha(fecha);
		cita.setHora(hora);
		cita.setCliente(cliente);
		cita.setTipoServicio(tipoServicio);
		autoguardar();
	}

	/** Cancela y elimina una cita. */
	public void borrarCita(String id) throws Exception {
		if (citas.isEmpty())
			throw new Exception("No hay citas registradas actualmente.");

		if (id == null || id.trim().isEmpty() || !id.trim().matches("^[0-9]+$"))
			throw new Exception("ID de cita inválido.");

		for (int i = 0; i < citas.size(); i++) {
			if (citas.get(i).getId().equals(id.trim())) {
				citas.remove(i);
				autoguardar();
				return;
			}
		}
		throw new Exception("Cita no encontrada.");
	}

	/** Busca una cita por su identificador. */
	public Cita consultarCita(String id) throws Exception {
		if (id == null)
			throw new Exception("ID no puede ser nulo.");

		String idLimpio = id.trim();
		for (int i = 0; i < citas.size(); i++) {
			if (citas.get(i).getId().equals(idLimpio)) {
				return citas.get(i);
			}
		}
		throw new Exception("Cita no encontrada con ID: " + id);
	}

	/** Marca una cita como confirmada sin enviar correo. */
	public void confirmarCita(String id) throws Exception {
		if (citas.isEmpty())
			throw new Exception("No hay citas registradas actualmente.");

		Cita cita = consultarCita(id);
		cita.confirmar();
		autoguardar();
	}

	/** Devuelve todas las citas en filas listas para la tabla. */
	public ArrayList<String[]> listarCitas() throws Exception {
		ArrayList<String[]> citaciones = new ArrayList<>();
		for (int i = 0; i < citas.size(); i++) {
			Cita c = citas.get(i);
			String[] datos = {
				c.getId(),
				c.getFecha().toString(),
				String.format("%02d:00", c.getHora()),
				c.getCliente() != null ? c.getCliente().getNombreCompleto() + " (" + c.getCliente().getEmail() + ")" : "Sin cliente",
				c.getTipoServicio() != null ? c.getTipoServicio().getNombre() : "Sin servicio",
				c.isConfirmada() ? "Confirmada" : "Pendiente"
			};
			citaciones.add(datos);
		}
		return citaciones;
	}

	/** Alias de listarCitas que se mantiene por compatibilidad. */
	public ArrayList<String[]> ListarCitas() throws Exception {
		return listarCitas();
	}

	/** Devuelve las citas de un día concreto en filas listas para la tabla. */
	public ArrayList<String[]> getCitasPorFecha(LocalDate fecha) throws Exception {
		ArrayList<String[]> citaciones = new ArrayList<>();
		if (fecha == null) return citaciones;

		for (int i = 0; i < citas.size(); i++) {
			Cita c = citas.get(i);
			if (c.getFecha().equals(fecha)) {
				String[] datos = {
					c.getId(),
					c.getFecha().toString(),
					String.format("%02d:00", c.getHora()),
					c.getCliente() != null ? c.getCliente().getNombreCompleto() : "Sin cliente",
					c.getTipoServicio() != null ? c.getTipoServicio().getNombre() : "Sin servicio",
					c.isConfirmada() ? "Confirmada" : "Pendiente"
				};
				citaciones.add(datos);
			}
		}
		return citaciones;
	}

	/** Devuelve las citas de los siete días que empiezan en la fecha indicada. */
	public ArrayList<String[]> getCitasSemana(LocalDate fechaInicio) throws Exception {
		ArrayList<String[]> citaciones = new ArrayList<>();
		if (fechaInicio == null) return citaciones;

		for (LocalDate fecha = fechaInicio;
			 fecha.isBefore(fechaInicio.plusDays(7));
			 fecha = fecha.plusDays(1)) {

			ArrayList<String[]> citasEnFecha = getCitasPorFecha(fecha);
			citaciones.addAll(citasEnFecha);
		}
		return citaciones;
	}

	/** Devuelve las citas del mes que empieza en la fecha indicada. */
	public ArrayList<String[]> getCitasMes(LocalDate fechaInicio) throws Exception {
		ArrayList<String[]> citaciones = new ArrayList<>();
		if (fechaInicio == null) return citaciones;

		for (LocalDate fecha = fechaInicio;
			 fecha.isBefore(fechaInicio.plusMonths(1));
			 fecha = fecha.plusDays(1)) {

			ArrayList<String[]> citasEnFecha = getCitasPorFecha(fecha);
			citaciones.addAll(citasEnFecha);
		}
		return citaciones;
	}

	/** Comprueba que los datos de un servicio sean válidos. */
	private void validarTipoServicio(String id, String nombre, String descripcion, double precio, int duracionMinutos) throws Exception {
		if (id != null && !id.isEmpty() && !id.equals("0") && !id.matches("^[0-9]+$"))
			throw new Exception("ID de servicio inválido.");

		if (nombre == null || nombre.trim().isEmpty())
			throw new Exception("El nombre del servicio no puede estar vacío.");

		if (descripcion == null || descripcion.trim().isEmpty())
			throw new Exception("La descripción no puede estar vacía.");

		if (precio <= 0)
			throw new Exception("El precio debe ser mayor a 0.");

		if (duracionMinutos <= 0)
			throw new Exception("La duración debe ser mayor a 0 minutos.");
	}

	/** Agrega un servicio al catálogo y guarda el cambio en disco. */
	public void crearTipoServicio(String nombre, String descripcion, double precio, int duracionMinutos) throws Exception {
		validarTipoServicio("0", nombre, descripcion, precio, duracionMinutos);

		String id = String.valueOf(consecutivoTipoServicio++);
		TipoServicio servicio = new TipoServicio(id, nombre.trim(), descripcion.trim(), precio, duracionMinutos);
		this.tiposServicio.add(servicio);
		autoguardar();
	}

	/** Actualiza un servicio existente sin cambiar su identificador. */
	public void modificarTipoServicio(String id, String nombre, String descripcion, double precio, int duracionMinutos) throws Exception {
		if (tiposServicio.isEmpty())
			throw new Exception("No hay servicios registrados actualmente.");

		validarTipoServicio(id, nombre, descripcion, precio, duracionMinutos);

		TipoServicio servicio = consultarTipoServicio(id);
		servicio.setNombre(nombre.trim());
		servicio.setDescripcion(descripcion.trim());
		servicio.setPrecio(precio);
		servicio.setDuracionMinutos(duracionMinutos);
		autoguardar();
	}

	/** Elimina un servicio solo si ninguna cita lo está usando. */
	public void borrarTipoServicio(String id) throws Exception {
		if (tiposServicio.isEmpty())
			throw new Exception("No hay servicios registrados actualmente.");

		if (id == null || id.trim().isEmpty() || !id.trim().matches("^[0-9]+$"))
			throw new Exception("ID de servicio inválido.");

		String idLimpio = id.trim();

		for (int i = 0; i < citas.size(); i++) {
			Cita c = citas.get(i);
			if (c.getTipoServicio() != null && c.getTipoServicio().getId().equals(idLimpio)) {
				throw new Exception("No se puede eliminar el tipo de servicio porque esta siendo utilizado en una cita programada.");
			}
		}

		for (int i = 0; i < tiposServicio.size(); i++) {
			if (tiposServicio.get(i).getId().equals(idLimpio)) {
				tiposServicio.remove(i);
				autoguardar();
				return;
			}
		}
		throw new Exception("Servicio no encontrado.");
	}

	/** Busca un servicio por su identificador. */
	public TipoServicio consultarTipoServicio(String id) throws Exception {
		if (id == null)
			throw new Exception("ID no puede ser nulo.");

		String idLimpio = id.trim();
		for (int i = 0; i < tiposServicio.size(); i++) {
			if (tiposServicio.get(i).getId().equals(idLimpio)) {
				return tiposServicio.get(i);
			}
		}
		throw new Exception("Servicio no encontrado con ID: " + id);
	}

	/** Devuelve el catálogo de servicios en filas listas para la tabla. */
	public ArrayList<String[]> listarTiposServicio() throws Exception {
		ArrayList<String[]> servicios = new ArrayList<>();
		for (int i = 0; i < tiposServicio.size(); i++) {
			TipoServicio s = tiposServicio.get(i);
			String[] datos = {
				s.getId(),
				s.getNombre(),
				s.getDescripcion(),
				String.valueOf(s.getPrecio()),
				String.valueOf(s.getDuracionMinutos())
			};
			servicios.add(datos);
		}
		return servicios;
	}

	/** Agrega un cliente a la lista de espera sin repetirlo. */
	public void agregarAListaEspera(String emailCliente) throws Exception {
		Cliente cliente = consultarCliente(emailCliente);

		for (Cliente c : listaEspera) {
			if (c.getEmail().equalsIgnoreCase(emailCliente.trim())) {
				throw new Exception("El cliente ya está en la lista de espera.");
			}
		}

		this.listaEspera.add(cliente);
		// Se ordena por nombre para que la lista sea fácil de leer.
		this.listaEspera.sort(null);
		autoguardar();
	}

	/** Saca a un cliente de la lista de espera y guarda el cambio. */
	public void eliminarDeListaEspera(String emailCliente) throws Exception {
		if (emailCliente == null)
			throw new Exception("Correo no puede ser nulo.");

		String emailLimpio = emailCliente.trim();
		for (int i = 0; i < listaEspera.size(); i++) {
			if (listaEspera.get(i).getEmail().equalsIgnoreCase(emailLimpio)) {
				listaEspera.remove(i);
				autoguardar();
				return;
			}
		}
		throw new Exception("El cliente no se encuentra en la lista de espera.");
	}

	private void eliminarDeListaEsperaSilencioso(String emailCliente) {
		for (int i = 0; i < listaEspera.size(); i++) {
			if (listaEspera.get(i).getEmail().equalsIgnoreCase(emailCliente)) {
				listaEspera.remove(i);
				return;
			}
		}
	}

	/** Devuelve la lista de espera numerada en filas listas para la tabla. */
	public ArrayList<String[]> mostrarListaEspera() throws Exception {
		ArrayList<String[]> enEspera = new ArrayList<>();
		for (int i = 0; i < listaEspera.size(); i++) {
			Cliente c = listaEspera.get(i);
			String[] datos = {
				String.valueOf(i + 1), // Número de turno.
				c.getNombreCompleto(),
				c.getTelefono(),
				c.getEmail()
			};
			enEspera.add(datos);
		}
		return enEspera;
	}

	/** Cambia el horario de un día y lo guarda en disco. */
	public void setHorario(int dia, boolean abierto, int inicio, int fin) throws Exception {
		if (horario == null) {
			horario = new HorarioAtencion();
		}
		// HorarioAtencion valida las horas y el rango antes de guardar.
		horario.setDia(dia, abierto, inicio, fin);
		autoguardar();
	}

	/** Devuelve el horario de atención y lo crea por defecto si no existe. */
	public HorarioAtencion getHorario() {
		if (horario == null) {
			horario = new HorarioAtencion();
		}
		return horario;
	}

	/** Envía el correo de confirmación y marca la cita como confirmada solo si el envío funciona. */
	public String enviarConfirmacion(String idCita) throws Exception {
		Cita cita = consultarCita(idCita);
		Cliente cliente = clienteConCorreo(cita);

		Correo.enviar(cliente.getEmail(), "Confirmación de cita - " + nombre,
				construirCuerpoCorreo(cita, true));

		cita.confirmar();
		autoguardar();
		return "Confirmación enviada exitosamente a: " + cliente.getEmail();
	}

	/** Envía el correo de recordatorio sin cambiar el estado de la cita. */
	public String enviarRecordatorio(String idCita) throws Exception {
		Cita cita = consultarCita(idCita);
		Cliente cliente = clienteConCorreo(cita);

		Correo.enviar(cliente.getEmail(), "Recordatorio de cita - " + nombre,
				construirCuerpoCorreo(cita, false));

		return "Recordatorio enviado exitosamente a: " + cliente.getEmail();
	}

	/** Devuelve el cliente de la cita y comprueba que tenga correo. */
	private Cliente clienteConCorreo(Cita cita) throws Exception {
		Cliente cliente = cita.getCliente();
		if (cliente == null || cliente.getEmail() == null || cliente.getEmail().trim().isEmpty())
			throw new Exception("La cita no tiene un cliente con correo electrónico registrado.");
		return cliente;
	}

	/** Redacta el texto del correo con los datos de la cita. */
	private String construirCuerpoCorreo(Cita cita, boolean esConfirmacion) {
		Cliente cliente = cita.getCliente();
		TipoServicio servicio = cita.getTipoServicio();

		StringBuilder cuerpo = new StringBuilder();
		cuerpo.append("Hola ").append(cliente.getNombreCompleto()).append(":\n\n");
		if (esConfirmacion) {
			cuerpo.append("Su cita en ").append(nombre).append(" ha quedado confirmada.\n\n");
		} else {
			cuerpo.append("Le recordamos su próxima cita en ").append(nombre).append(".\n\n");
		}

		cuerpo.append("Fecha: ").append(cita.getFecha().format(FORMATO_FECHA)).append('\n');
		cuerpo.append("Hora: ").append(String.format("%02d:00", cita.getHora())).append('\n');
		if (servicio != null) {
			cuerpo.append("Servicio: ").append(servicio.getNombre())
					.append(" (").append(servicio.getDuracionMinutos()).append(" min)\n");
			cuerpo.append("Precio: ₡").append(String.format("%.2f", servicio.getPrecio())).append('\n');
		}
		cuerpo.append("\nSi necesita reprogramar o cancelar, responda a este correo.\n\n");
		cuerpo.append(nombre).append('\n');
		return cuerpo.toString();
	}

	// Guardado automático.

	/** Guarda el estado después de cada cambio y conserva el error si falla. */
	private void autoguardar() {
		try {
			guardar();
			errorGuardado = null;
		} catch (Exception e) {
			errorGuardado = e.getMessage() != null ? e.getMessage() : e.toString();
			System.err.println("[Barberia] No se pudo guardar automáticamente: " + e);
		}
	}

	/** Devuelve el último error del guardado automático, o nulo si todo salió bien. */
	public String getErrorGuardado() {
		return errorGuardado;
	}

	/** Limpia el error para no volver a mostrarlo en el siguiente mensaje. */
	public void limpiarErrorGuardado() {
		errorGuardado = null;
	}

	/** Elige entre la carpeta state y la raíz del proyecto para guardar los datos. */
	private File obtenerArchivoGuardado() {
		File dirState = new File("state");
		if (dirState.exists() && dirState.isDirectory()) {
			return new File(ARCHIVO_STATE);
		}
		return new File(ARCHIVO_DAT);
	}

	/** Escribe toda la barbería en disco. */
	public void guardar() throws Exception {
		File archivo = obtenerArchivoGuardado();
		try (ObjectOutputStream salida = new ObjectOutputStream(new FileOutputStream(archivo))) {
			salida.writeObject(this);
		}
	}

	/** Recupera el estado guardado y deja la barbería vacía si no hay archivo. */
	public void cargar() throws Exception {
		File archivoState = new File(ARCHIVO_STATE);
		File archivoRaiz = new File(ARCHIVO_DAT);
		File archivoACargar = archivoState.exists() ? archivoState : (archivoRaiz.exists() ? archivoRaiz : null);

		if (archivoACargar == null) {
			return; // No hay archivo previo.
		}

		try (ObjectInputStream entrada = new ObjectInputStream(new FileInputStream(archivoACargar))) {
			Barberia barberia = (Barberia) entrada.readObject();

			this.nombre = barberia.nombre;
			this.horario = barberia.horario != null ? barberia.horario : new HorarioAtencion();
			this.clientes = barberia.clientes != null ? barberia.clientes : new ArrayList<>();
			this.listaEspera = barberia.listaEspera != null ? barberia.listaEspera : new ArrayList<>();
			this.citas = barberia.citas != null ? barberia.citas : new ArrayList<>();
			this.tiposServicio = barberia.tiposServicio != null ? barberia.tiposServicio : new ArrayList<>();
			this.consecutivoCita = barberia.consecutivoCita;
			this.consecutivoTipoServicio = barberia.consecutivoTipoServicio;
		}
	}

	// Getters auxiliares para la interfaz.

	/** Devuelve el nombre comercial de la barbería. */
	public String getNombre() {
		return nombre;
	}

	/** Devuelve la lista de clientes registrados. */
	public List<Cliente> getClientes() {
		return clientes;
	}

	/** Devuelve los clientes en espera, ordenados por nombre. */
	public List<Cliente> getListaEspera() {
		return listaEspera;
	}

	/** Devuelve todas las citas registradas. */
	public List<Cita> getCitas() {
		return citas;
	}

	/** Devuelve el catálogo de tipos de servicio. */
	public List<TipoServicio> getTiposServicio() {
		return tiposServicio;
	}
}
