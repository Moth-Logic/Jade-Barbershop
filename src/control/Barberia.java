package control;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;
import java.time.LocalDate;

import logica.Cliente;
import logica.Cita;
import logica.HorarioAtencion;
import logica.TipoServicio;

public class Barberia {
	private int consecutivoCita = 0;
	private int consecutivoTipoServicio = 0;
	private int consecutivoListaEspera = 1;
	private String nombre;
	private HorarioAtencion horario;
	private List<Cliente> clientes;
	private Map<Integer, Cliente> listaEspera;
	private List<Cita> citas;
	private List<TipoServicio> tiposServicio;
	
	public Barberia(String nombre) {
		this.nombre = nombre;
		clientes = new ArrayList<Cliente>();
		listaEspera = new TreeMap<>();
		citas = new ArrayList<Cita>();
		tiposServicio = new ArrayList<TipoServicio>();
	}
	public void validarCliente(String nombre, String tel, String email) throws Exception {
		if (nombre.isEmpty())
	        throw new Exception("Invalid name.");
	    
	    if (!tel.matches("^[0-9]{8}$"))
	        throw new Exception("Invalid phone number.");

	    if (!email.matches("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$"))
	        throw new Exception("Invalid email.");
	}
	public void crearCliente(String nombreCompleto, String tel, String email) throws Exception {
		
		validarCliente(nombreCompleto, tel, email);
		
		Cliente cliente = new Cliente(nombreCompleto, tel, email);
		this.clientes.add(cliente);
	}
	public void modificarCliente(String email, String nombreCompleto, String tel, String emailNuevo) throws Exception {
		
		if (clientes.size() == 0)
			throw new Exception("No current clients");

	    Cliente cliente = consultarCliente(email);
	    
	    validarCliente(nombreCompleto, tel, emailNuevo);

	    cliente.setNombreCompleto(nombreCompleto);
	    cliente.setTelefono(tel);
	    cliente.setEmail(emailNuevo);
	}
	public void borrarCliente(String email) throws Exception {
		
		if (clientes.size() == 0)
			throw new Exception("No current clients");
		
		if (!email.matches("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$"))
		    throw new Exception("Invalid email.");
		
		for (int i = 0; i < clientes.size(); i++) {
			if (clientes.get(i).getEmail().equals(email)) {
				clientes.remove(i);
				return;
			}
		}
		throw new Exception("Client not found.");
	}
	public Cliente consultarCliente(String email) throws Exception {
		for (int i = 0; i < clientes.size(); i++) {
			if (clientes.get(i).getEmail().equals(email)) {
				return clientes.get(i);
				
			}
		}
		throw new Exception("Client not found.");
	}
	public ArrayList<String[]> listarClientes() throws Exception {
		
		if (clientes.size() == 0)
			throw new Exception("No current clients");
		
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

	    if (id == null || id.isEmpty() || !id.matches("^[0-9]+$"))
	        throw new Exception("Invalid ID.");

	    if (fecha == null || fecha.isBefore(LocalDate.now()))
	        throw new Exception("Invalid date.");

	    if (hora < 0 || hora > 23)
	        throw new Exception("Invalid time.");
	    
	    if (!horario.estaDisponible(fecha, hora))
		    throw new Exception("Time outside business hours.");

	    if (emailCliente == null || !emailCliente.matches("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$"))
	        throw new Exception("Invalid email.");

	    if (idTipoServicio == null || idTipoServicio.isBlank() || !idTipoServicio.matches("^[0-9]+$"))
	        throw new Exception("Invalid service ID.");
	}
	public void crearCita(LocalDate fecha, int hora, String emailCliente, String idTipoServicio) throws Exception {

		validarCita("0", fecha, hora, emailCliente, idTipoServicio);
		
		Cliente cliente = consultarCliente(emailCliente);
		TipoServicio tipoServicio = consultarTipoServicio(idTipoServicio);
		
		String id = String.valueOf(consecutivoCita++);
		
		Cita cita = new Cita(id, fecha, hora, cliente, tipoServicio);
		this.citas.add(cita);
	}
	public void modificarCita(String id, LocalDate fecha, int hora, String emailCliente, String idTipoServicio) throws Exception {
		
		if (citas.size() == 0)
			throw new Exception("No current appointments");
		
		validarCita(id, fecha, hora, emailCliente, idTipoServicio);
		
		Cita cita = consultarCita(id);
		Cliente cliente = consultarCliente(emailCliente);
		TipoServicio tipoServicio = consultarTipoServicio(idTipoServicio);

	    cita.setFecha(fecha);
	    cita.setHora(hora);
	    cita.setCliente(cliente);
	    cita.setTipoServicio(tipoServicio);
	}
	public void borrarCita(String id) throws Exception {
		
		if (citas.size() == 0)
			throw new Exception("No current appointments.");
		
		if (id.isEmpty() || !id.matches("^[0-9]+$"))
		    throw new Exception("Invalid ID.");
		
		for (int i = 0; i < citas.size(); i++) {
			if (citas.get(i).getId().equals(id)) {
				citas.remove(i);
				return;
			}
		}
		throw new Exception("Appointment not found.");
	}
	public Cita consultarCita(String id) throws Exception {
		for (int i = 0; i < citas.size(); i++) {
			if (citas.get(i).getId().equals(id)) {
				return citas.get(i);
				
			}
		}
		throw new Exception("Appointment not found.");
	}
	public void confirmarCita(String id) throws Exception {
		
		if (citas.size() == 0)
			throw new Exception("No current appointments.");
		
		if (id.isEmpty() || !id.matches("^[0-9]+$"))
		    throw new Exception("Invalid ID.");
		
		Cita cita = consultarCita(id);
		
		cita.confirmar();
	}
	public ArrayList<String[]> ListarCitas() throws Exception {
		
		if (citas.size() == 0)
			throw new Exception("No current appointments.");
		
		ArrayList<String[]> citaciones = new ArrayList<>();
		
		for (int i = 0; i < citas.size(); i++) {
			String[] datos = {
				citas.get(i).getId(),
				citas.get(i).getFecha().toString(),
				String.valueOf(citas.get(i).getHora()),
				citas.get(i).getCliente().toString(),
				citas.get(i).getTipoServicio().toString(),
			};
			citaciones.add(datos);
		}
		return citaciones;
	}
	public ArrayList<String[]> getCitasPorFecha(LocalDate fecha) throws Exception {
		if (citas.size() == 0)
			throw new Exception("No current appointments.");
		
		ArrayList<String[]> citaciones = new ArrayList<>();
		
		for (int i = 0; i < citas.size(); i++) {
			if (citas.get(i).getFecha().equals(fecha)) {
				String[] datos = {
					citas.get(i).getId(),
					String.valueOf(citas.get(i).getHora()),
					citas.get(i).getCliente().toString(),
					citas.get(i).getTipoServicio().toString(),
				};
				citaciones.add(datos);
			}
			
		}
		if (citaciones.size() == 0)
			throw new Exception("No current appointments on date.");
		return citaciones;
	}
	public ArrayList<String[]> getCitasSemana(LocalDate fechaInicio) throws Exception {
		if (citas.size() == 0)
			throw new Exception("No current appointments.");
		
		ArrayList<String[]> citaciones = new ArrayList<>();
			
		for (LocalDate fecha = fechaInicio;
			fecha.isBefore(fechaInicio.plusDays(7));
			fecha = fecha.plusDays(1)) {
			
			ArrayList<String[]> CitasEnFecha = getCitasPorFecha(fecha);
			
			for (int i = 0; i < CitasEnFecha.size(); i++) {
				citaciones.add(CitasEnFecha.get(i));
			}
			
		}
		if (citaciones.size() == 0)
			throw new Exception("No current appointments on week.");
		return citaciones;
	}
	public ArrayList<String[]> getCitasMes(LocalDate fechaInicio) throws Exception {
		if (citas.size() == 0)
			throw new Exception("No current appointments.");
		
		ArrayList<String[]> citaciones = new ArrayList<>();
			
		for (LocalDate fecha = fechaInicio;
			fecha.isBefore(fechaInicio.plusMonths(1));
			fecha = fecha.plusDays(1)) {
			
			ArrayList<String[]> CitasEnFecha = getCitasPorFecha(fecha);
			
			for (int i = 0; i < CitasEnFecha.size(); i++) {
				citaciones.add(CitasEnFecha.get(i));
			}
			
		}
		if (citaciones.size() == 0)
			throw new Exception("No current appointments on month.");
		return citaciones;
	}
	private void validarTipoServicio(String id, String nombre, String descripcion, double precio, int duracionMinutos) throws Exception {

	    if (id == null || id.isEmpty() || !id.matches("^[0-9]+$"))
	        throw new Exception("Invalid ID.");

	    if (nombre == null || nombre.isEmpty())
	        throw new Exception("Invalid name.");

	    if (descripcion == null || descripcion.isEmpty())
	        throw new Exception("Invalid description.");
	    
	    if (precio <= 0)
	        throw new Exception("Invalid price.");

	    if (duracionMinutos <= 0)
	        throw new Exception("Invalid duration.");
	}
	public void crearTipoServicio(String nombre, String descripcion, double precio, int duracionMinutos) throws Exception {
		
		validarTipoServicio("0", nombre, descripcion, precio, duracionMinutos);
		
		String id = String.valueOf(consecutivoTipoServicio++);
		
		TipoServicio servicio = new TipoServicio(id, nombre, descripcion, precio, duracionMinutos);
		this.tiposServicio.add(servicio);
	}
	public void modificarTipoServicio(String id, String nombre, String descripcion, double precio, int duracionMinutos) throws Exception {
		if (tiposServicio.size() == 0)
			throw new Exception("No current services");
		
		validarTipoServicio(id, nombre, descripcion, precio, duracionMinutos);
		
		TipoServicio servicio = consultarTipoServicio(id);

		servicio.setNombre(nombre);
		servicio.setDescripcion(descripcion);
	    servicio.setPrecio(precio);
	    servicio.setDuracionMinutos(duracionMinutos);
	}
	public void borrarTipoServicio(String id) throws Exception {

		if (tiposServicio.size() == 0)
			throw new Exception("No current services.");
		
		if (id.isEmpty() || !id.matches("^[0-9]+$"))
		    throw new Exception("Invalid ID.");
		
		for (int i = 0; i < tiposServicio.size(); i++) {
			if (tiposServicio.get(i).getId().equals(id)) {
				tiposServicio.remove(i);
				return;
			}
		}
		throw new Exception("Service not found.");
	}
	public TipoServicio consultarTipoServicio(String id) throws Exception {
		for (int i = 0; i < tiposServicio.size(); i++) {
			if (tiposServicio.get(i).getId().equals(id)) {
				return tiposServicio.get(i);
				
			}
		}
		throw new Exception("Service not found.");
	}
	public ArrayList<String[]> listarTiposServicio() throws Exception {
		if (tiposServicio.size() == 0)
			throw new Exception("No current services.");
		
		ArrayList<String[]> servicios = new ArrayList<>();
		
		for (int i = 0; i < tiposServicio.size(); i++) {
			String[] datos = {
				tiposServicio.get(i).getId(),
				tiposServicio.get(i).getNombre(),
				tiposServicio.get(i).getDescripcion(),
				String.valueOf(tiposServicio.get(i).getPrecio()),
				String.valueOf(tiposServicio.get(i).getDuracionMinutos()),
			};
			servicios.add(datos);
		}
		
		return servicios;
	}
	public void agregarAListaEspera(String emailCliente) throws Exception {
		
		Cliente cliente = consultarCliente(emailCliente);
		
		this.listaEspera.put(consecutivoListaEspera++, cliente);
	}
	public void eliminarDeListaEspera(String emailCliente) throws Exception {
		
		Cliente cliente = consultarCliente(emailCliente);
		
		for (int i = 0; i < listaEspera.size(); i++) {
			if (listaEspera.get(i) == cliente) {
				listaEspera.remove(i);
				consecutivoListaEspera--;
				return;
			}
		}
		throw new Exception("Client not in waiting list.");
	}
	public ArrayList<String[]> mostrarListaEspera() throws Exception {
		if (listaEspera.size() == 0)
			throw new Exception("Waiting list is currently empty.");
		
		ArrayList<String[]> enEspera = new ArrayList<>();
		
		for (int i = 1; i <= listaEspera.size(); i++) {
			String[] datos = {
				listaEspera.get(i).getId(),
				listaEspera.get(i).getNombreCompleto(),
				listaEspera.get(i).getEmail()
			};
			enEspera.add(datos);
		}
		
		return enEspera;
	}
	public void setHorario(int dia, boolean abierto, int inicio, int fin) {
		
	}
}
