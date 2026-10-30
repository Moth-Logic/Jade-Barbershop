package control;

import java.util.List;
import java.util.ArrayList;
import java.time.LocalDate;

import logica.Cliente;
import logica.Cita;
import logica.HorarioAtencion;
import logica.TipoServicio;

public class Barberia {
	private int consecutivoCita = 0;
	private String nombre;
	private HorarioAtencion horario;
	private List<Cliente> clientes;
	private List<Cita> citas;
	private List<TipoServicio> tiposServicio;
	
	public Barberia(String nombre) {
		this.nombre = nombre;
		clientes = new ArrayList<Cliente>();
		citas = new ArrayList<Cita>();
		tiposServicio = new ArrayList<TipoServicio>();
	}
	public void crearCliente(String nombreCompleto, String tel, String email) throws Exception {
		
		if (nombreCompleto.isEmpty())
	        throw new Exception("Invalid name.");
		
		if (!tel.matches("^[0-9]{8}$"))
		    throw new Exception("Invalid phone number.");
		
		if (!email.matches("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$"))
		    throw new Exception("Invalid email.");
		
		Cliente cliente = new Cliente(nombreCompleto, tel, email);
		this.clientes.add(cliente);
	}
	public void modificarCliente(String email, String nombreCompleto, String tel, String emailNuevo) throws Exception {
		
		if (clientes.size() == 0)
			throw new Exception("No current clients");

	    Cliente cliente = consultarCliente(email);

	    if (nombreCompleto.isEmpty())
	        throw new Exception("Invalid name.");
	    
	    if (!tel.matches("^[0-9]{8}$"))
	        throw new Exception("Invalid phone number.");

	    if (!emailNuevo.matches("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$"))
	        throw new Exception("Invalid email.");

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
		
		Cita cita = new Cita(id ,fecha, hora, cliente, tipoServicio);
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
		
		if (id.isEmpty())
		    throw new Exception("Invalid ID.");
		
		if (!id.matches("^[0-9]+$"))
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
		
		if (id.isEmpty())
		    throw new Exception("Invalid ID.");
		
		if (!id.matches("^[0-9]+$"))
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
	public void crearTipoServicio(String nombre, String descripcion, double precio, int duracionMinutos) {
		
	}
	public void modificarTipoServicio(String id, String nombre, String descripcion, double precio, int duracionMinutos) {
		
	}
	public void borrarTipoServicio(String id) {
		
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
			throw new Exception("No current appointments.");
		
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
}
