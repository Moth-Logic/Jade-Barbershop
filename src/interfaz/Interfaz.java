package interfaz;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;

import control.Barberia;
import logica.HorarioAtencion;

public class Interfaz extends JFrame {

	private static final long serialVersionUID = 1L;

	private Barberia barberia;
	private JTabbedPane tabbedPane;

	// Componentes: Clientes
	private JTable tablaClientes;
	private DefaultTableModel modeloClientes;
	private JTextField txtNombreCliente;
	private JTextField txtTelCliente;
	private JTextField txtEmailCliente;
	private String emailClienteSeleccionado;

	// Componentes: Tipos de Servicio
	private JTable tablaServicios;
	private DefaultTableModel modeloServicios;
	private JTextField txtNombreServicio;
	private JTextField txtDescServicio;
	private JTextField txtPrecioServicio;
	private JTextField txtDuracionServicio;
	private String idServicioSeleccionado;

	// Componentes: Citas
	private JTable tablaCitas;
	private DefaultTableModel modeloCitas;
	private JTextField txtFechaCita;
	private JTextField txtHoraCita;
	private JTextField txtEmailClienteCita;
	private JTextField txtIdServicioCita;
	private String idCitaSeleccionada;

	// Componentes: Calendario
	private JTable tablaCalendario;
	private DefaultTableModel modeloCalendario;
	private JTextField txtFechaCalendario;

	// Componentes: Lista de Espera
	private JTable tablaEspera;
	private DefaultTableModel modeloEspera;
	private JTextField txtEmailEspera;
	private String emailEsperaSeleccionado;

	// Componentes: Horario de Atención
	private JCheckBox[] chkAbierto = new JCheckBox[7];
	private JSpinner[] spnInicio = new JSpinner[7];
	private JSpinner[] spnFin = new JSpinner[7];

	public Interfaz() {
		barberia = new Barberia("Jade Barbershop");
		try {
			barberia.cargar();
		} catch (Exception e) {
			// Si falla la carga se iniciará con el estado por defecto
		}
		initComponents();
		cargarDatosIniciales();
	}

	private void initComponents() {
		setTitle("Jade Barbershop - Sistema de Gestión");
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setBounds(100, 100, 980, 680);
		setLocationRelativeTo(null);

		tabbedPane = new JTabbedPane();
		getContentPane().add(tabbedPane, BorderLayout.CENTER);

		tabbedPane.addTab("Clientes", crearPanelClientes());
		tabbedPane.addTab("Tipos de Servicio", crearPanelServicios());
		tabbedPane.addTab("Citas", crearPanelCitas());
		tabbedPane.addTab("Calendario", crearPanelCalendario());
		tabbedPane.addTab("Lista de Espera", crearPanelEspera());
		tabbedPane.addTab("Horario", crearPanelHorario());

		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				try {
					barberia.guardar();
				} catch (Exception ex) {
					JOptionPane.showMessageDialog(Interfaz.this,
						"Error al guardar los datos: " + ex.getMessage(),
						"Error de Persistencia", JOptionPane.ERROR_MESSAGE);
				}
			}
		});
	}

	private void cargarDatosIniciales() {
		actualizarTablaClientes();
		actualizarTablaServicios();
		actualizarTablaCitas();
		actualizarTablaEspera();
		actualizarTablaCalendarioHoy();
		cargarValoresHorario();
	}

	// =========================================================================
	// 1. PANEL CLIENTES
	// =========================================================================
	private JPanel crearPanelClientes() {
		JPanel panel = new JPanel(new BorderLayout(5, 5));
		panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

		modeloClientes = new DefaultTableModel(
			new String[]{"Nombre", "Teléfono", "Email"}, 0) {
			@Override
			public boolean isCellEditable(int row, int col) {
				return false;
			}
		};
		tablaClientes = new JTable(modeloClientes);
		tablaClientes.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		tablaClientes.getSelectionModel().addListSelectionListener(e -> {
			if (e.getValueIsAdjusting()) return;
			int row = tablaClientes.getSelectedRow();
			if (row >= 0) {
				txtNombreCliente.setText((String) modeloClientes.getValueAt(row, 0));
				txtTelCliente.setText((String) modeloClientes.getValueAt(row, 1));
				txtEmailCliente.setText((String) modeloClientes.getValueAt(row, 2));
				emailClienteSeleccionado = (String) modeloClientes.getValueAt(row, 2);
			}
		});
		panel.add(new JScrollPane(tablaClientes), BorderLayout.CENTER);

		JPanel formPanel = new JPanel(new GridLayout(3, 2, 5, 5));
		formPanel.add(new JLabel("Nombre Completo:"));
		txtNombreCliente = new JTextField();
		formPanel.add(txtNombreCliente);
		formPanel.add(new JLabel("Teléfono (8 dígitos):"));
		txtTelCliente = new JTextField();
		formPanel.add(txtTelCliente);
		formPanel.add(new JLabel("Email (identificador único):"));
		txtEmailCliente = new JTextField();
		formPanel.add(txtEmailCliente);

		JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 5));

		JButton btnCrear = new JButton("Crear");
		btnCrear.addActionListener(e -> {
			try {
				barberia.crearCliente(
					txtNombreCliente.getText(),
					txtTelCliente.getText(),
					txtEmailCliente.getText());
				actualizarTablaClientes();
				limpiarCamposCliente();
				JOptionPane.showMessageDialog(this, "Cliente creado exitosamente");
			} catch (Exception ex) {
				JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
			}
		});
		btnPanel.add(btnCrear);

		JButton btnModificar = new JButton("Modificar");
		btnModificar.addActionListener(e -> {
			try {
				if (emailClienteSeleccionado == null) {
					JOptionPane.showMessageDialog(this, "Seleccione un cliente de la tabla");
					return;
				}
				barberia.modificarCliente(
					emailClienteSeleccionado,
					txtNombreCliente.getText(),
					txtTelCliente.getText(),
					txtEmailCliente.getText());
				actualizarTablaClientes();
				limpiarCamposCliente();
				JOptionPane.showMessageDialog(this, "Cliente modificado exitosamente");
			} catch (Exception ex) {
				JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
			}
		});
		btnPanel.add(btnModificar);

		JButton btnBorrar = new JButton("Borrar");
		btnBorrar.addActionListener(e -> {
			try {
				if (emailClienteSeleccionado == null) {
					JOptionPane.showMessageDialog(this, "Seleccione un cliente de la tabla");
					return;
				}
				int opcion = JOptionPane.showConfirmDialog(this,
					"¿Está seguro de que desea borrar este cliente?",
					"Confirmar", JOptionPane.YES_NO_OPTION);
				if (opcion == JOptionPane.YES_OPTION) {
					barberia.borrarCliente(emailClienteSeleccionado);
					actualizarTablaClientes();
					actualizarTablaEspera();
					limpiarCamposCliente();
				}
			} catch (Exception ex) {
				JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
			}
		});
		btnPanel.add(btnBorrar);

		JButton btnBuscar = new JButton("Buscar");
		btnBuscar.addActionListener(e -> {
			try {
				String email = txtEmailCliente.getText();
				logica.Cliente c = barberia.consultarCliente(email);
				txtNombreCliente.setText(c.getNombreCompleto());
				txtTelCliente.setText(c.getTelefono());
				txtEmailCliente.setText(c.getEmail());
				emailClienteSeleccionado = c.getEmail();
				seleccionarFilaCliente(c.getEmail());
			} catch (Exception ex) {
				JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
			}
		});
		btnPanel.add(btnBuscar);

		JButton btnListar = new JButton("Listar");
		btnListar.addActionListener(e -> actualizarTablaClientes());
		btnPanel.add(btnListar);

		JButton btnLimpiar = new JButton("Limpiar");
		btnLimpiar.addActionListener(e -> limpiarCamposCliente());
		btnPanel.add(btnLimpiar);

		JPanel southPanel = new JPanel(new BorderLayout());
		southPanel.add(formPanel, BorderLayout.CENTER);
		southPanel.add(btnPanel, BorderLayout.SOUTH);
		panel.add(southPanel, BorderLayout.SOUTH);

		return panel;
	}

	private void seleccionarFilaCliente(String email) {
		for (int i = 0; i < modeloClientes.getRowCount(); i++) {
			if (email.equalsIgnoreCase((String) modeloClientes.getValueAt(i, 2))) {
				tablaClientes.setRowSelectionInterval(i, i);
				break;
			}
		}
	}

	private void actualizarTablaClientes() {
		modeloClientes.setRowCount(0);
		try {
			ArrayList<String[]> lista = barberia.listarClientes();
			for (int i = 0; i < lista.size(); i++) {
				modeloClientes.addRow(lista.get(i));
			}
		} catch (Exception e) {
			// Si no hay clientes la tabla simplemente queda vacía
		}
	}

	private void limpiarCamposCliente() {
		txtNombreCliente.setText("");
		txtTelCliente.setText("");
		txtEmailCliente.setText("");
		emailClienteSeleccionado = null;
		tablaClientes.clearSelection();
	}

	// =========================================================================
	// 2. PANEL TIPOS DE SERVICIO
	// =========================================================================
	private JPanel crearPanelServicios() {
		JPanel panel = new JPanel(new BorderLayout(5, 5));
		panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

		modeloServicios = new DefaultTableModel(
			new String[]{"ID", "Nombre", "Descripción", "Precio (₡)", "Duración (min)"}, 0) {
			@Override
			public boolean isCellEditable(int row, int col) {
				return false;
			}
		};
		tablaServicios = new JTable(modeloServicios);
		tablaServicios.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		tablaServicios.getSelectionModel().addListSelectionListener(e -> {
			if (e.getValueIsAdjusting()) return;
			int row = tablaServicios.getSelectedRow();
			if (row >= 0) {
				idServicioSeleccionado = (String) modeloServicios.getValueAt(row, 0);
				txtNombreServicio.setText((String) modeloServicios.getValueAt(row, 1));
				txtDescServicio.setText((String) modeloServicios.getValueAt(row, 2));
				txtPrecioServicio.setText((String) modeloServicios.getValueAt(row, 3));
				txtDuracionServicio.setText((String) modeloServicios.getValueAt(row, 4));
			}
		});
		panel.add(new JScrollPane(tablaServicios), BorderLayout.CENTER);

		JPanel formPanel = new JPanel(new GridLayout(4, 2, 5, 5));
		formPanel.add(new JLabel("Nombre:"));
		txtNombreServicio = new JTextField();
		formPanel.add(txtNombreServicio);
		formPanel.add(new JLabel("Descripción:"));
		txtDescServicio = new JTextField();
		formPanel.add(txtDescServicio);
		formPanel.add(new JLabel("Precio (₡):"));
		txtPrecioServicio = new JTextField();
		formPanel.add(txtPrecioServicio);
		formPanel.add(new JLabel("Duración (min):"));
		txtDuracionServicio = new JTextField();
		formPanel.add(txtDuracionServicio);

		JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 5));

		JButton btnCrear = new JButton("Crear");
		btnCrear.addActionListener(e -> {
			try {
				barberia.crearTipoServicio(
					txtNombreServicio.getText(),
					txtDescServicio.getText(),
					Double.parseDouble(txtPrecioServicio.getText().trim()),
					Integer.parseInt(txtDuracionServicio.getText().trim()));
				actualizarTablaServicios();
				limpiarCamposServicio();
				JOptionPane.showMessageDialog(this, "Tipo de servicio creado exitosamente");
			} catch (NumberFormatException ex) {
				JOptionPane.showMessageDialog(this, "Precio o duración inválidos.", "Error de Formato", JOptionPane.ERROR_MESSAGE);
			} catch (Exception ex) {
				JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
			}
		});
		btnPanel.add(btnCrear);

		JButton btnModificar = new JButton("Modificar");
		btnModificar.addActionListener(e -> {
			try {
				if (idServicioSeleccionado == null) {
					JOptionPane.showMessageDialog(this, "Seleccione un servicio de la tabla");
					return;
				}
				barberia.modificarTipoServicio(
					idServicioSeleccionado,
					txtNombreServicio.getText(),
					txtDescServicio.getText(),
					Double.parseDouble(txtPrecioServicio.getText().trim()),
					Integer.parseInt(txtDuracionServicio.getText().trim()));
				actualizarTablaServicios();
				limpiarCamposServicio();
				JOptionPane.showMessageDialog(this, "Tipo de servicio modificado exitosamente");
			} catch (NumberFormatException ex) {
				JOptionPane.showMessageDialog(this, "Precio o duración inválidos.", "Error de Formato", JOptionPane.ERROR_MESSAGE);
			} catch (Exception ex) {
				JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
			}
		});
		btnPanel.add(btnModificar);

		JButton btnBorrar = new JButton("Borrar");
		btnBorrar.addActionListener(e -> {
			try {
				if (idServicioSeleccionado == null) {
					JOptionPane.showMessageDialog(this, "Seleccione un servicio de la tabla");
					return;
				}
				int opcion = JOptionPane.showConfirmDialog(this,
					"¿Está seguro de que desea borrar este servicio?",
					"Confirmar", JOptionPane.YES_NO_OPTION);
				if (opcion == JOptionPane.YES_OPTION) {
					barberia.borrarTipoServicio(idServicioSeleccionado);
					actualizarTablaServicios();
					limpiarCamposServicio();
				}
			} catch (Exception ex) {
				JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
			}
		});
		btnPanel.add(btnBorrar);

		JButton btnListar = new JButton("Listar");
		btnListar.addActionListener(e -> actualizarTablaServicios());
		btnPanel.add(btnListar);

		JButton btnLimpiar = new JButton("Limpiar");
		btnLimpiar.addActionListener(e -> limpiarCamposServicio());
		btnPanel.add(btnLimpiar);

		JPanel southPanel = new JPanel(new BorderLayout());
		southPanel.add(formPanel, BorderLayout.CENTER);
		southPanel.add(btnPanel, BorderLayout.SOUTH);
		panel.add(southPanel, BorderLayout.SOUTH);

		return panel;
	}

	private void actualizarTablaServicios() {
		modeloServicios.setRowCount(0);
		try {
			ArrayList<String[]> lista = barberia.listarTiposServicio();
			for (int i = 0; i < lista.size(); i++) {
				modeloServicios.addRow(lista.get(i));
			}
		} catch (Exception e) {
		}
	}

	private void limpiarCamposServicio() {
		txtNombreServicio.setText("");
		txtDescServicio.setText("");
		txtPrecioServicio.setText("");
		txtDuracionServicio.setText("");
		idServicioSeleccionado = null;
		tablaServicios.clearSelection();
	}

	// =========================================================================
	// 3. PANEL CITAS
	// =========================================================================
	private JPanel crearPanelCitas() {
		JPanel panel = new JPanel(new BorderLayout(5, 5));
		panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

		modeloCitas = new DefaultTableModel(
			new String[]{"ID", "Fecha", "Hora", "Cliente", "Servicio", "Estado"}, 0) {
			@Override
			public boolean isCellEditable(int row, int col) {
				return false;
			}
		};
		tablaCitas = new JTable(modeloCitas);
		tablaCitas.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		tablaCitas.getSelectionModel().addListSelectionListener(e -> {
			if (e.getValueIsAdjusting()) return;
			int row = tablaCitas.getSelectedRow();
			if (row >= 0) {
				idCitaSeleccionada = (String) modeloCitas.getValueAt(row, 0);
				txtFechaCita.setText((String) modeloCitas.getValueAt(row, 1));
				String horaStr = (String) modeloCitas.getValueAt(row, 2);
				if (horaStr.contains(":")) {
					horaStr = horaStr.split(":")[0];
				}
				txtHoraCita.setText(horaStr);

				// Extraer email del cliente si viene en formato Nombre (email)
				try {
					logica.Cita citaObj = barberia.consultarCita(idCitaSeleccionada);
					if (citaObj.getCliente() != null) {
						txtEmailClienteCita.setText(citaObj.getCliente().getEmail());
					}
					if (citaObj.getTipoServicio() != null) {
						txtIdServicioCita.setText(citaObj.getTipoServicio().getId());
					}
				} catch (Exception ex) {
				}
			}
		});
		panel.add(new JScrollPane(tablaCitas), BorderLayout.CENTER);

		JPanel formPanel = new JPanel(new GridLayout(4, 2, 5, 5));
		formPanel.add(new JLabel("Fecha (YYYY-MM-DD):"));
		txtFechaCita = new JTextField(LocalDate.now().toString());
		formPanel.add(txtFechaCita);

		formPanel.add(new JLabel("Hora (0 a 23):"));
		txtHoraCita = new JTextField("9");
		formPanel.add(txtHoraCita);

		formPanel.add(new JLabel("Email del Cliente:"));
		txtEmailClienteCita = new JTextField();
		formPanel.add(txtEmailClienteCita);

		formPanel.add(new JLabel("ID Tipo de Servicio:"));
		txtIdServicioCita = new JTextField();
		formPanel.add(txtIdServicioCita);

		JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 5));

		JButton btnCrear = new JButton("Crear Cita");
		btnCrear.addActionListener(e -> {
			try {
				LocalDate fecha = LocalDate.parse(txtFechaCita.getText().trim());
				int hora = Integer.parseInt(txtHoraCita.getText().trim());
				barberia.crearCita(fecha, hora, txtEmailClienteCita.getText().trim(), txtIdServicioCita.getText().trim());
				actualizarTablaCitas();
				actualizarTablaCalendarioHoy();
				limpiarCamposCita();
				JOptionPane.showMessageDialog(this, "Cita agendada exitosamente");
			} catch (DateTimeParseException ex) {
				JOptionPane.showMessageDialog(this, "Formato de fecha inválido. Utilice YYYY-MM-DD (ej: 2026-11-10)", "Error", JOptionPane.ERROR_MESSAGE);
			} catch (NumberFormatException ex) {
				JOptionPane.showMessageDialog(this, "La hora debe ser un número entero (0-23)", "Error", JOptionPane.ERROR_MESSAGE);
			} catch (Exception ex) {
				JOptionPane.showMessageDialog(this, ex.getMessage(), "Error al Crear Cita", JOptionPane.ERROR_MESSAGE);
			}
		});
		btnPanel.add(btnCrear);

		JButton btnModificar = new JButton("Modificar Cita");
		btnModificar.addActionListener(e -> {
			try {
				if (idCitaSeleccionada == null) {
					JOptionPane.showMessageDialog(this, "Seleccione una cita de la tabla");
					return;
				}
				LocalDate fecha = LocalDate.parse(txtFechaCita.getText().trim());
				int hora = Integer.parseInt(txtHoraCita.getText().trim());
				barberia.modificarCita(idCitaSeleccionada, fecha, hora, txtEmailClienteCita.getText().trim(), txtIdServicioCita.getText().trim());
				actualizarTablaCitas();
				actualizarTablaCalendarioHoy();
				limpiarCamposCita();
				JOptionPane.showMessageDialog(this, "Cita modificada exitosamente");
			} catch (DateTimeParseException ex) {
				JOptionPane.showMessageDialog(this, "Formato de fecha inválido. Utilice YYYY-MM-DD", "Error", JOptionPane.ERROR_MESSAGE);
			} catch (NumberFormatException ex) {
				JOptionPane.showMessageDialog(this, "Hora inválida (0-23)", "Error", JOptionPane.ERROR_MESSAGE);
			} catch (Exception ex) {
				JOptionPane.showMessageDialog(this, ex.getMessage(), "Error al Modificar", JOptionPane.ERROR_MESSAGE);
			}
		});
		btnPanel.add(btnModificar);

		JButton btnBorrar = new JButton("Borrar Cita");
		btnBorrar.addActionListener(e -> {
			try {
				if (idCitaSeleccionada == null) {
					JOptionPane.showMessageDialog(this, "Seleccione una cita de la tabla");
					return;
				}
				int opcion = JOptionPane.showConfirmDialog(this,
					"¿Desea cancelar y borrar esta cita?",
					"Confirmar", JOptionPane.YES_NO_OPTION);
				if (opcion == JOptionPane.YES_OPTION) {
					barberia.borrarCita(idCitaSeleccionada);
					actualizarTablaCitas();
					actualizarTablaCalendarioHoy();
					limpiarCamposCita();
					JOptionPane.showMessageDialog(this, "Cita borrada exitosamente");
				}
			} catch (Exception ex) {
				JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
			}
		});
		btnPanel.add(btnBorrar);

		JButton btnConfirmar = new JButton("Confirmar Cita");
		btnConfirmar.addActionListener(e -> {
			try {
				if (idCitaSeleccionada == null) {
					JOptionPane.showMessageDialog(this, "Seleccione una cita de la tabla");
					return;
				}
				barberia.confirmarCita(idCitaSeleccionada);
				actualizarTablaCitas();
				actualizarTablaCalendarioHoy();
				JOptionPane.showMessageDialog(this, "Cita confirmada");
			} catch (Exception ex) {
				JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
			}
		});
		btnPanel.add(btnConfirmar);

		JButton btnEnviarConfirmacion = new JButton("Enviar Confirmación");
		btnEnviarConfirmacion.addActionListener(e -> {
			try {
				if (idCitaSeleccionada == null) {
					JOptionPane.showMessageDialog(this, "Seleccione una cita de la tabla");
					return;
				}
				String respuesta = barberia.enviarConfirmacion(idCitaSeleccionada);
				actualizarTablaCitas();
				JOptionPane.showMessageDialog(this, respuesta, "Notificación", JOptionPane.INFORMATION_MESSAGE);
			} catch (Exception ex) {
				JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
			}
		});
		btnPanel.add(btnEnviarConfirmacion);

		JButton btnEnviarRecordatorio = new JButton("Enviar Recordatorio");
		btnEnviarRecordatorio.addActionListener(e -> {
			try {
				if (idCitaSeleccionada == null) {
					JOptionPane.showMessageDialog(this, "Seleccione una cita de la tabla");
					return;
				}
				String respuesta = barberia.enviarRecordatorio(idCitaSeleccionada);
				JOptionPane.showMessageDialog(this, respuesta, "Notificación", JOptionPane.INFORMATION_MESSAGE);
			} catch (Exception ex) {
				JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
			}
		});
		btnPanel.add(btnEnviarRecordatorio);

		JButton btnListar = new JButton("Listar Citas");
		btnListar.addActionListener(e -> actualizarTablaCitas());
		btnPanel.add(btnListar);

		JButton btnLimpiar = new JButton("Limpiar");
		btnLimpiar.addActionListener(e -> limpiarCamposCita());
		btnPanel.add(btnLimpiar);

		JPanel southPanel = new JPanel(new BorderLayout());
		southPanel.add(formPanel, BorderLayout.CENTER);
		southPanel.add(btnPanel, BorderLayout.SOUTH);
		panel.add(southPanel, BorderLayout.SOUTH);

		return panel;
	}

	private void actualizarTablaCitas() {
		modeloCitas.setRowCount(0);
		try {
			ArrayList<String[]> lista = barberia.listarCitas();
			for (int i = 0; i < lista.size(); i++) {
				modeloCitas.addRow(lista.get(i));
			}
		} catch (Exception e) {
		}
	}

	private void limpiarCamposCita() {
		txtFechaCita.setText(LocalDate.now().toString());
		txtHoraCita.setText("9");
		txtEmailClienteCita.setText("");
		txtIdServicioCita.setText("");
		idCitaSeleccionada = null;
		tablaCitas.clearSelection();
	}

	// =========================================================================
	// 4. PANEL CALENDARIO
	// =========================================================================
	private JPanel crearPanelCalendario() {
		JPanel panel = new JPanel(new BorderLayout(5, 5));
		panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

		JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
		topPanel.add(new JLabel("Fecha de Consulta (YYYY-MM-DD):"));
		txtFechaCalendario = new JTextField(LocalDate.now().toString(), 10);
		topPanel.add(txtFechaCalendario);

		JButton btnVerDia = new JButton("Citas del Día");
		btnVerDia.addActionListener(e -> {
			try {
				LocalDate fecha = LocalDate.parse(txtFechaCalendario.getText().trim());
				ArrayList<String[]> citas = barberia.getCitasPorFecha(fecha);
				mostrarCitasEnCalendario(citas);
			} catch (DateTimeParseException ex) {
				JOptionPane.showMessageDialog(this, "Formato de fecha inválido (use YYYY-MM-DD)", "Error", JOptionPane.ERROR_MESSAGE);
			} catch (Exception ex) {
				JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
			}
		});
		topPanel.add(btnVerDia);

		JButton btnVerSemana = new JButton("Citas de la Semana");
		btnVerSemana.addActionListener(e -> {
			try {
				LocalDate fecha = LocalDate.parse(txtFechaCalendario.getText().trim());
				ArrayList<String[]> citas = barberia.getCitasSemana(fecha);
				mostrarCitasEnCalendario(citas);
			} catch (DateTimeParseException ex) {
				JOptionPane.showMessageDialog(this, "Formato de fecha inválido (use YYYY-MM-DD)", "Error", JOptionPane.ERROR_MESSAGE);
			} catch (Exception ex) {
				JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
			}
		});
		topPanel.add(btnVerSemana);

		JButton btnVerMes = new JButton("Citas del Mes");
		btnVerMes.addActionListener(e -> {
			try {
				LocalDate fecha = LocalDate.parse(txtFechaCalendario.getText().trim());
				ArrayList<String[]> citas = barberia.getCitasMes(fecha);
				mostrarCitasEnCalendario(citas);
			} catch (DateTimeParseException ex) {
				JOptionPane.showMessageDialog(this, "Formato de fecha inválido (use YYYY-MM-DD)", "Error", JOptionPane.ERROR_MESSAGE);
			} catch (Exception ex) {
				JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
			}
		});
		topPanel.add(btnVerMes);

		JButton btnHoy = new JButton("Hoy");
		btnHoy.addActionListener(e -> {
			txtFechaCalendario.setText(LocalDate.now().toString());
			actualizarTablaCalendarioHoy();
		});
		topPanel.add(btnHoy);

		panel.add(topPanel, BorderLayout.NORTH);

		modeloCalendario = new DefaultTableModel(
			new String[]{"ID", "Fecha", "Hora", "Cliente", "Servicio", "Estado"}, 0) {
			@Override
			public boolean isCellEditable(int row, int col) {
				return false;
			}
		};
		tablaCalendario = new JTable(modeloCalendario);
		panel.add(new JScrollPane(tablaCalendario), BorderLayout.CENTER);

		return panel;
	}

	private void mostrarCitasEnCalendario(ArrayList<String[]> citas) {
		modeloCalendario.setRowCount(0);
		for (int i = 0; i < citas.size(); i++) {
			modeloCalendario.addRow(citas.get(i));
		}
		if (citas.isEmpty()) {
			JOptionPane.showMessageDialog(this, "No se encontraron citas agendadas para el período seleccionado.", "Calendario", JOptionPane.INFORMATION_MESSAGE);
		}
	}

	private void actualizarTablaCalendarioHoy() {
		try {
			ArrayList<String[]> citas = barberia.getCitasPorFecha(LocalDate.now());
			modeloCalendario.setRowCount(0);
			for (int i = 0; i < citas.size(); i++) {
				modeloCalendario.addRow(citas.get(i));
			}
		} catch (Exception e) {
		}
	}

	// =========================================================================
	// 5. PANEL LISTA DE ESPERA
	// =========================================================================
	private JPanel crearPanelEspera() {
		JPanel panel = new JPanel(new BorderLayout(5, 5));
		panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

		modeloEspera = new DefaultTableModel(
			new String[]{"Turno", "Nombre", "Teléfono", "Email"}, 0) {
			@Override
			public boolean isCellEditable(int row, int col) {
				return false;
			}
		};
		tablaEspera = new JTable(modeloEspera);
		tablaEspera.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		tablaEspera.getSelectionModel().addListSelectionListener(e -> {
			if (e.getValueIsAdjusting()) return;
			int row = tablaEspera.getSelectedRow();
			if (row >= 0) {
				emailEsperaSeleccionado = (String) modeloEspera.getValueAt(row, 3);
				txtEmailEspera.setText(emailEsperaSeleccionado);
			}
		});
		panel.add(new JScrollPane(tablaEspera), BorderLayout.CENTER);

		JPanel formPanel = new JPanel(new GridLayout(1, 2, 5, 5));
		formPanel.add(new JLabel("Email del Cliente:"));
		txtEmailEspera = new JTextField();
		formPanel.add(txtEmailEspera);

		JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 5));

		JButton btnAgregar = new JButton("Agregar a Espera");
		btnAgregar.addActionListener(e -> {
			try {
				String email = txtEmailEspera.getText().trim();
				barberia.agregarAListaEspera(email);
				actualizarTablaEspera();
				txtEmailEspera.setText("");
				emailEsperaSeleccionado = null;
				JOptionPane.showMessageDialog(this, "Cliente agregado a la lista de espera");
			} catch (Exception ex) {
				JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
			}
		});
		btnPanel.add(btnAgregar);

		JButton btnEliminar = new JButton("Eliminar de Espera");
		btnEliminar.addActionListener(e -> {
			try {
				String email = txtEmailEspera.getText().trim();
				if (email.isEmpty() && emailEsperaSeleccionado != null) {
					email = emailEsperaSeleccionado;
				}
				if (email.isEmpty()) {
					JOptionPane.showMessageDialog(this, "Seleccione o ingrese el email del cliente a retirar");
					return;
				}
				barberia.eliminarDeListaEspera(email);
				actualizarTablaEspera();
				txtEmailEspera.setText("");
				emailEsperaSeleccionado = null;
				JOptionPane.showMessageDialog(this, "Cliente retirado de la lista de espera");
			} catch (Exception ex) {
				JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
			}
		});
		btnPanel.add(btnEliminar);

		JButton btnActualizar = new JButton("Actualizar");
		btnActualizar.addActionListener(e -> actualizarTablaEspera());
		btnPanel.add(btnActualizar);

		JButton btnLimpiar = new JButton("Limpiar");
		btnLimpiar.addActionListener(e -> {
			txtEmailEspera.setText("");
			emailEsperaSeleccionado = null;
			tablaEspera.clearSelection();
		});
		btnPanel.add(btnLimpiar);

		JPanel southPanel = new JPanel(new BorderLayout());
		southPanel.add(formPanel, BorderLayout.CENTER);
		southPanel.add(btnPanel, BorderLayout.SOUTH);
		panel.add(southPanel, BorderLayout.SOUTH);

		return panel;
	}

	private void actualizarTablaEspera() {
		modeloEspera.setRowCount(0);
		try {
			ArrayList<String[]> lista = barberia.mostrarListaEspera();
			for (int i = 0; i < lista.size(); i++) {
				modeloEspera.addRow(lista.get(i));
			}
		} catch (Exception e) {
		}
	}

	// =========================================================================
	// 6. PANEL HORARIO DE ATENCIÓN
	// =========================================================================
	private JPanel crearPanelHorario() {
		JPanel panel = new JPanel(new BorderLayout(10, 10));
		panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

		JPanel diasPanel = new JPanel(new GridLayout(7, 4, 10, 10));
		String[] nombresDias = HorarioAtencion.NOMBRES_DIAS;

		for (int i = 0; i < 7; i++) {
			JLabel lblDia = new JLabel(nombresDias[i] + ":");
			chkAbierto[i] = new JCheckBox("Abierto");
			spnInicio[i] = new JSpinner(new SpinnerNumberModel(8, 0, 23, 1));
			spnFin[i] = new JSpinner(new SpinnerNumberModel(18, 0, 24, 1));

			// Al desmarcar abierto se pueden deshabilitar los spinners
			final int diaIndex = i;
			chkAbierto[i].addActionListener(e -> {
				boolean enc = chkAbierto[diaIndex].isSelected();
				spnInicio[diaIndex].setEnabled(enc);
				spnFin[diaIndex].setEnabled(enc);
			});

			JPanel fila = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 2));
			fila.add(lblDia);
			fila.add(chkAbierto[i]);
			fila.add(new JLabel("Inicio:"));
			fila.add(spnInicio[i]);
			fila.add(new JLabel(":00  Fin:"));
			fila.add(spnFin[i]);
			fila.add(new JLabel(":00"));

			diasPanel.add(fila);
		}

		panel.add(new JScrollPane(diasPanel), BorderLayout.CENTER);

		JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));

		JButton btnGuardar = new JButton("Guardar Horario");
		btnGuardar.addActionListener(e -> {
			try {
				for (int i = 0; i < 7; i++) {
					boolean abierto = chkAbierto[i].isSelected();
					int inicio = (Integer) spnInicio[i].getValue();
					int fin = (Integer) spnFin[i].getValue();
					barberia.setHorario(i, abierto, inicio, fin);
				}
				JOptionPane.showMessageDialog(this, "Horario de atención actualizado exitosamente");
			} catch (Exception ex) {
				JOptionPane.showMessageDialog(this, ex.getMessage(), "Error al Configurar Horario", JOptionPane.ERROR_MESSAGE);
			}
		});
		btnPanel.add(btnGuardar);

		JButton btnRecargar = new JButton("Recargar Horario");
		btnRecargar.addActionListener(e -> cargarValoresHorario());
		btnPanel.add(btnRecargar);

		panel.add(btnPanel, BorderLayout.SOUTH);

		return panel;
	}

	private void cargarValoresHorario() {
		try {
			HorarioAtencion h = barberia.getHorario();
			boolean[] abierto = h.getAbierto();
			int[] inicio = h.getHoraInicio();
			int[] fin = h.getHoraFin();

			for (int i = 0; i < 7; i++) {
				if (chkAbierto[i] != null) {
					chkAbierto[i].setSelected(abierto[i]);
					spnInicio[i].setValue(inicio[i]);
					spnFin[i].setValue(fin[i]);
					spnInicio[i].setEnabled(abierto[i]);
					spnFin[i].setEnabled(abierto[i]);
				}
			}
		} catch (Exception e) {
		}
	}

	public static void main(String[] args) {
		SwingUtilities.invokeLater(() -> {
			new Interfaz().setVisible(true);
		});
	}
}
