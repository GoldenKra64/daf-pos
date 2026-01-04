package com.daf.view.producto;

import com.daf.controller.Categoria;
import com.daf.controller.Producto;
import com.daf.controller.UnidadMedida;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.util.Properties;

public class ProductoForm extends JPanel {

    private Properties props;

    private Connection conn;
    private ProductoView vistaAnterior;
    private Producto productoActual;
    private JTextField txtImg;
    private UnidadMedida unidadMedidaDP;
    private Categoria categoriaDP;

    // Campos
    private JComboBox<UnidadMedida> cmbUM;
    private JComboBox<Categoria> cmbCategoria;
    private JTextField txtDescripcion;
    private JTextField txtPrecioVenta;
    private JSpinner spnStock;
    private JComboBox<String> cmbPrioridad;
    private JSpinner spnPromocion;

    private JButton btnGuardar, btnCancelar;

    // Labels de error
    private JLabel lblErrorUM, lblErrorDesc, lblErrorPrecio, lblErrorStock, lblErrorPrioridad;

    public ProductoForm(Connection conn, ProductoView vistaAnterior, Producto producto) {
        this.conn = conn;
        this.vistaAnterior = vistaAnterior;
        this.productoActual = producto;

        this.unidadMedidaDP = new UnidadMedida(conn);
        this.categoriaDP = new Categoria(conn);

        loadProperties();
        initComponents();
        if (producto != null) {
            cargarDatos(producto);
        }
    }

    private void loadProperties() {
        props = new Properties();
        try (FileInputStream fis = new FileInputStream("src/main/resources/config.properties")) {
            props.load(fis);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Font fontPlain() {
        return new Font(
                props.getProperty("FONT_FAMILY"),
                Font.PLAIN,
                Integer.parseInt(props.getProperty("FONT_SIZE"))
        );
    }

    private Font fontBold() {
        return new Font(
                props.getProperty("FONT_FAMILY"),
                Font.BOLD,
                Integer.parseInt(props.getProperty("FONT_SIZE"))
        );
    }

    private Font fontTitle() {
        return new Font(
                props.getProperty("FONT_FAMILY"),
                Font.BOLD,
                Integer.parseInt(props.getProperty("FONT_SIZE_TITLE"))
        );
    }

    private Font fontError() {
        return new Font(
                props.getProperty("FONT_FAMILY"),
                Font.PLAIN,
                Integer.parseInt(props.getProperty("FONT_SIZE_ERROR"))
        );
    }

    // ---- Helpers para “ancho real” (por caracteres) ----
    private JTextField buildTextField(int columns) {
        JTextField tf = new JTextField(columns);
        tf.setFont(fontPlain());
        Dimension pref = tf.getPreferredSize();
        pref.height = 30;
        tf.setPreferredSize(pref);
        tf.setMaximumSize(pref); // clave: no se estira
        return tf;
    }

    private JPanel wrapInput(JComponent input) {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        row.setBackground(Color.WHITE);
        row.add(input);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        return row;
    }

    private void sizeSpinner(JSpinner spn, int columns) {
        spn.setFont(fontPlain());
        try {
            JSpinner.DefaultEditor ed = (JSpinner.DefaultEditor) spn.getEditor();
            JFormattedTextField tf = ed.getTextField();
            tf.setColumns(columns);
            tf.setFont(fontPlain());

            Dimension pref = tf.getPreferredSize();
            pref.height = 30;

            Dimension spPref = spn.getPreferredSize();
            spPref.width = pref.width;
            spPref.height = 30;

            spn.setPreferredSize(spPref);
            spn.setMaximumSize(spPref); // no se estira
        } catch (Exception ignored) {}
    }
    
    private void fixComboWidth(JComboBox<?> combo, String sampleText, int height) {
        combo.setFont(fontPlain());
        FontMetrics fm = combo.getFontMetrics(combo.getFont());

        // +45 aprox: icono flecha + padding interno
        int width = fm.stringWidth(sampleText) + 45;

        Dimension d = combo.getPreferredSize();
        d.width = width;
        d.height = height;

        combo.setPreferredSize(d);
        combo.setMaximumSize(d); // clave para que no se estire
    }


    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        setOpaque(false);

        JPanel panelPrincipal = new JPanel();
        panelPrincipal.setLayout(new BoxLayout(panelPrincipal, BoxLayout.Y_AXIS));
        panelPrincipal.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));
        panelPrincipal.setBackground(Color.WHITE);

        JLabel lblTitulo = new JLabel(productoActual == null ? "NUEVO PRODUCTO" : "EDITAR PRODUCTO");
        lblTitulo.setFont(fontTitle());
        lblTitulo.setAlignmentX(Component.CENTER_ALIGNMENT);
        panelPrincipal.add(lblTitulo);
        panelPrincipal.add(Box.createRigidArea(new Dimension(0, 30)));

        // UM (CHAR(10)) -> el combo lo controlamos con prototype
        panelPrincipal.add(crearCampo("Unidad de Medida (Venta):", wrapInput(crearComboUM())));
        lblErrorUM = crearLabelError();
        panelPrincipal.add(lblErrorUM);
        panelPrincipal.add(Box.createRigidArea(new Dimension(0, 15)));

        // Categoría (CHAR(10), opcional)
        panelPrincipal.add(crearCampo("Categoría :", wrapInput(crearComboCategoria())));
        panelPrincipal.add(Box.createRigidArea(new Dimension(0, 15)));

        // Descripción CHAR(60) -> visible ~40
        txtDescripcion = buildTextField(40);
        txtDescripcion.getDocument().addDocumentListener(new ValidationDocumentListener(this::validarDescripcion));
        panelPrincipal.add(crearCampo("Descripción:", wrapInput(txtDescripcion)));
        lblErrorDesc = crearLabelError();
        panelPrincipal.add(lblErrorDesc);
        panelPrincipal.add(Box.createRigidArea(new Dimension(0, 15)));

        // Precio DECIMAL(8,2) -> visible ~10 (ej: 123456.78)
        txtPrecioVenta = buildTextField(10);
        txtPrecioVenta.getDocument().addDocumentListener(new ValidationDocumentListener(this::validarPrecio));
        panelPrincipal.add(crearCampo("Precio de Venta:", wrapInput(txtPrecioVenta)));
        lblErrorPrecio = crearLabelError();
        panelPrincipal.add(lblErrorPrecio);
        panelPrincipal.add(Box.createRigidArea(new Dimension(0, 15)));

        // Stock INT4 -> visible ~6
        spnStock = new JSpinner(new SpinnerNumberModel(0, 0, Integer.MAX_VALUE, 1));
        sizeSpinner(spnStock, 6);
        spnStock.addChangeListener(e -> validarStock());
        panelPrincipal.add(crearCampo("Stock:", wrapInput(spnStock)));
        lblErrorStock = crearLabelError();
        panelPrincipal.add(lblErrorStock);
        panelPrincipal.add(Box.createRigidArea(new Dimension(0, 15)));

        // Prioridad CHAR(1) -> combo corto
        cmbPrioridad = new JComboBox<>(new String[]{"Seleccione...", "F - FIFO", "L - LIFO"});
        cmbPrioridad.setFont(fontPlain());
        cmbPrioridad.setPrototypeDisplayValue("Seleccione...      "); // controla ancho
        Dimension pr = cmbPrioridad.getPreferredSize();
        pr.height = 30;
        cmbPrioridad.setPreferredSize(pr);
        cmbPrioridad.setMaximumSize(pr);
        cmbPrioridad.addActionListener(e -> validarPrioridad());
        panelPrincipal.add(crearCampo("Prioridad:", wrapInput(cmbPrioridad)));
        lblErrorPrioridad = crearLabelError();
        panelPrincipal.add(lblErrorPrioridad);
        panelPrincipal.add(Box.createRigidArea(new Dimension(0, 15)));

        // Imagen VARCHAR(255) -> visible ~45
        txtImg = buildTextField(45);
        panelPrincipal.add(crearCampo("Ruta/URL de Imagen :", wrapInput(txtImg)));
        panelPrincipal.add(Box.createRigidArea(new Dimension(0, 15)));

        // Promoción INT4 -> visible ~6
        spnPromocion = new JSpinner(new SpinnerNumberModel(0, 0, Integer.MAX_VALUE, 1));
        sizeSpinner(spnPromocion, 6);
        panelPrincipal.add(crearCampo("Promoción :", wrapInput(spnPromocion)));
        panelPrincipal.add(Box.createRigidArea(new Dimension(0, 30)));

        // Botones (igual)
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        panelBotones.setBackground(Color.WHITE);

        btnGuardar = new JButton("Guardar");
        btnGuardar.setBackground(new Color(76, 175, 80));
        btnGuardar.setForeground(Color.WHITE);
        btnGuardar.setPreferredSize(new Dimension(120, 35));
        btnGuardar.setFont(fontBold());
        btnGuardar.addActionListener(e -> guardar());

        btnCancelar = new JButton("Cancelar");
        btnCancelar.setBackground(new Color(244, 67, 54));
        btnCancelar.setForeground(Color.WHITE);
        btnCancelar.setPreferredSize(new Dimension(120, 35));
        btnCancelar.setFont(fontBold());
        btnCancelar.addActionListener(e -> cancelar());

        panelBotones.add(btnGuardar);
        panelBotones.add(btnCancelar);
        panelPrincipal.add(panelBotones);

        JScrollPane sp = new JScrollPane(panelPrincipal);
        sp.setBorder(null);
        add(sp, BorderLayout.CENTER);
    }

    // =================== Helpers UI ===================

    private JPanel crearCampo(String etiqueta, JComponent componente) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));
        panel.setBackground(Color.WHITE);

        JLabel lbl = new JLabel(etiqueta);
        lbl.setFont(fontPlain());
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(lbl);
        panel.add(Box.createRigidArea(new Dimension(0, 5)));

        componente.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(componente);

        return panel;
    }

    private JLabel crearLabelError() {
        JLabel lbl = new JLabel(" ");
        lbl.setForeground(Color.RED);
        lbl.setFont(fontError());
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        lbl.setMaximumSize(new Dimension(Integer.MAX_VALUE, 20));
        return lbl;
    }

    // =================== COMBOS ===================

    private JComboBox<UnidadMedida> crearComboUM() {
        cmbUM = new JComboBox<>();
        cmbUM.addItem(null);

        for (UnidadMedida um : unidadMedidaDP.getAllDP()) {
            cmbUM.addItem(um);
        }

        cmbUM.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(
                    JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                setText(value == null ? "Seleccione una unidad..." :
                        ((UnidadMedida) value).getUmDescripcion());
                setFont(fontPlain());
                return this;
            }
        });

        fixComboWidth(cmbUM, "Seleccione una unidad...", 30);

        cmbUM.addActionListener(e -> validarUM());
        return cmbUM;
    }


    private JComboBox<Categoria> crearComboCategoria() {
        cmbCategoria = new JComboBox<>();
        cmbCategoria.addItem(null);

        for (Categoria c : categoriaDP.getAllDP()) {
            cmbCategoria.addItem(c);
        }

        cmbCategoria.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(
                    JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                setText(value == null ? "Sin categoría" :
                        ((Categoria) value).getCatDescripcion());
                setFont(fontPlain());
                return this;
            }
        });

        fixComboWidth(cmbCategoria, "Sin categoría", 30);

        return cmbCategoria;
    }


    // =================== VALIDACIONES ===================

    private void validarUM() {
        lblErrorUM.setText(cmbUM.getSelectedItem() == null ?
                "Debe seleccionar una unidad de medida" : " ");
    }

    private void validarDescripcion() {
        String t = txtDescripcion.getText().trim();
        if (t.isEmpty()) lblErrorDesc.setText("La descripción es obligatoria");
        else if (t.length() > 60) lblErrorDesc.setText("Máximo 60 caracteres");
        else lblErrorDesc.setText(" ");
    }

    private void validarPrecio() {
        try {
            double v = Double.parseDouble(txtPrecioVenta.getText().trim());
            lblErrorPrecio.setText(v < 0 ? "Debe ser positivo" : " ");
        } catch (Exception e) {
            lblErrorPrecio.setText("Número inválido");
        }
    }

    private void validarStock() {
        lblErrorStock.setText((Integer) spnStock.getValue() < 0 ? "Debe ser positivo" : " ");
    }

    private void validarPrioridad() {
        lblErrorPrioridad.setText(cmbPrioridad.getSelectedIndex() == 0 ? "Seleccione prioridad" : " ");
    }

    // =================== GUARDAR (sin cambios) ===================

    private void guardar() {
        validarUM();
        validarDescripcion();
        validarPrecio();
        validarStock();
        validarPrioridad();

        if (!lblErrorUM.getText().trim().isEmpty()
                || !lblErrorDesc.getText().trim().isEmpty()
                || !lblErrorPrecio.getText().trim().isEmpty()
                || !lblErrorStock.getText().trim().isEmpty()
                || !lblErrorPrioridad.getText().trim().isEmpty()) {

            JOptionPane.showMessageDialog(this,
                    "Corrija los errores antes de guardar",
                    "Errores de validación",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            Producto p = (productoActual == null) ? new Producto(conn) : productoActual;

            p.setUmVenta(((UnidadMedida) cmbUM.getSelectedItem()).getUmCodigo());

            Categoria c = (Categoria) cmbCategoria.getSelectedItem();
            p.setCatCodigo(c == null ? null : c.getCatCodigo());

            p.setPrdDescripcion(txtDescripcion.getText().trim());
            p.setPrdPrecioVenta(Double.parseDouble(txtPrecioVenta.getText().trim()));
            p.setPrdStock((Integer) spnStock.getValue());
            p.setPrdPrioridad(cmbPrioridad.getSelectedItem().toString().startsWith("F") ? "F" : "L");
            p.setPrdEstado("ACT");

            int promo = (Integer) spnPromocion.getValue();
            p.setPrdPromocion(promo == 0 ? null : promo);

            String error = p.validate();
            if (error != null) {
                JOptionPane.showMessageDialog(this, error, "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (p.saveDP()) {
                JOptionPane.showMessageDialog(this, "Producto guardado exitosamente");
                vistaAnterior.refrescarDatos();
                ((CardLayout) vistaAnterior.getParent().getLayout())
                        .show(vistaAnterior.getParent(), "PRODUCTO");
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void cargarDatos(Producto p) {
        for (int i = 0; i < cmbUM.getItemCount(); i++) {
            UnidadMedida um = cmbUM.getItemAt(i);
            if (um != null && um.getUmCodigo().equals(p.getUmVenta())) {
                cmbUM.setSelectedIndex(i);
                break;
            }
        }

        if (p.getCatCodigo() != null) {
            for (int i = 0; i < cmbCategoria.getItemCount(); i++) {
                Categoria c = cmbCategoria.getItemAt(i);
                if (c != null && c.getCatCodigo().equals(p.getCatCodigo())) {
                    cmbCategoria.setSelectedIndex(i);
                    break;
                }
            }
        }

        txtDescripcion.setText(p.getPrdDescripcion());
        txtPrecioVenta.setText(String.valueOf(p.getPrdPrecioVenta()));
        spnStock.setValue(p.getPrdStock());
        cmbPrioridad.setSelectedIndex(p.getPrdPrioridad().equals("F") ? 1 : 2);
        txtImg.setText(p.getPrdImg() == null ? "" : p.getPrdImg().trim());
        spnPromocion.setValue(p.getPrdPromocion() == null ? 0 : p.getPrdPromocion());
    }

    private void cancelar() {
        ((CardLayout) vistaAnterior.getParent().getLayout())
                .show(vistaAnterior.getParent(), "PRODUCTO");
    }

    class ValidationDocumentListener implements DocumentListener {
        private Runnable action;
        public ValidationDocumentListener(Runnable action) { this.action = action; }
        public void insertUpdate(DocumentEvent e) { action.run(); }
        public void removeUpdate(DocumentEvent e) { action.run(); }
        public void changedUpdate(DocumentEvent e) { action.run(); }
    }
}
