package com.daf.view.cliente;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.io.FileInputStream;
import java.sql.Connection;
import java.util.List;
import java.util.Properties;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import com.daf.controller.Cliente;
import com.daf.view.MenuPrincipal;

public class ClienteView extends JPanel {
    private Properties props;
    private Connection conn;
    private MenuPrincipal menuPrincipal;
    private Cliente clienteDP;

    private JTextField txtBuscar;
    private JButton btnRegresar, btnBuscar, btnCrear;
    private JTable table;
    private DefaultTableModel tableModel;
    
    private JLabel lblPaginacion;
    private JButton btnAnterior, btnSiguiente;
    private int paginaActual = 1;
    private int REGISTROS_POR_PAGINA = 20;
    private List<Cliente> todosLosRegistros;
    private List<Cliente> registrosFiltrados;

    public ClienteView(Connection conn, MenuPrincipal menuPrincipal) {
        this.conn = conn;
        this.menuPrincipal = menuPrincipal;
        this.clienteDP = new Cliente(conn);
        loadProperties();
        initComponents();
        cargarDatos();
    }

    private void loadProperties() {
        props = new Properties();
        try (FileInputStream fis = new FileInputStream("src/main/resources/config.properties")) {
            props.load(fis);
            try { REGISTROS_POR_PAGINA = Integer.parseInt(props.getProperty("REGISTROS_POR_PAGINA")); } catch (Exception e){}
        } catch (Exception e) {}
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        setOpaque(false);

        // SUPERIOR
        JPanel panelSuperior = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        panelSuperior.setBackground(new Color(255, 178, 102));
        btnRegresar = new JButton("Regresar");
        btnRegresar.setPreferredSize(new Dimension(120, 35));
        btnRegresar.addActionListener(e -> menuPrincipal.regresarMenu());
        panelSuperior.add(btnRegresar);
        add(panelSuperior, BorderLayout.NORTH);

        // BUSQUEDA
        JPanel panelBusqueda = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        panelBusqueda.setBackground(new Color(255, 239, 204));
        txtBuscar = new JTextField(30);
        panelBusqueda.add(txtBuscar);
        
        btnBuscar = new JButton("Buscar");
        btnBuscar.setBackground(new Color(255, 178, 102));
        btnBuscar.addActionListener(e -> buscarCliente());
        panelBusqueda.add(btnBuscar);

        btnCrear = new JButton("Nuevo Cliente");
        btnCrear.setBackground(new Color(255, 178, 102));
        btnCrear.addActionListener(e -> abrirFormularioCrear());
        panelBusqueda.add(btnCrear);

        // TABLA (SIN APELLIDO)
        JPanel panelPrincipal = new JPanel(new BorderLayout(10, 10));
        panelPrincipal.setBackground(new Color(255, 239, 204));
        panelPrincipal.add(panelBusqueda, BorderLayout.NORTH);

        // Columnas
        String[] columnas = {"Código", "Cédula/RUC", "Nombre/Razón Social", "Teléfono", "Celular", "Acciones"};
        tableModel = new DefaultTableModel(columnas, 0) {
            public boolean isCellEditable(int row, int column) { return column == 5; }
        };

        table = new JTable(tableModel);
        table.setRowHeight(40);
        table.getColumnModel().getColumn(5).setCellRenderer(new ButtonRenderer());
        table.getColumnModel().getColumn(5).setCellEditor(new ButtonEditor(new JCheckBox()));

        JScrollPane scrollPane = new JScrollPane(table);
        panelPrincipal.add(scrollPane, BorderLayout.CENTER);
        add(panelPrincipal, BorderLayout.CENTER);

        // PAGINACION
        JPanel panelPaginacion = new JPanel(new FlowLayout(FlowLayout.CENTER));
        panelPaginacion.setBackground(new Color(255, 239, 204));
        btnAnterior = new JButton("<");
        btnAnterior.addActionListener(e -> { if(paginaActual > 1) { paginaActual--; actualizarTabla(); }});
        
        lblPaginacion = new JLabel("1/1");
        
        btnSiguiente = new JButton(">");
        btnSiguiente.addActionListener(e -> { 
            int total = (int) Math.ceil((double) registrosFiltrados.size() / REGISTROS_POR_PAGINA);
            if(paginaActual < total) { paginaActual++; actualizarTabla(); }
        });

        panelPaginacion.add(btnAnterior);
        panelPaginacion.add(lblPaginacion);
        panelPaginacion.add(btnSiguiente);
        add(panelPaginacion, BorderLayout.SOUTH);
    }

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
                c.getCliTelefono(),
                c.getCliCelular(),
                c.getCliCodigo()
            };
            tableModel.addRow(fila);
        }
        lblPaginacion.setText(paginaActual + "/" + totalPaginas);
    }

    private void buscarCliente() {
        String txt = txtBuscar.getText().trim();
        if (txt.isEmpty()) registrosFiltrados = todosLosRegistros;
        else registrosFiltrados = clienteDP.getByNameDP(txt);
        paginaActual = 1;
        actualizarTabla();
    }

    private void abrirFormularioCrear() {
        ClienteForm form = new ClienteForm(conn, this, null);
        menuPrincipal.registrarVista("CLIENTE_FORM", form);
        menuPrincipal.mostrarVista("CLIENTE_FORM");
    }

    private void abrirFormularioEditar(String cliCodigo) {
        for(Cliente c : todosLosRegistros) {
            if(c.getCliCodigo().equals(cliCodigo)) {
                ClienteForm form = new ClienteForm(conn, this, c);
                menuPrincipal.registrarVista("CLIENTE_FORM", form);
                menuPrincipal.mostrarVista("CLIENTE_FORM");
                break;
            }
        }
    }

    private void eliminarCliente(String cliCodigo) {
        if(JOptionPane.showConfirmDialog(this, "¿Eliminar?", "Confirme", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            Cliente c = new Cliente(conn);
            c.setCliCodigo(cliCodigo);
            if(c.deleteDP()) {
                JOptionPane.showMessageDialog(this, "Eliminado");
                cargarDatos();
            } else {
                JOptionPane.showMessageDialog(this, "Error al eliminar");
            }
        }
    }

    public void refrescarDatos() { cargarDatos(); }

    class ButtonRenderer extends JPanel implements TableCellRenderer {
        public ButtonRenderer() {
            setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
            setBackground(new Color(255, 178, 102));
            add(new JButton("Editar"));
            add(new JButton("Borrar"));
        }
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) { return this; }
    }

    class ButtonEditor extends DefaultCellEditor {
        JPanel panel; JButton btnEd, btnEl; String codigo;
        public ButtonEditor(JCheckBox cb) {
            super(cb);
            panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 5));
            panel.setBackground(new Color(255, 178, 102));
            btnEd = new JButton("Editar");
            btnEd.addActionListener(e -> { fireEditingStopped(); abrirFormularioEditar(codigo); });
            btnEl = new JButton("Borrar");
            btnEl.addActionListener(e -> { fireEditingStopped(); eliminarCliente(codigo); });
            panel.add(btnEd); panel.add(btnEl);
        }
        public Component getTableCellEditorComponent(JTable t, Object v, boolean s, int r, int c) { codigo=(String)v; return panel; }
        public Object getCellEditorValue() { return codigo; }
    }
}