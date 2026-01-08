package com.daf.view.ordencompra;

import com.daf.controller.OrdenCompra;
import com.daf.view.MenuPrincipal;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.sql.Connection;
import java.util.List;

public class OrdenCompraView extends JPanel {

    private final Connection conn;
    private final MenuPrincipal menuPrincipal;
    private final OrdenCompra ocDP;

    private JTextField txtBuscar;
    private JButton btnRegresar, btnBuscar, btnCrear;

    private JTable table;
    private DefaultTableModel tableModel;

    private List<OrdenCompra> todos;
    private List<OrdenCompra> filtrados;

    public OrdenCompraView(Connection conn, MenuPrincipal menuPrincipal) {
        this.conn = conn;
        this.menuPrincipal = menuPrincipal;
        this.ocDP = new OrdenCompra(conn);

        initComponents();
        cargarDatos();
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        setOpaque(false);

        JPanel panelSuperior = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        panelSuperior.setBackground(new Color(255, 178, 102));

        btnRegresar = new JButton("Regresar");
        btnRegresar.setPreferredSize(new Dimension(120, 35));
        btnRegresar.addActionListener(e -> menuPrincipal.regresarMenu());
        panelSuperior.add(btnRegresar);

        add(panelSuperior, BorderLayout.NORTH);

        JPanel panelBusqueda = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        panelBusqueda.setBackground(new Color(255, 239, 204));

        txtBuscar = new JTextField(40);
        txtBuscar.setPreferredSize(new Dimension(400, 30));
        panelBusqueda.add(txtBuscar);

        btnBuscar = new JButton("Buscar");
        btnBuscar.setBackground(new Color(255, 178, 102));
        btnBuscar.addActionListener(e -> buscar());
        panelBusqueda.add(btnBuscar);

        btnCrear = new JButton("Crear");
        btnCrear.setBackground(new Color(255, 178, 102));
        btnCrear.addActionListener(e -> abrirFormulario(null));
        panelBusqueda.add(btnCrear);

        // Tabla
        String[] cols = {"Proveedor", "Fecha", "Subtotal", "IVA", "Total", "Estado", "Acciones"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int row, int col) { return col == 6; }
        };

        table = new JTable(tableModel);
        table.setRowHeight(40);
        table.getColumnModel().getColumn(6).setCellRenderer(new ButtonRenderer());
        table.getColumnModel().getColumn(6).setCellEditor(new ButtonEditor(new JCheckBox()));

        JPanel panelCentro = new JPanel(new BorderLayout(10,10));
        panelCentro.setBackground(new Color(255, 239, 204));
        panelCentro.add(panelBusqueda, BorderLayout.NORTH);
        panelCentro.add(new JScrollPane(table), BorderLayout.CENTER);

        add(panelCentro, BorderLayout.CENTER);
    }

    private void cargarDatos() {
        todos = ocDP.getAllDP();
        filtrados = todos;
        actualizarTabla();
    }

    private void actualizarTabla() {
        tableModel.setRowCount(0);

        for (OrdenCompra oc : filtrados) {
            Object[] row = {
                oc.getPrvRazonSocial(),
                oc.getOcFecha() == null ? "" : oc.getOcFecha().toString(),
                String.format("$%.2f", oc.getOcSubtotal()),
                String.format("$%.2f", oc.getOcIva()),
                String.format("$%.2f", oc.getOcTotal()),
                oc.getOcEstado(),
                oc.getOcCodigo()
            };
            tableModel.addRow(row);
        }
    }

    private void buscar() {
        String filtro = txtBuscar.getText().trim();
        filtrados = filtro.isEmpty() ? todos : ocDP.getByProveedorDP(filtro);
        actualizarTabla();
    }

    private void abrirFormulario(String ocCodigo) {
        OrdenCompra oc = null;
        if (ocCodigo != null) oc = ocDP.getByCodigoDP(ocCodigo);

        OrdenCompraForm form = new OrdenCompraForm(conn, this, oc);
        menuPrincipal.registrarVista("ORDEN_COMPRA_FORM", form);
        menuPrincipal.mostrarVista("ORDEN_COMPRA_FORM");
    }

    public void refrescarDatos() { cargarDatos(); }

    class ButtonRenderer extends JPanel implements TableCellRenderer {
        private final JButton btnActualizar = new JButton("Actualizar");
        public ButtonRenderer() {
            setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
            setBackground(new Color(255, 178, 102));
            btnActualizar.setBackground(new Color(255, 178, 102));
            add(btnActualizar);
        }
        @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            return this;
        }
    }

    class ButtonEditor extends DefaultCellEditor {
        private final JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 5));
        private final JButton btnActualizar = new JButton("Actualizar");
        private String ocCodigo;

        public ButtonEditor(JCheckBox checkBox) {
            super(checkBox);
            panel.setBackground(new Color(255, 178, 102));
            btnActualizar.setBackground(new Color(255, 178, 102));
            btnActualizar.addActionListener(e -> {
                fireEditingStopped();
                abrirFormulario(ocCodigo);
            });
            panel.add(btnActualizar);
        }

        @Override public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            ocCodigo = (String) value;
            return panel;
        }

        @Override public Object getCellEditorValue() { return ocCodigo; }
    }
}
