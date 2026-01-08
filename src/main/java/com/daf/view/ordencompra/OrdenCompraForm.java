package com.daf.view.ordencompra;

import com.daf.controller.DetalleOC;
import com.daf.controller.MateriaPrima;
import com.daf.controller.OrdenCompra;
import com.daf.controller.Proveedor;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.AbstractCellEditor;

import java.awt.*;
import java.io.FileInputStream;
import java.math.BigDecimal;
import java.util.List;
import java.util.Properties;
import java.sql.Connection;

public class OrdenCompraForm extends JPanel {

    private final Connection conn;
    private final OrdenCompraView vistaAnterior;
    private OrdenCompra ocActual;

    private final Proveedor proveedorDP;
    private final MateriaPrima materiaPrimaDP;

    private final Properties props = new Properties();

    // Cabecera
    private JComboBox<Proveedor> cmbProveedor;
    private JLabel lblCodigo;
    private JLabel lblFecha;
    private JLabel lblEstado;

    // Detalle
    private JButton btnAnadirDetalle;
    private JTable tableDetalles;
    private DefaultTableModel tableModelDetalles;

    // Totales
    private JLabel lblSubtotalValor;
    private JLabel lblIvaValor;
    private JLabel lblTotalValor;

    // Botones
    private JButton btnGuardar, btnAprobar, btnAnular, btnCancelar;

    public OrdenCompraForm(Connection conn, OrdenCompraView vistaAnterior, OrdenCompra oc) {
        this.conn = conn;
        this.vistaAnterior = vistaAnterior;

        this.proveedorDP = new Proveedor(conn);
        this.materiaPrimaDP = new MateriaPrima(conn);

        loadProperties();

        if (oc == null) {
            this.ocActual = new OrdenCompra(conn);
        } else {
            this.ocActual = oc;
        }

        initComponents();

        // Si viene desde editar/ver
        if (oc != null) {
            cargarDatos(ocActual);
        } else {
            // nuevo: estado PEN, fecha se setea al guardar (pero podemos mostrar "HOY" en UI)
            lblEstado.setText("PEN");
            lblFecha.setText("HOY");
        }

        configurarEstadoComponentes();
        recalcularTotalesUI();
    }

    private void loadProperties() {
        try (FileInputStream fis = new FileInputStream("src/main/resources/config.properties")) {
            props.load(fis);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private BigDecimal getIvaRate() {
        try {
            return new BigDecimal(props.getProperty("IVA", "0.12").trim());
        } catch (Exception e) {
            return new BigDecimal("0.12");
        }
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        setOpaque(false);
        setBackground(new Color(255, 239, 204));

        JPanel panelPrincipal = new JPanel(new BorderLayout(10, 10));
        panelPrincipal.setBackground(new Color(255, 239, 204));
        panelPrincipal.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // ================== CABECERA ==================
        JPanel panelCabecera = new JPanel();
        panelCabecera.setLayout(new BoxLayout(panelCabecera, BoxLayout.Y_AXIS));
        panelCabecera.setBackground(Color.WHITE);
        panelCabecera.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.GRAY),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));

        JLabel titulo = new JLabel((ocActual.getOcCodigo() == null || ocActual.getOcCodigo().isBlank())
                ? "NUEVA ORDEN DE COMPRA"
                : "ORDEN DE COMPRA");
        titulo.setFont(new Font("Arial", Font.BOLD, 18));
        titulo.setAlignmentX(Component.LEFT_ALIGNMENT);
        panelCabecera.add(titulo);
        panelCabecera.add(Box.createRigidArea(new Dimension(0, 10)));

        // Código
        JPanel panelCodigo = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panelCodigo.setBackground(Color.WHITE);
        panelCodigo.add(new JLabel("Código:"));
        lblCodigo = new JLabel(ocActual.getOcCodigo() == null ? "(Auto)" : ocActual.getOcCodigo());
        lblCodigo.setFont(new Font("Arial", Font.BOLD, 13));
        panelCodigo.add(lblCodigo);
        panelCabecera.add(panelCodigo);

        // Fecha
        JPanel panelFecha = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panelFecha.setBackground(Color.WHITE);
        panelFecha.add(new JLabel("Fecha:"));
        lblFecha = new JLabel(ocActual.getOcFecha() == null ? "" : ocActual.getOcFecha().toString());
        lblFecha.setFont(new Font("Arial", Font.BOLD, 13));
        panelFecha.add(lblFecha);
        panelCabecera.add(panelFecha);

        // Estado
        JPanel panelEstado = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panelEstado.setBackground(Color.WHITE);
        panelEstado.add(new JLabel("Estado:"));
        lblEstado = new JLabel(ocActual.getOcEstado() == null ? "PEN" : ocActual.getOcEstado());
        lblEstado.setFont(new Font("Arial", Font.BOLD, 13));
        panelEstado.add(lblEstado);
        panelCabecera.add(panelEstado);

        // Proveedor
        JPanel panelProveedor = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panelProveedor.setBackground(Color.WHITE);

        JLabel lblProv = new JLabel("Proveedor:");
        lblProv.setPreferredSize(new Dimension(100, 25));
        panelProveedor.add(lblProv);

        cmbProveedor = crearComboProveedor();
        cmbProveedor.setPreferredSize(new Dimension(320, 30));
        panelProveedor.add(cmbProveedor);

        panelCabecera.add(panelProveedor);

        panelPrincipal.add(panelCabecera, BorderLayout.NORTH);

        // ================== DETALLES ==================
        JPanel panelDetalles = new JPanel(new BorderLayout(5, 5));
        panelDetalles.setBackground(Color.WHITE);
        panelDetalles.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("Detalles de la Orden"),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        JPanel panelBotonAnadir = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panelBotonAnadir.setBackground(Color.WHITE);

        btnAnadirDetalle = new JButton("+ Añadir Detalle");
        btnAnadirDetalle.setBackground(new Color(76, 175, 80));
        btnAnadirDetalle.setForeground(Color.WHITE);
        btnAnadirDetalle.addActionListener(e -> anadirDetalle());
        panelBotonAnadir.add(btnAnadirDetalle);

        panelDetalles.add(panelBotonAnadir, BorderLayout.NORTH);

        String[] cols = {"Materia Prima", "Cantidad", "Precio Unit.", "Subtotal", "Eliminar"};
        tableModelDetalles = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                // editable solo en PEN
                if (!"PEN".equals(ocActual.getOcEstado())) return false;
                return col == 0 || col == 1 || col == 4;
            }
        };

        tableDetalles = new JTable(tableModelDetalles);
        tableDetalles.setRowHeight(35);

        tableDetalles.getColumnModel().getColumn(0).setCellEditor(new ComboBoxCellEditor());
        tableDetalles.getColumnModel().getColumn(0).setCellRenderer(new ComboBoxCellRenderer());

        tableDetalles.getColumnModel().getColumn(1).setCellEditor(new CantidadCellEditor());

        tableDetalles.getColumnModel().getColumn(4).setCellRenderer(new ButtonCellRenderer());
        tableDetalles.getColumnModel().getColumn(4).setCellEditor(new ButtonCellEditor(new JCheckBox()));

        // listener para recalcular totales cuando cambia algo
        tableModelDetalles.addTableModelListener(e -> recalcularTotalesUI());

        panelDetalles.add(new JScrollPane(tableDetalles), BorderLayout.CENTER);

        // Totales
        JPanel panelTotales = new JPanel(new GridLayout(3, 2, 10, 5));
        panelTotales.setBackground(new Color(255, 239, 204));
        panelTotales.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

        panelTotales.add(new JLabel("Subtotal:"));
        lblSubtotalValor = new JLabel("$0.00");
        lblSubtotalValor.setFont(new Font("Arial", Font.BOLD, 13));
        panelTotales.add(lblSubtotalValor);

        panelTotales.add(new JLabel("IVA:"));
        lblIvaValor = new JLabel("$0.00");
        lblIvaValor.setFont(new Font("Arial", Font.BOLD, 13));
        panelTotales.add(lblIvaValor);

        panelTotales.add(new JLabel("Total:"));
        lblTotalValor = new JLabel("$0.00");
        lblTotalValor.setFont(new Font("Arial", Font.BOLD, 13));
        panelTotales.add(lblTotalValor);

        panelDetalles.add(panelTotales, BorderLayout.SOUTH);

        panelPrincipal.add(panelDetalles, BorderLayout.CENTER);

        // ================== BOTONES ==================
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        panelBotones.setBackground(new Color(255, 239, 204));

        btnAprobar = new JButton("Aprobar");
        btnAprobar.setPreferredSize(new Dimension(120, 35));
        btnAprobar.setBackground(new Color(76, 175, 80));
        btnAprobar.setForeground(Color.WHITE);
        btnAprobar.addActionListener(e -> aprobar());

        btnAnular = new JButton("Anular");
        btnAnular.setPreferredSize(new Dimension(120, 35));
        btnAnular.setBackground(new Color(255, 152, 0));
        btnAnular.setForeground(Color.WHITE);
        btnAnular.addActionListener(e -> anular());

        btnGuardar = new JButton("Guardar");
        btnGuardar.setPreferredSize(new Dimension(120, 35));
        btnGuardar.setBackground(new Color(33, 150, 243));
        btnGuardar.setForeground(Color.WHITE);
        btnGuardar.addActionListener(e -> guardar());

        btnCancelar = new JButton("Cancelar");
        btnCancelar.setPreferredSize(new Dimension(120, 35));
        btnCancelar.setBackground(new Color(158, 158, 158));
        btnCancelar.setForeground(Color.WHITE);
        btnCancelar.addActionListener(e -> cancelar());

        panelBotones.add(btnAprobar);
        panelBotones.add(btnAnular);
        panelBotones.add(btnGuardar);
        panelBotones.add(btnCancelar);

        panelPrincipal.add(panelBotones, BorderLayout.SOUTH);

        add(panelPrincipal, BorderLayout.CENTER);
    }

    private JComboBox<Proveedor> crearComboProveedor() {
        JComboBox<Proveedor> combo = new JComboBox<>();
        combo.addItem(null);

        List<Proveedor> proveedores = proveedorDP.getAllDP();
        for (Proveedor p : proveedores) combo.addItem(p);

        combo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                          boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value == null) setText("Seleccione un proveedor...");
                else setText(((Proveedor) value).getPrvRazonSocial());
                return this;
            }
        });

        return combo;
    }

    private void anadirDetalle() {
        if (ocActual.getDetalles() == null) {
            // por seguridad
            ocActual.setDetalles(new java.util.ArrayList<>());
        }
        DetalleOC d = new DetalleOC();
        d.setPxocCantidad(new BigDecimal("0"));
        ocActual.addDetalle(d);

        Object[] fila = {"Seleccionar...", "0", "$0.00", "$0.00", "Eliminar"};
        tableModelDetalles.addRow(fila);
    }

    private void eliminarDetalle(int fila) {
        if (fila >= 0 && fila < ocActual.getDetalles().size()) {
            ocActual.removeDetalle(fila);
            tableModelDetalles.removeRow(fila);
            recalcularTotalesUI();
        }
    }

    private void actualizarDetallesDesdeTabla() {
        List<DetalleOC> dets = ocActual.getDetalles();

        for (int i = 0; i < Math.min(tableModelDetalles.getRowCount(), dets.size()); i++) {
            DetalleOC d = dets.get(i);

            // Cantidad
            Object cObj = tableModelDetalles.getValueAt(i, 1);
            BigDecimal cantidad = BigDecimal.ZERO;
            try {
                if (cObj != null) cantidad = new BigDecimal(cObj.toString().trim());
            } catch (Exception ignored) {}
            d.setPxocCantidad(cantidad);

            d.recalcular();
        }
    }

    private void recalcularTotalesUI() {
        // Evitar bucles
        javax.swing.event.TableModelListener[] listeners = tableModelDetalles.getTableModelListeners();
        for (javax.swing.event.TableModelListener l : listeners) tableModelDetalles.removeTableModelListener(l);

        try {
            actualizarDetallesDesdeTabla();

            BigDecimal subtotal = BigDecimal.ZERO;
            for (DetalleOC d : ocActual.getDetalles()) {
                d.recalcular();
                subtotal = subtotal.add(d.getPxocSubtotal());
            }

            BigDecimal iva = subtotal.multiply(getIvaRate());
            BigDecimal total = subtotal.add(iva);

            // actualizar subtotales por fila
            for (int i = 0; i < tableModelDetalles.getRowCount() && i < ocActual.getDetalles().size(); i++) {
                DetalleOC d = ocActual.getDetalles().get(i);
                tableModelDetalles.setValueAt(String.format("$%.2f", d.getMpPrecioCompra()), i, 2);
                tableModelDetalles.setValueAt(String.format("$%.2f", d.getPxocSubtotal()), i, 3);
            }

            lblSubtotalValor.setText(String.format("$%.2f", subtotal));
            lblIvaValor.setText(String.format("$%.2f", iva));
            lblTotalValor.setText(String.format("$%.2f", total));

        } finally {
            for (javax.swing.event.TableModelListener l : listeners) tableModelDetalles.addTableModelListener(l);
        }
    }

    private void cargarDatos(OrdenCompra oc) {
        lblCodigo.setText(oc.getOcCodigo());
        lblFecha.setText(oc.getOcFecha() == null ? "" : oc.getOcFecha().toString());
        lblEstado.setText(oc.getOcEstado());

        // Proveedor
        for (int i = 0; i < cmbProveedor.getItemCount(); i++) {
            Proveedor p = cmbProveedor.getItemAt(i);
            if (p != null && p.getPrvCodigo().trim().equals(oc.getPrvCodigo().trim())) {
                cmbProveedor.setSelectedIndex(i);
                break;
            }
        }

        // Detalles
        tableModelDetalles.setRowCount(0);
        for (DetalleOC d : oc.getDetalles()) {
            Object[] fila = {
                    d.getMpDescripcion() != null ? d.getMpDescripcion() : "Seleccionar...",
                    d.getPxocCantidad() == null ? "0" : d.getPxocCantidad().toString(),
                    String.format("$%.2f", d.getMpPrecioCompra()),
                    String.format("$%.2f", d.getPxocSubtotal()),
                    "Eliminar"
            };
            tableModelDetalles.addRow(fila);
        }
    }

    private void configurarEstadoComponentes() {
        boolean esNuevo = (ocActual.getOcCodigo() == null || ocActual.getOcCodigo().isBlank());
        boolean esPEN = "PEN".equals(ocActual.getOcEstado());

        // Bloquear proveedor al guardar (si ya existe)
        cmbProveedor.setEnabled(esNuevo && esPEN);

        // Detalle editable solo PEN
        btnAnadirDetalle.setEnabled(esPEN);
        tableDetalles.setEnabled(esPEN);

        // Botones
        btnGuardar.setEnabled(esPEN);
        btnAprobar.setEnabled(esPEN);
        btnAnular.setEnabled(esPEN);
    }

    private void guardar() {
        try {
            // proveedor
            Proveedor prov = (Proveedor) cmbProveedor.getSelectedItem();
            if (prov == null) {
                JOptionPane.showMessageDialog(this, "Debe seleccionar un proveedor", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // actualizar oc
            ocActual.setPrvCodigo(prov.getPrvCodigo());
            ocActual.setOcEstado("PEN");

            // detalles desde tabla (y validar selección MP)
            actualizarDetallesDesdeTabla();

            // Validación: al menos un detalle y MP seleccionada
            if (ocActual.getDetalles() == null || ocActual.getDetalles().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Debe agregar al menos un detalle", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            for (DetalleOC d : ocActual.getDetalles()) {
                if (d.getMpCodigo() == null || d.getMpCodigo().isBlank()) {
                    JOptionPane.showMessageDialog(this, "Hay detalles sin materia prima seleccionada", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                if (d.getPxocCantidad() == null || d.getPxocCantidad().doubleValue() <= 0) {
                    JOptionPane.showMessageDialog(this, "La cantidad debe ser mayor a 0", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }

            String error = ocActual.validate();
            if (error != null) {
                JOptionPane.showMessageDialog(this, error, "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (ocActual.saveDP()) {
                JOptionPane.showMessageDialog(this, "Orden guardada exitosamente (PEN)");
                vistaAnterior.refrescarDatos();

                // refrescar UI: ahora ya tiene código
                lblCodigo.setText(ocActual.getOcCodigo());
                lblFecha.setText(ocActual.getOcFecha() == null ? "" : ocActual.getOcFecha().toString());
                lblEstado.setText(ocActual.getOcEstado());

                configurarEstadoComponentes();
            } else {
                JOptionPane.showMessageDialog(this, "Error al guardar la orden", "Error", JOptionPane.ERROR_MESSAGE);
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void aprobar() {
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "¿Está seguro de aprobar la orden? No podrá editarla después.",
                "Confirmar aprobación",
                JOptionPane.YES_NO_OPTION
        );
        if (confirm != JOptionPane.YES_OPTION) return;

        if (ocActual.getOcCodigo() == null || ocActual.getOcCodigo().isBlank()) {
            JOptionPane.showMessageDialog(this, "Debe guardar la orden antes de aprobar", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (ocActual.aprobarDP()) {
            JOptionPane.showMessageDialog(this, "Orden aprobada exitosamente");
            lblEstado.setText("APR");
            configurarEstadoComponentes();
            vistaAnterior.refrescarDatos();
        } else {
            JOptionPane.showMessageDialog(this, "No se pudo aprobar (solo PEN)", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void anular() {
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "¿Está seguro de anular la orden? No podrá editarla después.",
                "Confirmar anulación",
                JOptionPane.YES_NO_OPTION
        );
        if (confirm != JOptionPane.YES_OPTION) return;

        if (ocActual.getOcCodigo() == null || ocActual.getOcCodigo().isBlank()) {
            JOptionPane.showMessageDialog(this, "Debe guardar la orden antes de anular", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (ocActual.anularDP()) {
            JOptionPane.showMessageDialog(this, "Orden anulada exitosamente");
            lblEstado.setText("ANU");
            configurarEstadoComponentes();
            vistaAnterior.refrescarDatos();
        } else {
            JOptionPane.showMessageDialog(this, "No se pudo anular (solo PEN)", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void cancelar() {
        Container parent = vistaAnterior.getParent();
        if (parent != null && parent.getLayout() instanceof CardLayout) {
            ((CardLayout) parent.getLayout()).show(parent, "ORDEN_COMPRA");
        }
    }

    // ======================= Tabla: MP Combo =======================

    class ComboBoxCellEditor extends AbstractCellEditor implements TableCellEditor {
        private final JComboBox<MateriaPrima> combo;
        private int fila;

        public ComboBoxCellEditor() {
            combo = new JComboBox<>();
            List<MateriaPrima> materias = materiaPrimaDP.getForComboBoxDP();
            for (MateriaPrima mp : materias) combo.addItem(mp);

            combo.setRenderer(new DefaultListCellRenderer() {
                @Override
                public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                              boolean isSelected, boolean cellHasFocus) {
                    super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                    if (value instanceof MateriaPrima) setText(((MateriaPrima) value).getMpDescripcion());
                    return this;
                }
            });

            combo.addActionListener(e -> {
                if (fila < 0 || fila >= ocActual.getDetalles().size()) return;

                MateriaPrima mp = (MateriaPrima) combo.getSelectedItem();
                if (mp == null) return;

                // Evitar duplicados
                String nuevoCod = mp.getMpCodigo().trim();
                for (int i = 0; i < ocActual.getDetalles().size(); i++) {
                    if (i == fila) continue;
                    DetalleOC other = ocActual.getDetalles().get(i);
                    if (other.getMpCodigo() != null && other.getMpCodigo().trim().equals(nuevoCod)) {
                        JOptionPane.showMessageDialog(OrdenCompraForm.this,
                                "Esta materia prima ya fue agregada en otra fila",
                                "Duplicado",
                                JOptionPane.WARNING_MESSAGE);
                        combo.setSelectedItem(null);
                        return;
                    }
                }

                DetalleOC d = ocActual.getDetalles().get(fila);
                d.setMpCodigo(mp.getMpCodigo());
                d.setMpDescripcion(mp.getMpDescripcion());

                // Precio compra (necesitas que tu combo traiga precio real)
                try {
                    d.setMpPrecioCompra(BigDecimal.valueOf(mp.getMpPrecioCompra()));
                } catch (Exception ex) {
                    d.setMpPrecioCompra(BigDecimal.ZERO);
                }

                d.recalcular();
                tableModelDetalles.setValueAt(mp.getMpDescripcion(), fila, 0);
                tableModelDetalles.setValueAt(String.format("$%.2f", d.getMpPrecioCompra()), fila, 2);
                tableModelDetalles.setValueAt(String.format("$%.2f", d.getPxocSubtotal()), fila, 3);
                recalcularTotalesUI();
            });
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            this.fila = row;
            return combo;
        }

        @Override
        public Object getCellEditorValue() {
            MateriaPrima mp = (MateriaPrima) combo.getSelectedItem();
            return mp != null ? mp.getMpDescripcion() : "Seleccionar...";
        }
    }

    class ComboBoxCellRenderer implements TableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                       boolean hasFocus, int row, int column) {
            JLabel label = new JLabel(value != null ? value.toString() : "");
            if (isSelected) {
                label.setOpaque(true);
                label.setBackground(table.getSelectionBackground());
            }
            return label;
        }
    }

    // ======================= Editor Cantidad =======================

    class CantidadCellEditor extends AbstractCellEditor implements TableCellEditor {
        private final JTextField textField = new JTextField();
        private int fila;

        public CantidadCellEditor() {
            textField.setHorizontalAlignment(JTextField.RIGHT);
            textField.addActionListener(e -> {
                actualizar();
                fireEditingStopped();
            });
            textField.addFocusListener(new java.awt.event.FocusAdapter() {
                @Override public void focusLost(java.awt.event.FocusEvent e) {
                    actualizar();
                }
            });
        }

        private void actualizar() {
            if (fila < 0 || fila >= ocActual.getDetalles().size()) return;

            try {
                BigDecimal cant = new BigDecimal(textField.getText().trim());
                if (cant.doubleValue() <= 0) throw new NumberFormatException();

                DetalleOC d = ocActual.getDetalles().get(fila);
                d.setPxocCantidad(cant);
                d.recalcular();

                tableModelDetalles.setValueAt(cant.toString(), fila, 1);
                tableModelDetalles.setValueAt(String.format("$%.2f", d.getPxocSubtotal()), fila, 3);
                recalcularTotalesUI();

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(OrdenCompraForm.this,
                        "La cantidad debe ser un número válido mayor a 0",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                textField.setText("0");
            }
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            this.fila = row;
            textField.setText(value != null ? value.toString() : "0");
            return textField;
        }

        @Override
        public Object getCellEditorValue() {
            return textField.getText().trim();
        }
    }

    // ======================= Botón eliminar =======================

    class ButtonCellRenderer extends JButton implements TableCellRenderer {
        public ButtonCellRenderer() {
            setText("Eliminar");
            setBackground(new Color(244, 67, 54));
            setForeground(Color.WHITE);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                       boolean hasFocus, int row, int column) {
            setEnabled("PEN".equals(ocActual.getOcEstado()));
            return this;
        }
    }

    class ButtonCellEditor extends DefaultCellEditor {
        private final JButton button = new JButton("Eliminar");
        private int fila;

        public ButtonCellEditor(JCheckBox checkBox) {
            super(checkBox);
            button.setBackground(new Color(244, 67, 54));
            button.setForeground(Color.WHITE);
            button.addActionListener(e -> {
                fireEditingStopped();
                if ("PEN".equals(ocActual.getOcEstado())) eliminarDetalle(fila);
            });
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            fila = row;
            button.setEnabled("PEN".equals(ocActual.getOcEstado()));
            return button;
        }

        @Override
        public Object getCellEditorValue() { return "Eliminar"; }
    }
}
