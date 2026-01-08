package com.daf.view.cliente;

import java.awt.*;
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
    private Connection conn;
    private MenuPrincipal menuPrincipal;
    private Cliente clienteDP;
    private JTextField txtBuscar;
    private JTable table;
    private DefaultTableModel tableModel;
    private JLabel lblPaginacion;
    private int paginaActual = 1;
    private int REGISTROS_POR_PAGINA = 20;
    private List<Cliente> registrosFiltrados;
    private Color naranjaDaf = new Color(255, 178, 102); // Color #FFB266

    public ClienteView(Connection conn, MenuPrincipal menuPrincipal) {
        this.conn = conn; this.menuPrincipal = menuPrincipal;
        this.clienteDP = new Cliente(conn);
        loadProperties();
        initComponents();
        cargarDatos();
    }

    private void loadProperties() {
        Properties props = new Properties();
        try (FileInputStream fis = new FileInputStream("src/main/resources/config.properties")) {
            props.load(fis);
            REGISTROS_POR_PAGINA = Integer.parseInt(props.getProperty("REGISTROS_POR_PAGINA", "20"));
        } catch (Exception e) {}
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        
        // PANEL SUPERIOR
        JPanel panelSup = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panelSup.setBackground(naranjaDaf);
        JButton btnRegresar = new JButton("Regresar");
        btnRegresar.addActionListener(e -> menuPrincipal.regresarMenu());
        panelSup.add(btnRegresar);
        add(panelSup, BorderLayout.NORTH);

        // PANEL BÚSQUEDA
        JPanel panelBusca = new JPanel(new FlowLayout(FlowLayout.LEFT));
        txtBuscar = new JTextField(25);
        
        JButton btnBusca = new JButton("Buscar");
        btnBusca.setBackground(naranjaDaf);
        btnBusca.setFocusPainted(false);
        btnBusca.addActionListener(e -> buscarCliente());
        
        JButton btnNuevo = new JButton("Nuevo Cliente");
        btnNuevo.setBackground(naranjaDaf);
        btnNuevo.setFocusPainted(false);
        btnNuevo.addActionListener(e -> abrirFormularioCrear());
        
        panelBusca.add(txtBuscar); panelBusca.add(btnBusca); panelBusca.add(btnNuevo);

        // CONFIGURACIÓN DE TABLA
        String[] cols = {"Código", "Cédula", "Nombre", "Apellido", "Teléfono", "Celular", "Acciones"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return c == 6; }
        };
        table = new JTable(tableModel);
        table.setRowHeight(35);
        
        // Renderizador y Editor para la columna 6 (Acciones)
        table.getColumnModel().getColumn(6).setCellRenderer(new ButtonRenderer());
        table.getColumnModel().getColumn(6).setCellEditor(new ButtonEditor(new JCheckBox()));

        JPanel panelCentro = new JPanel(new BorderLayout());
        panelCentro.add(panelBusca, BorderLayout.NORTH);
        panelCentro.add(new JScrollPane(table), BorderLayout.CENTER);
        add(panelCentro, BorderLayout.CENTER);

        // PANEL PAGINACIÓN
        JPanel panelPag = new JPanel(new FlowLayout());
        JButton btnAnt = new JButton("<");
        btnAnt.addActionListener(e -> { if(paginaActual > 1){ paginaActual--; actualizarTabla(); }});
        lblPaginacion = new JLabel("1/1");
        JButton btnSig = new JButton(">");
        btnSig.addActionListener(e -> { 
            if(paginaActual < (int)Math.ceil((double)registrosFiltrados.size()/REGISTROS_POR_PAGINA)){ paginaActual++; actualizarTabla(); }
        });
        panelPag.add(btnAnt); panelPag.add(lblPaginacion); panelPag.add(btnSig);
        add(panelPag, BorderLayout.SOUTH);
    }

    private void cargarDatos() {
        registrosFiltrados = clienteDP.getAllDP();
        actualizarTabla();
    }

    private void actualizarTabla() {
        tableModel.setRowCount(0);
        int inicio = (paginaActual - 1) * REGISTROS_POR_PAGINA;
        int fin = Math.min(inicio + REGISTROS_POR_PAGINA, registrosFiltrados.size());
        for (int i = inicio; i < fin; i++) {
            Cliente c = registrosFiltrados.get(i);
            tableModel.addRow(new Object[]{
                c.getCliCodigo(), c.getCliCedula(), c.getCliNombre(), 
                c.getCliApellido(), c.getCliTelefono(), c.getCliCelular(), 
                c.getCliCodigo()
            });
        }
        int total = (int) Math.ceil((double) registrosFiltrados.size() / REGISTROS_POR_PAGINA);
        lblPaginacion.setText(paginaActual + "/" + (total == 0 ? 1 : total));
    }

    private void buscarCliente() {
        registrosFiltrados = clienteDP.getByNameDP(txtBuscar.getText().trim());
        paginaActual = 1; actualizarTabla();
    }

    private void abrirFormularioCrear() {
        menuPrincipal.registrarVista("CLIENTE_FORM", new ClienteForm(conn, this, null));
        menuPrincipal.mostrarVista("CLIENTE_FORM");
    }

    public void refrescarDatos() { cargarDatos(); }

    // RENDERIZADOR DE BOTONES (Lo que se ve)
    class ButtonRenderer extends JPanel implements TableCellRenderer {
        public ButtonRenderer() {
            setLayout(new FlowLayout(FlowLayout.CENTER, 5, 0));
            setOpaque(true);
            JButton bEd = new JButton("Editar"); // Nombre cambiado
            JButton bEl = new JButton("Eliminar"); // Nombre cambiado
            bEd.setBackground(naranjaDaf);
            bEl.setBackground(naranjaDaf);
            add(bEd); add(bEl);
        }
        @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) { 
            setBackground(s ? t.getSelectionBackground() : t.getBackground());
            return this; 
        }
    }

    // EDITOR DE BOTONES (Lo que funciona al hacer clic)
    class ButtonEditor extends DefaultCellEditor {
        JPanel p; String cod;
        public ButtonEditor(JCheckBox cb) {
            super(cb); p = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
            JButton bEd = new JButton("Editar"); // Nombre cambiado
            JButton bEl = new JButton("Eliminar"); // Nombre cambiado
            bEd.setBackground(naranjaDaf);
            bEl.setBackground(naranjaDaf);
            bEd.addActionListener(e -> { fireEditingStopped(); editar(cod); });
            bEl.addActionListener(e -> { fireEditingStopped(); eliminar(cod); });
            p.add(bEd); p.add(bEl);
        }
        @Override public Component getTableCellEditorComponent(JTable t, Object v, boolean s, int r, int c) { 
            cod = (String)v; p.setBackground(t.getSelectionBackground());
            return p; 
        }
        @Override public Object getCellEditorValue() { return cod; }
        
        private void editar(String id) {
            for(Cliente c : registrosFiltrados) if(c.getCliCodigo().equals(id)) {
                menuPrincipal.registrarVista("CLIENTE_FORM", new ClienteForm(conn, ClienteView.this, c));
                menuPrincipal.mostrarVista("CLIENTE_FORM");
                break;
            }
        }
        private void eliminar(String id) {
            if(JOptionPane.showConfirmDialog(null, "¿Está seguro de eliminar este cliente?", "Confirmar", JOptionPane.YES_NO_OPTION) == 0) {
                Cliente c = new Cliente(conn); c.setCliCodigo(id);
                if(c.deleteDP()) cargarDatos();
            }
        }
    }
}