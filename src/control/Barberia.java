package control;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import logica.Cita;
import logica.Cliente;
import logica.HorarioAtencion;
import logica.TipoServicio;

public class Barberia implements Serializable {
	/**
	 * Representa la barbería y su lógica de negocio.
	 */
	private static final long serialVersionUID = 1L;
	private static final String ARCHIVO_DAT = "barberia.dat";
	private static final String ARCHIVO_STATE = "state" + File.separator + "barberia.dat";

	private int consecutivoCita = 0;
	private int consecutivoTipoServicio = 0;
	private String nombre;
	private HorarioAtencion horario;
	private List<Cliente> clientes;
	private List<Cliente> listaEspera;
	private List<Cita> citas;
	private List<TipoServicio> tiposServicio;

	public Barberia(String nombre) {
		this.nombre = nombre;
		this.horario = new HorarioAtencion();
		this.clientes = new ArrayList<>();
		this.listaEspera = new ArrayList<>();
		this.citas = new ArrayList<>();
		this.tiposServicio = new ArrayList<>();
	}

	public void validarCliente(String nombre, String tel, String email) throws Exception {
		if (nombre == null || nombre.trim().isEmpty())
			throw new Exception("El nombre no puede estar vacío.");

		if (tel == null || !tel.trim().matches("^[0-9]{8}$"))
			throw new Exception("El teléfono debe contener exactamente 8 dígitos numéricos.");

		if (email == null || !email.trim().matches("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$"))
			throw new Exception("El formato del correo electrónico es inválido.");
	}

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
	}

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
	}

	public void borrarCliente(String email) throws Exception {
		if (clientes.isEmpty())
			throw new Exception("No hay clientes registrados actualmente.");

		if (email == null || !email.trim().matches("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$"))
			throw new Exception("Formato de correo inválido.");

		String emailLimpio = email.trim();
		for (int i = 0; i < clientes.size(); i++) {
			if (clientes.get(i).getEmail().equalsIgnoreCase(emailLimpio)) {
				// Remover también de lista de espera si estuviera
				eliminarDeListaEsperaSilencioso(emailLimpio);
				clientes.remove(i);
				return;
			}
		}
		throw new Exception("Cliente no encontrado.");
	}

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

	public void crearCita(LocalDate fecha, int hora, String emailCliente, String idTipoServicio) throws Exception {
		validarCita("0", fecha, hora, emailCliente, idTipoServicio);

		// Validar si ya existe cita para la misma fecha y hora (evitar colisiones)
		for (Cita c : citas) {
			if (c.getFecha().equals(fecha) && c.getHora() == hora) {
				throw new Exception("Ya existe una cita agendada para el " + fecha + " a las " + hora + ":00.");
			}
		}

		Cliente cliente = consultarCliente(emailCliente);
		TipoServicio tipoServicio = consultarTipoServicio(idTipoServicio);

		String id = String.valueOf(consecutivoCita++);
		Cita cita = new Cita(id, fecha, hora, cliente, tipoServicio);
		this.citas.add(cita);
	}

	public void modificarCita(String id, LocalDate fecha, int hora, String emailCliente, String idTipoServicio) throws Exception {
		if (citas.isEmpty())
			throw new Exception("No hay citas registradas actualmente.");

		validarCita(id, fecha, hora, emailCliente, idTipoServicio);

		// Verificar que no haya colisión con otra cita
		for (Cita c : citas) {
			if (!c.getId().equals(id) && c.getFecha().equals(fecha) && c.getHora() == hora) {
				throw new Exception("Ya existe otra cita agendada para el " + fecha + " a las " + hora + ":00.");
			}
		}

		Cita cita = consultarCita(id);
		Cliente cliente = consultarCliente(emailCliente);
		TipoServicio tipoServicio = consultarTipoServicio(idTipoServicio);

		cita.setFecha(fecha);
		cita.setHora(hora);
		cita.setCliente(cliente);
		cita.setTipoServicio(tipoServicio);
	}

	public void borrarCita(String id) throws Exception {
		if (citas.isEmpty())
			throw new Exception("No hay citas registradas actualmente.");

		if (id == null || id.trim().isEmpty() || !id.trim().matches("^[0-9]+$"))
			throw new Exception("ID de cita inválido.");

		for (int i = 0; i < citas.size(); i++) {
			if (citas.get(i).getId().equals(id.trim())) {
				citas.remove(i);
				return;
			}
		}
		throw new Exception("Cita no encontrada.");
	}

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

	public void confirmarCita(String id) throws Exception {
		if (citas.isEmpty())
			throw new Exception("No hay citas registradas actualmente.");

		Cita cita = consultarCita(id);
		cita.confirmar();
	}

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

	// Alias para compatibilidad con llamadas existentes
	public ArrayList<String[]> ListarCitas() throws Exception {
		return listarCitas();
	}

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

	public void crearTipoServicio(String nombre, String descripcion, double precio, int duracionMinutos) throws Exception {
		validarTipoServicio("0", nombre, descripcion, precio, duracionMinutos);

		String id = String.valueOf(consecutivoTipoServicio++);
		TipoServicio servicio = new TipoServicio(id, nombre.trim(), descripcion.trim(), precio, duracionMinutos);
		this.tiposServicio.add(servicio);
	}

	public void modificarTipoServicio(String id, String nombre, String descripcion, double precio, int duracionMinutos) throws Exception {
		if (tiposServicio.isEmpty())
			throw new Exception("No hay servicios registrados actualmente.");

		validarTipoServicio(id, nombre, descripcion, precio, duracionMinutos);

		TipoServicio servicio = consultarTipoServicio(id);
		servicio.setNombre(nombre.trim());
		servicio.setDescripcion(descripcion.trim());
		servicio.setPrecio(precio);
		servicio.setDuracionMinutos(duracionMinutos);
	}

	public void borrarTipoServicio(String id) throws Exception {
		if (tiposServicio.isEmpty())
			throw new Exception("No hay servicios registrados actualmente.");

		if (id == null || id.trim().isEmpty() || !id.trim().matches("^[0-9]+$"))
			throw new Exception("ID de servicio inválido.");

		for (int i = 0; i < tiposServicio.size(); i++) {
			if (tiposServicio.get(i).getId().equals(id.trim())) {
				tiposServicio.remove(i);
				return;
			}
		}
		throw new Exception("Servicio no encontrado.");
	}

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

	public void agregarAListaEspera(String emailCliente) throws Exception {
		Cliente cliente = consultarCliente(emailCliente);

		for (Cliente c : listaEspera) {
			if (c.getEmail().equalsIgnoreCase(emailCliente.trim())) {
				throw new Exception("El cliente ya está en la lista de espera.");
			}
		}

		this.listaEspera.add(cliente);
		this.listaEspera.sort(null);
	}

	public void eliminarDeListaEspera(String emailCliente) throws Exception {
		if (emailCliente == null)
			throw new Exception("Correo no puede ser nulo.");

		String emailLimpio = emailCliente.trim();
		for (int i = 0; i < listaEspera.size(); i++) {
			if (listaEspera.get(i).getEmail().equalsIgnoreCase(emailLimpio)) {
				listaEspera.remove(i);
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

	public ArrayList<String[]> mostrarListaEspera() throws Exception {
		ArrayList<String[]> enEspera = new ArrayList<>();
		for (int i = 0; i < listaEspera.size(); i++) {
			Cliente c = listaEspera.get(i);
			String[] datos = {
				String.valueOf(i + 1), // Posición en la lista
				c.getNombreCompleto(),
				c.getTelefono(),
				c.getEmail()
			};
			enEspera.add(datos);
		}
		return enEspera;
	}

	public void setHorario(int dia, boolean abierto, int inicio, int fin) throws Exception {
		if (horario == null) {
			horario = new HorarioAtencion();
		}
		horario.setDia(dia, abierto, inicio, fin);
	}

	public HorarioAtencion getHorario() {
		if (horario == null) {
			horario = new HorarioAtencion();
		}
		return horario;
	}

	public String enviarConfirmacion(String idCita) throws Exception {
		Cita cita = consultarCita(idCita);
		cita.confirmar();
		Cliente cliente = cita.getCliente();
		return "Confirmación enviada exitosamente a: " + (cliente != null ? cliente.getEmail() : "N/A");
	}

	public String enviarRecordatorio(String idCita) throws Exception {
		Cita cita = consultarCita(idCita);
		Cliente cliente = cita.getCliente();
		return "Recordatorio enviado exitosamente a: " + (cliente != null ? cliente.getEmail() : "N/A");
	}

	private File obtenerArchivoGuardado() {
		File dirState = new File("state");
		if (dirState.exists() && dirState.isDirectory()) {
			return new File(dirState, ARCHIVO_DAT);
		}
		return new File(ARCHIVO_DAT);
	}

	public void guardar() throws Exception {
		File archivo = obtenerArchivoGuardado();
		try (ObjectOutputStream salida = new ObjectOutputStream(new FileOutputStream(archivo))) {
			salida.writeObject(this);
		}
	}

	public void cargar() throws Exception {
		File archivoState = new File("state", ARCHIVO_DAT);
		File archivoRaiz = new File(ARCHIVO_DAT);
		File archivoACargar = archivoState.exists() ? archivoState : (archivoRaiz.exists() ? archivoRaiz : null);

		if (archivoACargar == null) {
			return; // No hay archivo previo, inicia con estado limpio
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

	// Getters auxiliares para vistas y modelos de la GUI
	public String getNombre() {
		return nombre;
	}

	public List<Cliente> getClientes() {
		return clientes;
	}

	public List<Cliente> getListaEspera() {
		return listaEspera;
	}

	public List<Cita> getCitas() {
		return citas;
	}

	public List<TipoServicio> getTiposServicio() {
		return tiposServicio;
	}
}
