package com.daf.view.estandar;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.util.List;
import java.util.Properties;

import javax.swing.AbstractCellEditor;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultCellEditor;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

import com.daf.controller.DetalleEstandar;
import com.daf.controller.Estandar;
import com.daf.controller.MateriaPrima;
import com.daf.controller.Producto;

public class EstandarForm extends JPanel {
    private Connection conn;
    private EstandarView vistaAnterior;
    private Estandar estandarActual;
    private Producto productoDP;
    private MateriaPrima materiaPrimaDP;

    // Componentes del formulario
    private JComboBox<Producto> cmbProducto;
    private JButton btnAnadirDetalle;
    private JTable tableDetalles;
    private DefaultTableModel tableModelDetalles;
    private JLabel lblCantidadTotal;
    private JLabel lblCostoTotal;
    private JButton btnGuardar, btnAprobar, btnAnular;
    
    // Labels para mostrar totales
    private JLabel lblTotalCantidadValor;
    private JLabel lblTotalCostoValor;

    private Properties props = new Properties();

    public EstandarForm(Connection conn, EstandarView vistaAnterior, Estandar estandar) {
        this.conn = conn;
        this.vistaAnterior = vistaAnterior;
        this.estandarActual = estandar;
        this.productoDP = new Producto(conn);
        this.materiaPrimaDP = new MateriaPrima(conn);
        loadProperties();
        initComponents();
        if (estandar != null) {
            cargarDatos(estandar);
        }
        configurarEstadoBotones();
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
        setBackground(new Color(255, 239, 204));

        // Panel principal
        JPanel panelPrincipal = new JPanel(new BorderLayout(10, 10));
        panelPrincipal.setBackground(new Color(255, 239, 204));
        panelPrincipal.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // === PANEL SUPERIOR: Cabecera ===
        JPanel panelCabecera = new JPanel();
        panelCabecera.setLayout(new BoxLayout(panelCabecera, BoxLayout.Y_AXIS));
        panelCabecera.setBackground(Color.WHITE);
        panelCabecera.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.GRAY),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));

        // Producto
        JPanel panelProducto = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panelProducto.setBackground(Color.WHITE);
        JLabel lblProducto = new JLabel("Producto:");
        lblProducto.setPreferredSize(new Dimension(100, 25));
        cmbProducto = crearComboProducto();
        cmbProducto.setPreferredSize(new Dimension(300, 30));
        panelProducto.add(lblProducto);
        panelProducto.add(cmbProducto);
        panelCabecera.add(panelProducto);

        panelPrincipal.add(panelCabecera, BorderLayout.NORTH);

        // === PANEL CENTRAL: Detalles ===
        JPanel panelDetalles = new JPanel(new BorderLayout(5, 5));
        panelDetalles.setBackground(Color.WHITE);
        panelDetalles.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder("Detalles del Estándar"),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        // Botón añadir detalle
        JPanel panelBotonAnadir = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panelBotonAnadir.setBackground(Color.WHITE);
        btnAnadirDetalle = new JButton("+ Añadir Detalle");
        btnAnadirDetalle.setBackground(new Color(76, 175, 80));
        btnAnadirDetalle.setForeground(Color.WHITE);
        btnAnadirDetalle.addActionListener(e -> anadirDetalle());
        panelBotonAnadir.add(btnAnadirDetalle);
        panelDetalles.add(panelBotonAnadir, BorderLayout.NORTH);

        // Tabla de detalles
        String[] columnasDetalles = {"Materia Prima", "Cantidad", "Precio Unit.", "Subtotal", "Eliminar"};
        tableModelDetalles = new DefaultTableModel(columnasDetalles, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 0 || column == 1 || column == 4; // MP, Cantidad, Eliminar
            }
        };

        tableDetalles = new JTable(tableModelDetalles);
        tableDetalles.setRowHeight(35);
        tableDetalles.getColumnModel().getColumn(0).setCellEditor(new ComboBoxCellEditor());
        tableDetalles.getColumnModel().getColumn(0).setCellRenderer(new ComboBoxCellRenderer());
        tableDetalles.getColumnModel().getColumn(1).setCellEditor(new CantidadCellEditor());
        tableDetalles.getColumnModel().getColumn(4).setCellRenderer(new ButtonCellRenderer());
        tableDetalles.getColumnModel().getColumn(4).setCellEditor(new ButtonCellEditor(new JCheckBox()));
        
        // Listener para detectar cambios en la tabla y recalcular
        tableModelDetalles.addTableModelListener(e -> {
            recalcularTotales();
        });
        
        JScrollPane scrollDetalles = new JScrollPane(tableDetalles);
        scrollDetalles.setPreferredSize(new Dimension(0, 250));
        panelDetalles.add(scrollDetalles, BorderLayout.CENTER);

        // Panel de totales
        JPanel panelTotales = new JPanel(new GridLayout(2, 2, 10, 5));
        panelTotales.setBackground(new Color(255, 239, 204));
        panelTotales.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        
        lblCantidadTotal = new JLabel("Cantidad Total:");
        lblCantidadTotal.setFont(new Font(props.getProperty("FONT_FAMILY"), Font.BOLD, Integer.parseInt(props.getProperty("FONT_SIZE"))));
        lblTotalCantidadValor = new JLabel("0");
        lblTotalCantidadValor.setFont(new Font(props.getProperty("FONT_FAMILY"), Font.BOLD, Integer.parseInt(props.getProperty("FONT_SIZE"))));
        
        lblCostoTotal = new JLabel("Costo de Creación:");
        lblCostoTotal.setFont(new Font(props.getProperty("FONT_FAMILY"), Font.BOLD, Integer.parseInt(props.getProperty("FONT_SIZE"))));
        lblTotalCostoValor = new JLabel("$0.00");
        lblTotalCostoValor.setFont(new Font(props.getProperty("FONT_FAMILY"), Font.BOLD, Integer.parseInt(props.getProperty("FONT_SIZE"))));
        
        panelTotales.add(lblCantidadTotal);
        panelTotales.add(lblTotalCantidadValor);
        panelTotales.add(lblCostoTotal);
        panelTotales.add(lblTotalCostoValor);
        
        panelDetalles.add(panelTotales, BorderLayout.SOUTH);

        panelPrincipal.add(panelDetalles, BorderLayout.CENTER);

        // === PANEL INFERIOR: Botones ===
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        panelBotones.setBackground(new Color(255, 239, 204));

        btnAprobar = new JButton("Aprobar");
        btnAprobar.setPreferredSize(new Dimension(120, 35));
        btnAprobar.setBackground(new Color(76, 175, 80));
        btnAprobar.setForeground(Color.WHITE);
        btnAprobar.addActionListener(e -> aprobarEstandar());

        btnAnular = new JButton("Anular");
        btnAnular.setPreferredSize(new Dimension(120, 35));
        btnAnular.setBackground(new Color(255, 152, 0));
        btnAnular.setForeground(Color.WHITE);
        btnAnular.addActionListener(e -> anularEstandar());

        btnGuardar = new JButton("Guardar");
        btnGuardar.setPreferredSize(new Dimension(120, 35));
        btnGuardar.setBackground(new Color(33, 150, 243));
        btnGuardar.setForeground(Color.WHITE);
        btnGuardar.addActionListener(e -> guardarEstandar());

        JButton btnCancelar = new JButton("Cancelar");
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

    private JComboBox<Producto> crearComboProducto() {
        JComboBox<Producto> combo = new JComboBox<>();
        combo.addItem(null);

        List<Producto> productos = productoDP.getForComboBoxDP();
        for (Producto p : productos) {
            combo.addItem(p);
        }

        combo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value == null) {
                    setText("Seleccione un producto...");
                } else {
                    setText(((Producto) value).getPrdDescripcion());
                }
                return this;
            }
        });

        return combo;
    }

    private void anadirDetalle() {
        DetalleEstandar nuevoDetalle = new DetalleEstandar();
        nuevoDetalle.setMxpCantidad(0);
        estandarActual.addDetalle(nuevoDetalle);
        agregarFilaDetalle(nuevoDetalle);
    }

    private void agregarFilaDetalle(DetalleEstandar detalle) {
        Object[] fila = {
            detalle.getMpDescripcion() != null ? detalle.getMpDescripcion() : "Seleccionar...",
            detalle.getMxpCantidad(),
            detalle.getMpPrecioCompra() != null ? String.format("$%.2f", detalle.getMpPrecioCompra()) : "$0.00",
            String.format("$%.2f", detalle.getCostoDetalle()),
            "Eliminar"
        };
        tableModelDetalles.addRow(fila);
        recalcularTotales();
    }

    private void eliminarDetalle(int fila) {
        if (fila >= 0 && fila < estandarActual.getDetalles().size()) {
            estandarActual.removeDetalle(fila);
            tableModelDetalles.removeRow(fila);
            recalcularTotales();
        }
    }

    private void recalcularTotales() {
        // Remover temporalmente el listener para evitar bucle infinito
        javax.swing.event.TableModelListener[] listeners = tableModelDetalles.getTableModelListeners();
        for (javax.swing.event.TableModelListener listener : listeners) {
            tableModelDetalles.removeTableModelListener(listener);
        }
        
        try {
            // Actualizar valores de la tabla antes de calcular
            actualizarDetallesDesdeTabla();
            
            // Recalcular en el objeto Estandar
            estandarActual.recalcularTotales();
            
            // Actualizar los subtotales en cada fila de la tabla
            for (int i = 0; i < tableModelDetalles.getRowCount() && i < estandarActual.getDetalles().size(); i++) {
                DetalleEstandar detalle = estandarActual.getDetalles().get(i);
                tableModelDetalles.setValueAt(String.format("$%.2f", detalle.getCostoDetalle()), i, 3);
            }
            
            // Actualizar labels de totales
            lblTotalCantidadValor.setText(String.valueOf(estandarActual.getEstQtyTotal()));
            lblTotalCostoValor.setText(String.format("$%.2f", estandarActual.getEstPrecioTotal()));
        } finally {
            // Restaurar el listener
            for (javax.swing.event.TableModelListener listener : listeners) {
                tableModelDetalles.addTableModelListener(listener);
            }
        }
    }

    private void actualizarDetallesDesdeTabla() {
        List<DetalleEstandar> detalles = estandarActual.getDetalles();
        for (int i = 0; i < Math.min(tableModelDetalles.getRowCount(), detalles.size()); i++) {
            try {
                Object cantidadObj = tableModelDetalles.getValueAt(i, 1);
                if (cantidadObj != null) {
                    int cantidad = 0;
                    if (cantidadObj instanceof Integer) {
                        cantidad = (Integer) cantidadObj;
                    } else {
                        cantidad = Integer.parseInt(cantidadObj.toString());
                    }
                    detalles.get(i).setMxpCantidad(cantidad);
                }
            } catch (NumberFormatException e) {
                detalles.get(i).setMxpCantidad(0);
            }
        }
    }

    private void cargarDatos(Estandar estandar) {
        // Seleccionar el producto
        for (int i = 0; i < cmbProducto.getItemCount(); i++) {
            Producto p = cmbProducto.getItemAt(i);
            if (p != null && p.getPrdCodigo().equals(estandar.getPrdCodigo())) {
                cmbProducto.setSelectedIndex(i);
                break;
            }
        }

        // Cargar detalles
        for (DetalleEstandar detalle : estandar.getDetalles()) {
            agregarFilaDetalle(detalle);
        }
    }

    private void configurarEstadoBotones() {
        boolean esNuevo = (estandarActual.getEstCod() == null || estandarActual.getEstCod().trim().isEmpty());
        boolean esEditable = estandarActual.esEditable();
        
        // Si es nuevo, todo es editable
        if (esNuevo) {
            cmbProducto.setEnabled(true);
            btnAnadirDetalle.setEnabled(true);
            tableDetalles.setEnabled(true);
            btnGuardar.setEnabled(true);
            btnAprobar.setEnabled(true);
            btnAnular.setEnabled(false); // No se puede anular algo que no existe
        } else {
            // Si ya existe
            cmbProducto.setEnabled(false); // Producto no se puede cambiar una vez guardado
            btnAnadirDetalle.setEnabled(esEditable);
            tableDetalles.setEnabled(esEditable);
            
            // Botones según estado
            if ("PEN".equals(estandarActual.getEstEstado())) {
                btnGuardar.setEnabled(true);
                btnAprobar.setEnabled(true);
                btnAnular.setEnabled(true);
            } else {
                btnGuardar.setEnabled(false);
                btnAprobar.setEnabled(false);
                btnAnular.setEnabled(false);
            }
        }
    }

    private void guardarEstandar() {
        guardarConEstado("PEN");
    }

    private void aprobarEstandar() {
        int confirm = JOptionPane.showConfirmDialog(this,
            "¿Está seguro de aprobar este estándar? No podrá editarlo después.",
            "Confirmar Aprobación",
            JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            guardarConEstado("APR");
        }
    }

    private void anularEstandar() {
        int confirm = JOptionPane.showConfirmDialog(this,
            "¿Está seguro de anular este estándar? No podrá editarlo después.",
            "Confirmar Anulación",
            JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            guardarConEstado("ANU");
        }
    }

    private void guardarConEstado(String estado) {
        try {
            // Actualizar producto seleccionado
            Producto productoSeleccionado = (Producto) cmbProducto.getSelectedItem();
            if (productoSeleccionado == null) {
                JOptionPane.showMessageDialog(this, "Debe seleccionar un producto", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            estandarActual.setPrdCodigo(productoSeleccionado.getPrdCodigo());

            // Actualizar detalles desde la tabla
            actualizarDetallesDesdeTabla();
            
            // Establecer estado
            estandarActual.setEstEstado(estado);

            // Validar
            String error = estandarActual.validate();
            if (error != null) {
                JOptionPane.showMessageDialog(this, error, "Error de validación", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Guardar
            if (estandarActual.saveDP()) {
                String mensaje = estado.equals("PEN") ? "Estándar guardado como Pendiente" :
                                estado.equals("APR") ? "Estándar aprobado exitosamente" :
                                "Estándar anulado exitosamente";
                JOptionPane.showMessageDialog(this, mensaje);
                vistaAnterior.refrescarDatos();
                // Usar el CardLayout del padre del padre (MenuPrincipal)
                Container parent = vistaAnterior.getParent();
                if (parent != null && parent.getLayout() instanceof CardLayout) {
                    ((CardLayout)parent.getLayout()).show(parent, "ESTANDAR");
                }
            } else {
                JOptionPane.showMessageDialog(this, "Error al guardar el estándar", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void cancelar() {
        Container parent = vistaAnterior.getParent();
        if (parent != null && parent.getLayout() instanceof CardLayout) {
            ((CardLayout)parent.getLayout()).show(parent, "ESTANDAR");
        }
    }

    // Editor de ComboBox para materias primas en la tabla
    class ComboBoxCellEditor extends AbstractCellEditor implements TableCellEditor {
        private JComboBox<MateriaPrima> combo;

        public ComboBoxCellEditor() {
            combo = new JComboBox<>();
            List<MateriaPrima> materias = materiaPrimaDP.getForComboBoxDP();
            for (MateriaPrima mp : materias) {
                combo.addItem(mp);
            }
            combo.setRenderer(new DefaultListCellRenderer() {
                @Override
                public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                    super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                    if (value instanceof MateriaPrima) {
                        setText(((MateriaPrima) value).getMpDescripcion());
                    }
                    return this;
                }
            });
            combo.addActionListener(e -> {
                int fila = tableDetalles.getSelectedRow();
                if (fila >= 0) {
                    MateriaPrima mp = (MateriaPrima) combo.getSelectedItem();
                    if (mp != null) {
                        DetalleEstandar detalle = estandarActual.getDetalles().get(fila);
                        detalle.setMpCodigo(mp.getMpCodigo());
                        detalle.setMpDescripcion(mp.getMpDescripcion());
                        detalle.setMpPrecioCompra(mp.getMpPrecioCompra());
                        tableModelDetalles.setValueAt(String.format("$%.2f", mp.getMpPrecioCompra()), fila, 2);
                        tableModelDetalles.setValueAt(String.format("$%.2f", detalle.getCostoDetalle()), fila, 3);
                    }
                }
            });
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            return combo;
        }

        @Override
        public Object getCellEditorValue() {
            MateriaPrima mp = (MateriaPrima) combo.getSelectedItem();
            return mp != null ? mp.getMpDescripcion() : "";
        }
    }

    // Editor personalizado para la columna Cantidad
    class CantidadCellEditor extends AbstractCellEditor implements TableCellEditor {
        private JTextField textField;
        private int filaActual;

        public CantidadCellEditor() {
            textField = new JTextField();
            textField.setHorizontalAlignment(JTextField.RIGHT);
            
            // Listener para actualizar cuando pierde el foco
            textField.addFocusListener(new java.awt.event.FocusAdapter() {
                public void focusLost(java.awt.event.FocusEvent evt) {
                    actualizarCantidad();
                }
            });
            
            // Listener para actualizar cuando presiona Enter
            textField.addActionListener(e -> {
                actualizarCantidad();
                fireEditingStopped();
            });
        }

        private void actualizarCantidad() {
            try {
                String texto = textField.getText().trim();
                if (!texto.isEmpty()) {
                    int cantidad = Integer.parseInt(texto);
                    if (cantidad >= 0 && filaActual < estandarActual.getDetalles().size()) {
                        DetalleEstandar detalle = estandarActual.getDetalles().get(filaActual);
                        detalle.setMxpCantidad(cantidad);
                        
                        // Actualizar el subtotal en la tabla
                        tableModelDetalles.setValueAt(cantidad, filaActual, 1);
                        tableModelDetalles.setValueAt(String.format("$%.2f", detalle.getCostoDetalle()), filaActual, 3);
                        
                        // Recalcular totales generales
                        SwingUtilities.invokeLater(() -> {
                            estandarActual.recalcularTotales();
                            lblTotalCantidadValor.setText(String.valueOf(estandarActual.getEstQtyTotal()));
                            lblTotalCostoValor.setText(String.format("$%.2f", estandarActual.getEstPrecioTotal()));
                        });
                    }
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(EstandarForm.this, 
                    "La cantidad debe ser un número válido", 
                    "Error", 
                    JOptionPane.ERROR_MESSAGE);
            }
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            filaActual = row;
            textField.setText(value != null ? value.toString() : "0");
            return textField;
        }

        @Override
        public Object getCellEditorValue() {
            try {
                return Integer.parseInt(textField.getText().trim());
            } catch (NumberFormatException e) {
                return 0;
            }
        }
    }

    class ComboBoxCellRenderer implements TableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            JLabel label = new JLabel(value != null ? value.toString() : "");
            if (isSelected) {
                label.setBackground(table.getSelectionBackground());
                label.setOpaque(true);
            }
            return label;
        }
    }

    // Renderer para botón eliminar
    class ButtonCellRenderer extends JButton implements TableCellRenderer {
        public ButtonCellRenderer() {
            setText("Eliminar");
            setBackground(new Color(244, 67, 54));
            setForeground(Color.WHITE);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            return this;
        }
    }

    // Editor para botón eliminar
    class ButtonCellEditor extends DefaultCellEditor {
        private JButton button;
        private int filaActual;

        public ButtonCellEditor(JCheckBox checkBox) {
            super(checkBox);
            button = new JButton("Eliminar");
            button.setBackground(new Color(244, 67, 54));
            button.setForeground(Color.WHITE);
            button.addActionListener(e -> {
                fireEditingStopped();
                eliminarDetalle(filaActual);
            });
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            filaActual = row;
            return button;
        }

        @Override
        public Object getCellEditorValue() {
            return "Eliminar";
        }
    }
}