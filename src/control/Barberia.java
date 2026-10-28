package control;

import java.util.List;
import java.util.ArrayList;
import java.time.LocalDate;

import logica.Cliente;
import logica.Cita;
import logica.HorarioAtencion;
import logica.TipoServicio;

public class Barberia {
	private String nombre;
	private List<Cliente> clientes;
	private List<Cita> citas;
	private List<HorarioAtencion> horario;
	private List<TipoServicio> tiposServicio;
	
	public Barberia(String nombre) {
		this.nombre = nombre;
		clientes = new ArrayList<Cliente>();
		citas = new ArrayList<Cita>();
		horario = new ArrayList<HorarioAtencion>();
		tiposServicio = new ArrayList<TipoServicio>();
	}
	public void crearCliente(String nombreCompleto, String tel, String email) {
		Cliente cliente = new Cliente(nombreCompleto, tel, email);
		this.clientes.add(cliente);
	}
	public void modificarCliente(String email, String nombreCompleto, String tel) {
		
	}
	public void borrarCliente(String email) throws Exception {
		if (clientes.size() == 0)
			throw new Exception("List is empty.");
		for (int i = 0; i < clientes.size(); i++) {
			if (clientes.get(i).getEmail().equals(email)) {
				clientes.remove(i);
				return;
			}
		}
		throw new Exception("Email not found.");
	}
	public Cliente consultarCliente(String email) throws Exception {
		if (clientes.size() == 0)
			throw new Exception("List is empty.");
		for (int i = 0; i < clientes.size(); i++) {
			if (clientes.get(i).getEmail().equals(email)) {
				return clientes.get(i);
				
			}
		}
		throw new Exception("Email not found.");
	}
	public ArrayList<String[]> listarClientes() {
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
	public void crearCita(String id, LocalDate fecha, int hora, String emailCliente, String idTipoServicio) throws Exception {
		Cliente cliente = consultarCliente(emailCliente);
		TipoServicio tipoServicio = consultarTipoServicio(idTipoServicio);
		Cita cita = new Cita(id ,fecha, hora, cliente, tipoServicio);
		this.citas.add(cita);
	}
	public TipoServicio consultarTipoServicio(String id) throws Exception {
		if (tiposServicio.size() == 0)
			throw new Exception("List is empty.");
		for (int i = 0; i < tiposServicio.size(); i++) {
			if (clientes.get(i).getId().equals(id)) {
				return tiposServicio.get(i);
				
			}
		}
		throw new Exception("Id not found.");
	}
}
