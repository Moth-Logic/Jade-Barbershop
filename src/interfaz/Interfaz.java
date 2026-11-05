package interfaz;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import control.Barberia;

public class Interfaz_parte1 extends JFrame {

	private Barberia barberia;
	private JTabbedPane tabbedPane;

	private JTable tablaClientes;
	private DefaultTableModel modeloClientes;
	private JTextField txtNombreCliente;
	private JTextField txtTelCliente;
	private JTextField txtEmailCliente;
	private String emailClienteSeleccionado;

	private JTable tablaServicios;
	private DefaultTableModel modeloServicios;
	private JTextField txtNombreServicio;
	private JTextField txtDescServicio;
	private JTextField txtPrecioServicio;
	private JTextField txtDuracionServicio;
	private String idServicioSeleccionado;

	public Interfaz_parte1() {
		barberia = new Barberia("Jade Barbershop");
		try {
			barberia.cargar();
		} catch (Exception e) {
		}
		initComponents();
	}

	private void initComponents() {
		setTitle("Jade Barbershop");
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setBounds(100, 100, 900, 600);
		setLocationRelativeTo(null);

		tabbedPane = new JTabbedPane();
		getContentPane().add(tabbedPane, BorderLayout.CENTER);

		tabbedPane.addTab("Clientes", crearPanelClientes());
		tabbedPane.addTab("Tipos de Servicio", crearPanelServicios());

		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				try {
					barberia.guardar();
				} catch (Exception ex) {
					JOptionPane.showMessageDialog(null, "Error al guardar: " + ex.getMessage());
				}
			}
		});
	}

	private JPanel crearPanelClientes() {
		JPanel panel = new JPanel(new BorderLayout(5, 5));
		panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

		modeloClientes = new DefaultTableModel(
			new String[]{"Nombre", "Telefono", "Email"}, 0) {
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
		formPanel.add(new JLabel("Nombre:"));
		txtNombreCliente = new JTextField();
		formPanel.add(txtNombreCliente);
		formPanel.add(new JLabel("Telefono:"));
		txtTelCliente = new JTextField();
		formPanel.add(txtTelCliente);
		formPanel.add(new JLabel("Email:"));
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
					"Seguro que desea borrar este cliente?",
					"Confirmar", JOptionPane.YES_NO_OPTION);
				if (opcion == JOptionPane.YES_OPTION) {
					barberia.borrarCliente(emailClienteSeleccionado);
					actualizarTablaClientes();
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

	private void actualizarTablaClientes() {
		modeloClientes.setRowCount(0);
		try {
			ArrayList<String[]> lista = barberia.listarClientes();
			for (int i = 0; i < lista.size(); i++) {
				modeloClientes.addRow(lista.get(i));
			}
		} catch (Exception e) {
		}
	}

	private void limpiarCamposCliente() {
		txtNombreCliente.setText("");
		txtTelCliente.setText("");
		txtEmailCliente.setText("");
		emailClienteSeleccionado = null;
		tablaClientes.clearSelection();
	}

	private JPanel crearPanelServicios() {
		JPanel panel = new JPanel(new BorderLayout(5, 5));
		panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

		modeloServicios = new DefaultTableModel(
			new String[]{"ID", "Nombre", "Descripcion", "Precio", "Duracion (min)"}, 0) {
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
		formPanel.add(new JLabel("Descripcion:"));
		txtDescServicio = new JTextField();
		formPanel.add(txtDescServicio);
		formPanel.add(new JLabel("Precio:"));
		txtPrecioServicio = new JTextField();
		formPanel.add(txtPrecioServicio);
		formPanel.add(new JLabel("Duracion (min):"));
		txtDuracionServicio = new JTextField();
		formPanel.add(txtDuracionServicio);

		JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 5));

		JButton btnCrear = new JButton("Crear");
		btnCrear.addActionListener(e -> {
			try {
				barberia.crearTipoServicio(
					txtNombreServicio.getText(),
					txtDescServicio.getText(),
					Double.parseDouble(txtPrecioServicio.getText()),
					Integer.parseInt(txtDuracionServicio.getText()));
				actualizarTablaServicios();
				limpiarCamposServicio();
				JOptionPane.showMessageDialog(this, "Tipo de servicio creado");
			} catch (NumberFormatException ex) {
				JOptionPane.showMessageDialog(this, "Precio o duracion invalidos", "Error", JOptionPane.ERROR_MESSAGE);
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
					Double.parseDouble(txtPrecioServicio.getText()),
					Integer.parseInt(txtDuracionServicio.getText()));
				actualizarTablaServicios();
				limpiarCamposServicio();
				JOptionPane.showMessageDialog(this, "Tipo de servicio modificado");
			} catch (NumberFormatException ex) {
				JOptionPane.showMessageDialog(this, "Precio o duracion invalidos", "Error", JOptionPane.ERROR_MESSAGE);
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
					"Seguro que desea borrar este servicio?",
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

	public static void main(String[] args) {
		SwingUtilities.invokeLater(() -> {
			new Interfaz_parte1().setVisible(true);
		});
	}
}
