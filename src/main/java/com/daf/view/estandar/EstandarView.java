package com.daf.view.estandar;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.util.List;
import java.util.Properties;

import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;

import com.daf.controller.Estandar;
import com.daf.view.MenuPrincipal;

public class EstandarView extends JPanel {
    private Connection conn;
    private MenuPrincipal menuPrincipal;
    private Estandar estandarDP;

    // Componentes de la interfaz
    private JTextField txtBuscar;
    private JButton btnRegresar, btnBuscar;
    private JTable table;
    private DefaultTableModel tableModel;
    private JLabel lblPaginacion;
    private JButton btnAnterior, btnSiguiente;

    // Variables de paginación
    private int paginaActual = 1;
    private int REGISTROS_POR_PAGINA;
    private List<Estandar> todosLosRegistros;
    private List<Estandar> registrosFiltrados;

    private Properties props = new Properties();

    public EstandarView(Connection conn, MenuPrincipal menuPrincipal) {
        this.conn = conn;
        this.menuPrincipal = menuPrincipal;
        this.estandarDP = new Estandar(conn);

        loadProperties();

        REGISTROS_POR_PAGINA = Integer.parseInt(props.getProperty("REGISTROS_POR_PAGINA"));
        initComponents();
        cargarDatos();
    }

    private void loadProperties() {
        try (FileInputStream fis = new FileInputStream("src/main/resources/config.properties")) {
            props.load(fis);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        setOpaque(false);

        // Panel superior con fondo naranja
        JPanel panelSuperior = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        panelSuperior.setBackground(new Color(255, 178, 102));

        btnRegresar = new JButton("Regresar");
        btnRegresar.setPreferredSize(new Dimension(120, 35));
        btnRegresar.addActionListener(e -> regresarMenu());
        panelSuperior.add(btnRegresar);

        add(panelSuperior, BorderLayout.NORTH);

        // Panel de búsqueda
        JPanel panelBusqueda = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        panelBusqueda.setBackground(new Color(255, 239, 204));

        txtBuscar = new JTextField(40);
        txtBuscar.setPreferredSize(new Dimension(400, 30));
        panelBusqueda.add(txtBuscar);

        btnBuscar = new JButton("Buscar");
        btnBuscar.setBackground(new Color(255, 178, 102));
        btnBuscar.addActionListener(e -> buscarEstandar());
        panelBusqueda.add(btnBuscar);

        JButton btnCrear = new JButton("Crear");
        btnCrear.setBackground(new Color(255, 178, 102));
        btnCrear.addActionListener(e -> abrirFormularioCrear());
        panelBusqueda.add(btnCrear);

        // Crear panel principal que contendrá búsqueda y tabla
        JPanel panelPrincipal = new JPanel(new BorderLayout(10, 10));
        panelPrincipal.setBackground(new Color(255, 239, 204));
        panelPrincipal.add(panelBusqueda, BorderLayout.NORTH);

        // Panel de tabla
        String[] columnas = {"Producto", "Cantidad Total", "Precio Total", "Estado", "Actualizar"};
        tableModel = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 4; // Solo la columna de actualizar es editable
            }
        };

        table = new JTable(tableModel);
        table.setRowHeight(40);
        table.getColumnModel().getColumn(4).setCellRenderer(new ButtonRenderer());
        table.getColumnModel().getColumn(4).setCellEditor(new ButtonEditor(new JCheckBox()));
        
        // Ajustar anchos de columnas
        table.getColumnModel().getColumn(0).setPreferredWidth(250); // Producto
        table.getColumnModel().getColumn(1).setPreferredWidth(120); // Cantidad
        table.getColumnModel().getColumn(2).setPreferredWidth(120); // Precio
        table.getColumnModel().getColumn(3).setPreferredWidth(100); // Estado
        table.getColumnModel().getColumn(4).setPreferredWidth(100); // Actualizar

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBackground(new Color(255, 178, 102));
        panelPrincipal.add(scrollPane, BorderLayout.CENTER);

        add(panelPrincipal, BorderLayout.CENTER);

        // Panel de paginación
        JPanel panelPaginacion = new JPanel(new FlowLayout(FlowLayout.CENTER));
        panelPaginacion.setBackground(new Color(255, 239, 204));

        btnAnterior = new JButton("< Anterior");
        btnAnterior.addActionListener(e -> paginaAnterior());
        panelPaginacion.add(btnAnterior);

        lblPaginacion = new JLabel("1/1");
        lblPaginacion.setFont(new Font(props.getProperty("FONT_FAMILY"), Font.BOLD, Integer.parseInt(props.getProperty("FONT_SIZE"))));
        panelPaginacion.add(lblPaginacion);

        btnSiguiente = new JButton("Siguiente >");
        btnSiguiente.addActionListener(e -> paginaSiguiente());
        panelPaginacion.add(btnSiguiente);

        add(panelPaginacion, BorderLayout.SOUTH);
    }

    private void cargarDatos() {
        todosLosRegistros = estandarDP.getAllDP();
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
            Estandar est = registrosFiltrados.get(i);
            Object[] fila = {
                est.getNombreProducto(),
                est.getEstQtyTotal(),
                String.format("$%.2f", est.getEstPrecioTotal()),
                est.getEstadoDescripcion(),
                est.getEstCod() // Guardamos el código para el botón
            };
            tableModel.addRow(fila);
        }

        lblPaginacion.setText(paginaActual + "/" + totalPaginas);
        btnAnterior.setEnabled(paginaActual > 1);
        btnSiguiente.setEnabled(paginaActual < totalPaginas);
    }

    private void buscarEstandar() {
        String busqueda = txtBuscar.getText().trim();
        if (busqueda.isEmpty()) {
            registrosFiltrados = todosLosRegistros;
        } else {
            registrosFiltrados = estandarDP.getByNameDP(busqueda);
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

    private void abrirFormularioCrear() {
        // Crear nuevo estándar vacío
        Estandar nuevoEstandar = new Estandar(conn);
        MenuPrincipal menu = menuPrincipal;
        EstandarForm form = new EstandarForm(conn, this, nuevoEstandar);
        menu.registrarVista("ESTANDAR_FORM", form);
        menu.mostrarVista("ESTANDAR_FORM");
    }

    private void abrirFormularioEditar(String estCod) {
        // Buscar el estándar
        Estandar est = null;
        for (Estandar e : todosLosRegistros) {
            if (e.getEstCod().equals(estCod)) {
                est = e;
                break;
            }
        }

        if (est != null) {
            est.cargarDetallesDP(); // Cargar los detalles
            MenuPrincipal menu = menuPrincipal;
            EstandarForm form = new EstandarForm(conn, this, est);
            menu.registrarVista("ESTANDAR_FORM", form);
            menu.mostrarVista("ESTANDAR_FORM");
        }
    }

    private void regresarMenu() {
        menuPrincipal.regresarMenu();
    }

    public void refrescarDatos() {
        cargarDatos();
    }

    // Renderer para mostrar botón en la tabla
    class ButtonRenderer extends JPanel implements TableCellRenderer {
        private JButton btnEditar;

        public ButtonRenderer() {
            setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
            setBackground(new Color(255, 178, 102));

            btnEditar = new JButton("Actualizar");
            btnEditar.setBackground(new Color(100, 149, 237));
            btnEditar.setForeground(Color.WHITE);
            add(btnEditar);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            return this;
        }
    }

    // Editor para manejar clics en el botón
    class ButtonEditor extends DefaultCellEditor {
        private JPanel panel;
        private JButton btnEditar;
        private String estCod;

        public ButtonEditor(JCheckBox checkBox) {
            super(checkBox);
            panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 5));
            panel.setBackground(new Color(255, 178, 102));

            btnEditar = new JButton("Actualizar");
            btnEditar.setBackground(new Color(100, 149, 237));
            btnEditar.setForeground(Color.WHITE);
            btnEditar.addActionListener(e -> {
                fireEditingStopped();
                abrirFormularioEditar(estCod);
            });
            panel.add(btnEditar);
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value,
                boolean isSelected, int row, int column) {
            estCod = (String) value;
            return panel;
        }

        @Override
        public Object getCellEditorValue() {
            return estCod;
        }
    }
}