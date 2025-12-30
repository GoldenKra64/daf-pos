package com.daf.view.materiaprima;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.sql.Connection;
import java.util.List;

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

import com.daf.controller.MateriaPrima;
import com.daf.view.MenuPrincipal;

public class MateriaPrimaView extends JPanel {
    private Connection conn;
    private MenuPrincipal menuPrincipal;
    private MateriaPrima materiaPrimaDP;

    // Componentes de la interfaz
    private JTextField txtBuscar;
    private JButton btnRegresar, btnBuscar, btnCrear;
    private JTable table;
    private DefaultTableModel tableModel;
    private JLabel lblPaginacion;
    private JButton btnAnterior, btnSiguiente;

    // Variables de paginación
    private int paginaActual = 1;
    private final int REGISTROS_POR_PAGINA = 20;
    private List<MateriaPrima> todosLosRegistros;
    private List<MateriaPrima> registrosFiltrados;

    public MateriaPrimaView(Connection conn, MenuPrincipal menuPrincipal) {
        this.conn = conn;
        this.menuPrincipal = menuPrincipal;
        this.materiaPrimaDP = new MateriaPrima(conn);

        initComponents();
        cargarDatos();
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

        // Panel de búsqueda y crear
        JPanel panelBusqueda = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        panelBusqueda.setBackground(new Color(255, 239, 204));

        txtBuscar = new JTextField(40);
        txtBuscar.setPreferredSize(new Dimension(400, 30));
        panelBusqueda.add(txtBuscar);

        btnBuscar = new JButton("Buscar");
        btnBuscar.setBackground(new Color(255, 178, 102));
        btnBuscar.addActionListener(e -> buscarMateriaPrima());
        panelBusqueda.add(btnBuscar);

        btnCrear = new JButton("Crear");
        btnCrear.setBackground(new Color(255, 178, 102));
        btnCrear.addActionListener(e -> abrirFormularioCrear());
        panelBusqueda.add(btnCrear);

        add(panelBusqueda, BorderLayout.CENTER);

        // Crear panel principal que contendrá búsqueda y tabla
        JPanel panelPrincipal = new JPanel(new BorderLayout(10, 10));
        panelPrincipal.setBackground(new Color(255, 239, 204));
        panelPrincipal.add(panelBusqueda, BorderLayout.NORTH);

        // Panel de tabla
        String[] columnas = {"Unidad Medida", "Descripción", "Precio", "Cantidad", "Prioridad", "Acciones"};
        tableModel = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 5;
            }
        };

        table = new JTable(tableModel);
        table.setRowHeight(40);
        table.getColumnModel().getColumn(5).setCellRenderer(new ButtonRenderer());
        table.getColumnModel().getColumn(5).setCellEditor(new ButtonEditor(new JCheckBox()));

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
        lblPaginacion.setFont(new Font("Arial", Font.BOLD, 14));
        panelPaginacion.add(lblPaginacion);

        btnSiguiente = new JButton("Siguiente >");
        btnSiguiente.addActionListener(e -> paginaSiguiente());
        panelPaginacion.add(btnSiguiente);

        add(panelPaginacion, BorderLayout.SOUTH);
    }

    private void cargarDatos() {
        todosLosRegistros = materiaPrimaDP.getAllDP();
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
            MateriaPrima mp = registrosFiltrados.get(i);
            Object[] fila = {
                mp.getUmCompra(),
                mp.getMpDescripcion(),
                String.format("$%.2f", mp.getMpPrecioCompra()),
                mp.getMpCantidad(),
                mp.getMpPrioridad().equals("F") ? "FIFO" : "LIFO",
                mp.getMpCodigo()
            };
            tableModel.addRow(fila);
        }

        lblPaginacion.setText(paginaActual + "/" + totalPaginas);
        btnAnterior.setEnabled(paginaActual > 1);
        btnSiguiente.setEnabled(paginaActual < totalPaginas);
    }

    private void buscarMateriaPrima() {
        String busqueda = txtBuscar.getText().trim();
        if (busqueda.isEmpty()) {
            registrosFiltrados = todosLosRegistros;
        } else {
            registrosFiltrados = materiaPrimaDP.getByNameDP(busqueda);
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
        MenuPrincipal menu = menuPrincipal;
        MateriaPrimaForm form = new MateriaPrimaForm(conn, this, null);
        menu.registrarVista("MATERIA_PRIMA_FORM", form);
        menu.mostrarVista("MATERIA_PRIMA_FORM");
    }

    private void abrirFormularioEditar(String mpCodigo) {
        MateriaPrima mp = null;
        for (MateriaPrima m : todosLosRegistros) {
            if (m.getMpCodigo().equals(mpCodigo)) {
                mp = m;
                break;
            }
        }

        if (mp != null) {
            MenuPrincipal menu = menuPrincipal;
            MateriaPrimaForm form = new MateriaPrimaForm(conn, this, mp);
            menu.registrarVista("MATERIA_PRIMA_FORM", form);
            menu.mostrarVista("MATERIA_PRIMA_FORM");
        }
    }

    private void eliminarMateriaPrima(String mpCodigo) {
        int confirmacion = JOptionPane.showConfirmDialog(
            this,
            "¿Está seguro de eliminar esta materia prima?",
            "Confirmar eliminación",
            JOptionPane.YES_NO_OPTION
        );

        if (confirmacion == JOptionPane.YES_OPTION) {
            MateriaPrima mp = null;
            for (MateriaPrima m : todosLosRegistros) {
                if (m.getMpCodigo().equals(mpCodigo)) {
                    mp = m;
                    break;
                }
            }

            if (mp != null && mp.deleteDP()) {
                JOptionPane.showMessageDialog(this, "Materia prima eliminada exitosamente");
                cargarDatos();
            } else {
                JOptionPane.showMessageDialog(this, "Error al eliminar la materia prima", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void regresarMenu() {
        menuPrincipal.regresarMenu();
    }

    public void refrescarDatos() {
        cargarDatos();
    }

    /* Mostrar los botones en la tabla */
    class ButtonRenderer extends JPanel implements TableCellRenderer {
        private JButton btnActualizar;
        private JButton btnEliminar;

        public ButtonRenderer() {
            setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
            setBackground(new Color(255, 178, 102));

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

    // Editor para manejar clics en los botones
    class ButtonEditor extends DefaultCellEditor {
        private JPanel panel;
        private JButton btnActualizar;
        private JButton btnEliminar;
        private String mpCodigo;

        public ButtonEditor(JCheckBox checkBox) {
            super(checkBox);
            panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 5));
            panel.setBackground(new Color(255, 178, 102));

            btnActualizar = new JButton("Actualizar");
            btnActualizar.setBackground(new Color(255, 178, 102));
            btnActualizar.addActionListener(e -> {
                fireEditingStopped();
                abrirFormularioEditar(mpCodigo);
            });
            panel.add(btnActualizar);

            btnEliminar = new JButton("Eliminar");
            btnEliminar.setBackground(new Color(255, 178, 102));
            btnEliminar.addActionListener(e -> {
                fireEditingStopped();
                eliminarMateriaPrima(mpCodigo);
            });
            panel.add(btnEliminar);
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value,
                boolean isSelected, int row, int column) {
            mpCodigo = (String) value;
            return panel;
        }

        @Override
        public Object getCellEditorValue() {
            return mpCodigo;
        }
    }
}