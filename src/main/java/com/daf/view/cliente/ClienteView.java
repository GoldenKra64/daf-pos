package com.daf.view.cliente;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.io.FileInputStream;
import java.sql.Connection;
import java.util.List;
import java.util.Properties;

import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;

import com.daf.controller.Cliente;
import com.daf.view.MenuPrincipal;

public class ClienteView extends JPanel {
    private Properties props;

    private Connection conn;
    private MenuPrincipal menuPrincipal;
    private Cliente clienteDP;

    // Componentes de la interfaz
    private JTextField txtBuscar;
    private JButton btnRegresar, btnBuscar, btnCrear;
    private JTable table;
    private DefaultTableModel tableModel;
    
    // Componentes de paginación (Igual que Materia Prima)
    private JLabel lblPaginacion;
    private JButton btnAnterior, btnSiguiente;

    // Variables de control de datos
    private int paginaActual = 1;
    private int REGISTROS_POR_PAGINA;
    private List<Cliente> todosLosRegistros;
    private List<Cliente> registrosFiltrados;

    public ClienteView(Connection conn, MenuPrincipal menuPrincipal) {
        this.conn = conn;
        this.menuPrincipal = menuPrincipal;
        this.clienteDP = new Cliente(conn);

        loadProperties();
        
        // Cargar configuración o usar defecto si falla
        try {
            REGISTROS_POR_PAGINA = Integer.parseInt(props.getProperty("REGISTROS_POR_PAGINA"));
        } catch (NumberFormatException e) {
            REGISTROS_POR_PAGINA = 20; 
        }

        initComponents();
        cargarDatos();
    }

    private void loadProperties() {
        props = new Properties();
        try (FileInputStream fis = new FileInputStream("src/main/resources/config.properties")) {
            props.load(fis);
        } catch (Exception e) {
            System.err.println("No se pudo cargar config.properties, usando valores por defecto.");
        }
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        setOpaque(false);

        // 1. PANEL SUPERIOR (Naranja con botón Regresar)
        JPanel panelSuperior = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        panelSuperior.setBackground(new Color(255, 178, 102));

        btnRegresar = new JButton("Regresar");
        btnRegresar.setPreferredSize(new Dimension(120, 35));
        btnRegresar.addActionListener(e -> regresarMenu());
        panelSuperior.add(btnRegresar);

        add(panelSuperior, BorderLayout.NORTH);

        // 2. PANEL DE BÚSQUEDA Y CREAR (Crema)
        JPanel panelBusqueda = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        panelBusqueda.setBackground(new Color(255, 239, 204));

        txtBuscar = new JTextField(40);
        txtBuscar.setPreferredSize(new Dimension(400, 30));
        panelBusqueda.add(txtBuscar);

        btnBuscar = new JButton("Buscar");
        btnBuscar.setBackground(new Color(255, 178, 102));
        btnBuscar.addActionListener(e -> buscarCliente());
        panelBusqueda.add(btnBuscar);

        btnCrear = new JButton("Nuevo Cliente");
        btnCrear.setBackground(new Color(255, 178, 102));
        btnCrear.addActionListener(e -> abrirFormularioCrear());
        panelBusqueda.add(btnCrear);

        // 3. PANEL CENTRAL (Contiene Busqueda + Tabla)
        JPanel panelPrincipal = new JPanel(new BorderLayout(10, 10));
        panelPrincipal.setBackground(new Color(255, 239, 204));
        panelPrincipal.add(panelBusqueda, BorderLayout.NORTH);

        // Tabla
        String[] columnas = {"Código", "Cédula", "Nombre", "Apellido", "Teléfono", "Acciones"};
        tableModel = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 5; // Solo la columna de botones es editable
            }
        };

        table = new JTable(tableModel);
        table.setRowHeight(40);
        
        // Configurar renderizador y editor para los botones de la tabla
        table.getColumnModel().getColumn(5).setCellRenderer(new ButtonRenderer());
        table.getColumnModel().getColumn(5).setCellEditor(new ButtonEditor(new JCheckBox()));

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.getViewport().setBackground(Color.WHITE);
        panelPrincipal.add(scrollPane, BorderLayout.CENTER);

        add(panelPrincipal, BorderLayout.CENTER);

        // 4. PANEL DE PAGINACIÓN (Inferior)
        JPanel panelPaginacion = new JPanel(new FlowLayout(FlowLayout.CENTER));
        panelPaginacion.setBackground(new Color(255, 239, 204));

        btnAnterior = new JButton("< Anterior");
        btnAnterior.addActionListener(e -> paginaAnterior());
        panelPaginacion.add(btnAnterior);

        lblPaginacion = new JLabel("1/1");
        // Intentar usar fuente de properties, sino default
        try {
            lblPaginacion.setFont(new Font(props.getProperty("FONT_FAMILY"), Font.PLAIN, Integer.parseInt(props.getProperty("FONT_SIZE"))));
        } catch (Exception e) {
            lblPaginacion.setFont(new Font("Arial", Font.PLAIN, 14));
        }
        panelPaginacion.add(lblPaginacion);

        btnSiguiente = new JButton("Siguiente >");
        btnSiguiente.addActionListener(e -> paginaSiguiente());
        panelPaginacion.add(btnSiguiente);

        add(panelPaginacion, BorderLayout.SOUTH);
    }

    // ================= LÓGICA DE DATOS Y PAGINACIÓN =================

    private void cargarDatos() {
        todosLosRegistros = clienteDP.getAllDP();
        registrosFiltrados = todosLosRegistros;
        paginaActual = 1;
        actualizarTabla();
    }

    private void actualizarTabla() {
        tableModel.setRowCount(0);

        int totalPaginas = (int) Math.ceil((double) registrosFiltrados.size() / REGISTROS_POR_PAGINA);
        if (totalPaginas == 0) totalPaginas = 1;

        int inicio = (paginaActual - 1) * REGISTROS_POR_PAGINA;
        int fin = Math.min(inicio + REGISTROS_POR_PAGINA, registrosFiltrados.size());

        for (int i = inicio; i < fin; i++) {
            Cliente c = registrosFiltrados.get(i);
            Object[] fila = {
                c.getCliCodigo(),
                c.getCliCedula(),
                c.getCliNombre(),
                c.getCliApellido(),
                c.getCliTelefono(),
                c.getCliCodigo() // Importante: pasamos el código para el botón
            };
            tableModel.addRow(fila);
        }

        lblPaginacion.setText(paginaActual + "/" + totalPaginas);
        btnAnterior.setEnabled(paginaActual > 1);
        btnSiguiente.setEnabled(paginaActual < totalPaginas);
    }

    private void buscarCliente() {
        String busqueda = txtBuscar.getText().trim();
        if (busqueda.isEmpty()) {
            registrosFiltrados = todosLosRegistros;
        } else {
            registrosFiltrados = clienteDP.getByNameDP(busqueda);
        }
        paginaActual = 1;
        actualizarTabla();
    }

    private void paginaAnterior() {
        if (paginaActual > 1) {
            paginaActual--;
            actualizarTabla();
        }
    }

    private void paginaSiguiente() {
        int totalPaginas = (int) Math.ceil((double) registrosFiltrados.size() / REGISTROS_POR_PAGINA);
        if (paginaActual < totalPaginas) {
            paginaActual++;
            actualizarTabla();
        }
    }

    // ================= ACCIONES DE BOTONES =================

    private void abrirFormularioCrear() {
        ClienteForm form = new ClienteForm(conn, this, null);
        menuPrincipal.registrarVista("CLIENTE_FORM", form);
        menuPrincipal.mostrarVista("CLIENTE_FORM");
    }

    private void abrirFormularioEditar(String cliCodigo) {
        Cliente clienteSeleccionado = null;
        for (Cliente c : todosLosRegistros) {
            if (c.getCliCodigo().equals(cliCodigo)) {
                clienteSeleccionado = c;
                break;
            }
        }

        if (clienteSeleccionado != null) {
            ClienteForm form = new ClienteForm(conn, this, clienteSeleccionado);
            menuPrincipal.registrarVista("CLIENTE_FORM", form);
            menuPrincipal.mostrarVista("CLIENTE_FORM");
        }
    }

    private void eliminarCliente(String cliCodigo) {
        int confirmacion = JOptionPane.showConfirmDialog(
            this,
            "¿Está seguro de eliminar este cliente?",
            "Confirmar eliminación",
            JOptionPane.YES_NO_OPTION
        );

        if (confirmacion == JOptionPane.YES_OPTION) {
            Cliente c = new Cliente(conn);
            c.setCliCodigo(cliCodigo);
            
            if (c.deleteDP()) {
                JOptionPane.showMessageDialog(this, "Cliente eliminado exitosamente");
                cargarDatos(); // Recargar desde base de datos
            } else {
                JOptionPane.showMessageDialog(this, "Error al eliminar el cliente", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void regresarMenu() {
        menuPrincipal.regresarMenu();
    }

    // Método público para actualizar desde fuera (ej: al guardar en form)
    public void refrescarDatos() {
        cargarDatos();
    }

    // ================= RENDERIZADORES DE TABLA (Igual a MateriaPrima) =================

    /* Muestra los botones visualmente en la tabla */
    class ButtonRenderer extends JPanel implements TableCellRenderer {
        private JButton btnActualizar;
        private JButton btnEliminar;

        public ButtonRenderer() {
            setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
            setBackground(new Color(255, 178, 102)); // Naranja de fondo al seleccionar celda

            btnActualizar = new JButton("Actualizar");
            btnActualizar.setBackground(new Color(255, 178, 102));
            add(btnActualizar);

            btnEliminar = new JButton("Eliminar");
            btnEliminar.setBackground(new Color(255, 178, 102));
            add(btnEliminar);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            return this;
        }
    }

    /* Maneja la acción de clic en los botones */
    class ButtonEditor extends DefaultCellEditor {
        private JPanel panel;
        private JButton btnActualizar;
        private JButton btnEliminar;
        private String cliCodigo;

        public ButtonEditor(JCheckBox checkBox) {
            super(checkBox);
            panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 5));
            panel.setBackground(new Color(255, 178, 102));

            btnActualizar = new JButton("Actualizar");
            btnActualizar.setBackground(new Color(255, 178, 102));
            btnActualizar.addActionListener(e -> {
                fireEditingStopped();
                abrirFormularioEditar(cliCodigo);
            });
            panel.add(btnActualizar);

            btnEliminar = new JButton("Eliminar");
            btnEliminar.setBackground(new Color(255, 178, 102));
            btnEliminar.addActionListener(e -> {
                fireEditingStopped();
                eliminarCliente(cliCodigo);
            });
            panel.add(btnEliminar);
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value,
                boolean isSelected, int row, int column) {
            cliCodigo = (String) value;
            return panel;
        }

        @Override
        public Object getCellEditorValue() {
            return cliCodigo;
        }
    }
}