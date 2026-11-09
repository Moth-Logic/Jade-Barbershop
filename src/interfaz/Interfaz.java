package interfaz;

import java.awt.BorderLayout;
import java.awt.Cursor;
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

/**
 * Ventana principal de Jade Barbershop (capa de vista, Swing).
 *
 * La ventana está dividida en seis pestañas: Clientes, Tipos de Servicio, Citas,
 * Calendario, Lista de Espera y Horario. Cada pestaña tiene una tabla con los datos y un
 * formulario con botones; todos los botones delegan la lógica en la clase Barberia y
 * aquí solo se muestran los resultados o los mensajes de error.
 *
 * Detalle importante: el envío de correos se ejecuta en un hilo aparte, porque hablar
 * con el servidor SMTP tarda segundos y congelaría la ventana si se hiciera en el hilo
 * de la interfaz (EDT).
 */
public class Interfaz extends JFrame {

	/** Versión de la clase; la ventana no se serializa, pero se mantiene por convención. */
	private static final long serialVersionUID = 1L;

	/** Acceso a toda la lógica de negocio y a la persistencia. */
	private Barberia barberia;

	/** Contenedor de las seis pestañas de la ventana. */
	private JTabbedPane tabbedPane;

	// ------------------------- Componentes: Clientes -------------------------

	/** Tabla con los clientes registrados. */
	private JTable tablaClientes;

	/** Modelo de datos de {@link #tablaClientes}. */
	private DefaultTableModel modeloClientes;

	/** Campo del nombre completo. */
	private JTextField txtNombreCliente;

	/** Campo del teléfono de 8 dígitos. */
	private JTextField txtTelCliente;

	/** Campo del correo, que identifica al cliente. */
	private JTextField txtEmailCliente;

	/** Correo del cliente seleccionado en la tabla, o {@code null} si no hay ninguno. */
	private String emailClienteSeleccionado;

	// --------------------- Componentes: Tipos de Servicio --------------------

	/** Tabla con el catálogo de servicios. */
	private JTable tablaServicios;

	/** Modelo de datos de {@link #tablaServicios}. */
	private DefaultTableModel modeloServicios;

	/** Campo del nombre del servicio. */
	private JTextField txtNombreServicio;

	/** Campo de la descripción del servicio. */
	private JTextField txtDescServicio;

	/** Campo del precio en colones. */
	private JTextField txtPrecioServicio;

	/** Campo de la duración en minutos. */
	private JTextField txtDuracionServicio;

	/** ID del servicio seleccionado en la tabla, o {@code null} si no hay ninguno. */
	private String idServicioSeleccionado;

	// --------------------------- Componentes: Citas --------------------------

	/** Tabla con todas las citas. */
	private JTable tablaCitas;

	/** Modelo de datos de {@link #tablaCitas}. */
	private DefaultTableModel modeloCitas;

	/** Campo de la fecha de la cita (YYYY-MM-DD). */
	private JTextField txtFechaCita;

	/** Campo de la hora de la cita (0 a 23). */
	private JTextField txtHoraCita;

	/** Campo del correo del cliente de la cita. */
	private JTextField txtEmailClienteCita;

	/** Campo del ID del servicio de la cita. */
	private JTextField txtIdServicioCita;

	/** ID de la cita seleccionada en la tabla, o {@code null} si no hay ninguna. */
	private String idCitaSeleccionada;

	// ------------------------ Componentes: Calendario ------------------------

	/** Tabla de consultas del calendario (día, semana o mes). */
	private JTable tablaCalendario;

	/** Modelo de datos de {@link #tablaCalendario}. */
	private DefaultTableModel modeloCalendario;

	/** Fecha usada para las consultas del calendario. */
	private JTextField txtFechaCalendario;

	// ----------------------- Componentes: Lista de Espera --------------------

	/** Tabla con los clientes en espera. */
	private JTable tablaEspera;

	/** Modelo de datos de {@link #tablaEspera}. */
	private DefaultTableModel modeloEspera;

	/** Campo del correo del cliente a agregar o retirar de la espera. */
	private JTextField txtEmailEspera;

	/** Correo del cliente seleccionado en la lista de espera. */
	private String emailEsperaSeleccionado;

	// -------------------- Componentes: Horario de Atención ------------------

	/** Casillas que indican si la barbería abre cada día (0 = lunes ... 6 = domingo). */
	private JCheckBox[] chkAbierto = new JCheckBox[7];

	/** Hora de apertura de cada día. */
	private JSpinner[] spnInicio = new JSpinner[7];

	/** Hora de cierre de cada día. */
	private JSpinner[] spnFin = new JSpinner[7];

	/** Arma la ventana y carga los datos guardados en disco (si existen). */
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

	/**
	 * Crea la ventana, las seis pestañas y el guardado al cerrar.
	 * Se llama una sola vez desde el constructor.
	 */
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
				// Guardado explícito al cerrar, además del autoguardado que hace Barberia
				// después de cada cambio.
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

	/** Rellena todas las tablas y el horario con lo que se cargó del disco. */
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
	/** Pestaña 1: alta, modificación, borrado y búsqueda de clientes por correo. */
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
		// Al seleccionar una fila se copian sus datos al formulario para poder editarlos.
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
				mostrarExito("Cliente creado exitosamente");
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
				mostrarExito("Cliente modificado exitosamente");
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

	/** Selecciona en la tabla la fila del cliente con ese correo (si está visible). */
	private void seleccionarFilaCliente(String email) {
		for (int i = 0; i < modeloClientes.getRowCount(); i++) {
			if (email.equalsIgnoreCase((String) modeloClientes.getValueAt(i, 2))) {
				tablaClientes.setRowSelectionInterval(i, i);
				break;
			}
		}
	}

	/** Vuelve a llenar la tabla de clientes desde la capa de control. */
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

	/** Vacía el formulario y olvida el cliente seleccionado. */
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
	/** Pestaña 2: catálogo de servicios (precio y duración en minutos). */
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
				mostrarExito("Tipo de servicio creado exitosamente");
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
				mostrarExito("Tipo de servicio modificado exitosamente");
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

		JButton btnBuscar = new JButton("Buscar");
		btnBuscar.addActionListener(e -> {
			try {
				String idBuscar = JOptionPane.showInputDialog(this, "Ingrese el ID del tipo de servicio:");
				if (idBuscar != null && !idBuscar.trim().isEmpty()) {
					logica.TipoServicio s = barberia.consultarTipoServicio(idBuscar.trim());
					idServicioSeleccionado = s.getId();
					txtNombreServicio.setText(s.getNombre());
					txtDescServicio.setText(s.getDescripcion());
					txtPrecioServicio.setText(String.valueOf(s.getPrecio()));
					txtDuracionServicio.setText(String.valueOf(s.getDuracionMinutos()));
				}
			} catch (Exception ex) {
				JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
			}
		});
		btnPanel.add(btnBuscar);

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

	/** Vuelve a llenar la tabla de servicios desde la capa de control. */
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

	/** Vacía el formulario de servicios y olvida el servicio seleccionado. */
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
	/**
	 * Pestaña 3: agenda de citas. La fecha se escribe como YYYY-MM-DD y la hora es un
	 * entero de 0 a 23; el servicio se elige por su ID.
	 */
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
		// Al elegir una cita se cargan fecha, hora, correo del cliente y ID del servicio.
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
				mostrarExito("Cita agendada exitosamente");
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
				mostrarExito("Cita modificada exitosamente");
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
					mostrarExito("Cita borrada exitosamente");
				}
			} catch (Exception ex) {
				JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
			}
		});
		btnPanel.add(btnBorrar);

		JButton btnBuscar = new JButton("Buscar Cita");
		btnBuscar.addActionListener(e -> {
			try {
				String idBuscar = JOptionPane.showInputDialog(this, "Ingrese el ID de la cita:");
				if (idBuscar != null && !idBuscar.trim().isEmpty()) {
					logica.Cita c = barberia.consultarCita(idBuscar.trim());
					JOptionPane.showMessageDialog(this, c.toString(), "Cita encontrada", JOptionPane.INFORMATION_MESSAGE);
				}
			} catch (Exception ex) {
				JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
			}
		});
		btnPanel.add(btnBuscar);

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
				mostrarExito("Cita confirmada");
			} catch (Exception ex) {
				JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
			}
		});
		btnPanel.add(btnConfirmar);

		// Los dos botones de correo solo piden la cita seleccionada; el envío real se hace
		// en segundo plano (ver enviarNotificacion) para no bloquear la ventana.
		JButton btnEnviarConfirmacion = new JButton("Enviar Confirmación");
		btnEnviarConfirmacion.addActionListener(e -> {
			if (idCitaSeleccionada == null) {
				JOptionPane.showMessageDialog(this, "Seleccione una cita de la tabla");
				return;
			}
			enviarNotificacion(idCitaSeleccionada, true);
		});
		btnPanel.add(btnEnviarConfirmacion);

		JButton btnEnviarRecordatorio = new JButton("Enviar Recordatorio");
		btnEnviarRecordatorio.addActionListener(e -> {
			if (idCitaSeleccionada == null) {
				JOptionPane.showMessageDialog(this, "Seleccione una cita de la tabla");
				return;
			}
			enviarNotificacion(idCitaSeleccionada, false);
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

	/** Vuelve a llenar la tabla de citas desde la capa de control. */
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

	/** Restablece el formulario de citas a sus valores por defecto. */
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
	/** Pestaña 4: consultas de citas por día, por semana, por mes o las de hoy. */
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

	/**
	 * Muestra las citas recibidas en la tabla del calendario y avisa si no hay ninguna.
	 *
	 * @param citas filas devueltas por las consultas de Barberia
	 */
	private void mostrarCitasEnCalendario(ArrayList<String[]> citas) {
		modeloCalendario.setRowCount(0);
		for (int i = 0; i < citas.size(); i++) {
			modeloCalendario.addRow(citas.get(i));
		}
		if (citas.isEmpty()) {
			JOptionPane.showMessageDialog(this, "No se encontraron citas agendadas para el período seleccionado.", "Calendario", JOptionPane.INFORMATION_MESSAGE);
		}
	}

	/** Refresca el calendario con las citas de la fecha actual. */
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
	/** Pestaña 5: clientes que esperan un espacio disponible. */
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
				mostrarExito("Cliente agregado a la lista de espera");
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
				mostrarExito("Cliente retirado de la lista de espera");
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

	/** Vuelve a llenar la tabla de la lista de espera. */
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
	/** Pestaña 6: días abiertos y horas de apertura/cierre de cada día. */
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
				mostrarExito("Horario de atención actualizado exitosamente");
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

	/**
	 * Copia a los controles de la pestaña Horario los valores guardados en Barberia
	 * y deshabilita las horas de los días cerrados.
	 */
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

	/**
	 * Envía el correo de confirmación o de recordatorio en un hilo aparte.
	 *
	 * Mientras el servidor SMTP responde, la ventana sigue usable (el cursor cambia a
	 * reloj de arena). Al terminar, la tabla y los diálogos se actualizan en el hilo de
	 * la interfaz con SwingUtilities.invokeLater.
	 */
	private void enviarNotificacion(String idCita, boolean esConfirmacion) {
		setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

		Thread hilo = new Thread(() -> {
			try {
				String respuesta = esConfirmacion
						? barberia.enviarConfirmacion(idCita)
						: barberia.enviarRecordatorio(idCita);

				SwingUtilities.invokeLater(() -> {
					setCursor(Cursor.getDefaultCursor());
					actualizarTablaCitas();
					JOptionPane.showMessageDialog(this, respuesta, "Notificación", JOptionPane.INFORMATION_MESSAGE);
				});
			} catch (Exception ex) {
				SwingUtilities.invokeLater(() -> {
					setCursor(Cursor.getDefaultCursor());
					JOptionPane.showMessageDialog(this, ex.getMessage(),
							"Error al enviar el correo", JOptionPane.ERROR_MESSAGE);
				});
			}
		}, "envio-correo");
		hilo.start();
	}

	/**
	 * Muestra un mensaje de éxito y, si el guardado automático falló, añade la advertencia.
	 * El cambio igual quedó aplicado en memoria, así que no se obliga a repetir la acción.
	 */
	private void mostrarExito(String mensaje) {
		String errorGuardado = barberia.getErrorGuardado();
		if (errorGuardado == null) {
			JOptionPane.showMessageDialog(this, mensaje);
			return;
		}
		barberia.limpiarErrorGuardado();
		JOptionPane.showMessageDialog(this,
				mensaje + "\n\nAdvertencia: el cambio se aplicó, pero no se pudo guardar en disco:\n" + errorGuardado,
				"Guardado automático", JOptionPane.WARNING_MESSAGE);
	}

	/**
	 * Punto de entrada de la aplicación: crea y muestra la ventana en el hilo de la
	 * interfaz (EDT), que es lo que exige Swing.
	 *
	 * @param args argumentos de línea de comandos (no se usan)
	 */
	public static void main(String[] args) {
		SwingUtilities.invokeLater(() -> {
			new Interfaz().setVisible(true);
		});
	}
}
