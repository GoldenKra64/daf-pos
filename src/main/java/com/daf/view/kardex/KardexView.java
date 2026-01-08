package com.daf.view.kardex;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.io.FileInputStream;
import java.sql.Connection;
import java.text.SimpleDateFormat;
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

import com.daf.controller.Kardex;
import com.daf.view.MenuPrincipal;

public class KardexView extends JPanel {
    private Properties props;
    
    private Connection conn;
    private MenuPrincipal menuPrincipal;
    private Kardex kardexDP;

    private JTextField txtBuscar;
    private JButton btnRegresar, btnBuscar;
    private JTable table;
    private DefaultTableModel tableModel;
    private JLabel lblPaginacion;
    private JButton btnAnterior, btnSiguiente;

    private int paginaActual = 1;
    private int REGISTROS_POR_PAGINA;
    private List<Kardex> todosLosRegistros;
    private List<Kardex> registrosFiltrados;
    
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");

    public KardexView(Connection conn, MenuPrincipal menuPrincipal) {
        this.conn = conn;
        this.menuPrincipal = menuPrincipal;
        this.kardexDP = new Kardex(conn);

        loadProperties();

        REGISTROS_POR_PAGINA = Integer.parseInt(props.getProperty("REGISTROS_POR_PAGINA"));
        
        initComponents();
        cargarDatos();
    }

    private void loadProperties() {
        props = new Properties();
        try (FileInputStream fis = new FileInputStream("src/main/resources/config.properties")) {
            props.load(fis);
        } catch (Exception e) {
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
        btnBuscar.addActionListener(e -> buscarKardex());
        panelBusqueda.add(btnBuscar);

        // Crear panel principal que contendrá búsqueda y tabla
        JPanel panelPrincipal = new JPanel(new BorderLayout(10, 10));
        panelPrincipal.setBackground(new Color(255, 239, 204));
        panelPrincipal.add(panelBusqueda, BorderLayout.NORTH);

        // Panel de tabla
        String[] columnas = {"Origen", "Cantidad", "Total", "Fecha/Hora", "Acción", "Usuario", "Editar"};
        tableModel = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 6; // Columna editable
            }
        };

        table = new JTable(tableModel);
        table.setRowHeight(40);
        table.getColumnModel().getColumn(6).setCellRenderer(new ButtonRenderer());
        table.getColumnModel().getColumn(6).setCellEditor(new ButtonEditor(new JCheckBox()));
        
        // Ajustar anchos de columnas
        table.getColumnModel().getColumn(0).setPreferredWidth(150); // Origen
        table.getColumnModel().getColumn(1).setPreferredWidth(70);  // Cantidad
        table.getColumnModel().getColumn(2).setPreferredWidth(70);  // Total
        table.getColumnModel().getColumn(3).setPreferredWidth(130); // Fecha
        table.getColumnModel().getColumn(4).setPreferredWidth(200); // Acción
        table.getColumnModel().getColumn(5).setPreferredWidth(120); // Usuario
        table.getColumnModel().getColumn(6).setPreferredWidth(100); // Editar

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
        todosLosRegistros = kardexDP.getAllDP();
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
            Kardex k = registrosFiltrados.get(i);
            Object[] fila = {
                k.getOrigen(),
                k.getKrdCantidad(),
                k.getKrdQtyTotal(),
                k.getKrdFechahora() != null ? dateFormat.format(k.getKrdFechahora()) : "N/A",
                k.getKrdAccion(),
                k.getUsrId(),
                k.getKrdCodigo()    // Para el update
            };
            tableModel.addRow(fila);
        }

        lblPaginacion.setText(paginaActual + "/" + totalPaginas);
        btnAnterior.setEnabled(paginaActual > 1);
        btnSiguiente.setEnabled(paginaActual < totalPaginas);
    }

    private void buscarKardex() {
        String busqueda = txtBuscar.getText().trim();
        if (busqueda.isEmpty()) {
            registrosFiltrados = todosLosRegistros;
        } else {
            registrosFiltrados = kardexDP.getByNameDP(busqueda);
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

    private void abrirFormularioEditar(String krdCodigo) {
        Kardex k = null;
        for (Kardex kardex : todosLosRegistros) {
            if (kardex.getKrdCodigo().equals(krdCodigo)) {
                k = kardex;
                break;
            }
        }

        if (k != null) {
            MenuPrincipal menu = menuPrincipal;
            KardexForm form = new KardexForm(conn, this, k);
            menu.registrarVista("KARDEX_FORM", form);
            menu.mostrarVista("KARDEX_FORM");
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
        private String krdCodigo;

        public ButtonEditor(JCheckBox checkBox) {
            super(checkBox);
            panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 5));
            panel.setBackground(new Color(255, 178, 102));

            btnEditar = new JButton("Actualizar");
            btnEditar.setBackground(new Color(100, 149, 237));
            btnEditar.setForeground(Color.WHITE);
            btnEditar.addActionListener(e -> {
                fireEditingStopped();
                abrirFormularioEditar(krdCodigo);
            });
            panel.add(btnEditar);
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value,
                boolean isSelected, int row, int column) {
            krdCodigo = (String) value;
            return panel;
        }

        @Override
        public Object getCellEditorValue() {
            return krdCodigo;
        }
    }
}