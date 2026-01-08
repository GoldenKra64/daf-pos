package com.daf.view.materiaprima;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.io.FileInputStream;
import java.sql.Connection;
import java.util.List;
import java.util.Properties;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import com.daf.controller.MateriaPrima;
import com.daf.controller.UnidadMedida;

public class MateriaPrimaForm extends JPanel {
    private Properties props;

    private Connection conn;
    private MateriaPrimaView vistaAnterior;
    private MateriaPrima materiaPrimaActual;
    private UnidadMedida unidadMedidaDP;

    // Componentes del formulario
    private JComboBox<UnidadMedida> cmbUnidadMedida;
    private JTextField txtDescripcion;
    private JTextField txtPrecioCompra;
    private JSpinner spnCantidad;
    private JComboBox<String> cmbPrioridad;
    private JButton btnGuardar, btnCancelar;

    // Labels de error
    private JLabel lblErrorUnidad;
    private JLabel lblErrorDescripcion;
    private JLabel lblErrorPrecio;
    private JLabel lblErrorCantidad;
    private JLabel lblErrorPrioridad;

    public MateriaPrimaForm(Connection conn, MateriaPrimaView vistaAnterior, MateriaPrima materiaPrima) {
        this.conn = conn;
        this.vistaAnterior = vistaAnterior;
        this.materiaPrimaActual = materiaPrima;
        this.unidadMedidaDP = new UnidadMedida(conn);
        loadProperties();

        initComponents();
        if (materiaPrima != null) {
            cargarDatos(materiaPrima);
        }
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

        // Panel principal con padding
        JPanel panelPrincipal = new JPanel();
        panelPrincipal.setLayout(new BoxLayout(panelPrincipal, BoxLayout.Y_AXIS));
        panelPrincipal.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));
        panelPrincipal.setBackground(Color.WHITE);

        // Título
        JLabel lblTitulo = new JLabel(materiaPrimaActual == null ? "NUEVA MATERIA PRIMA" : "EDITAR MATERIA PRIMA");
        lblTitulo.setFont(new Font(props.getProperty("FONT_FAMILY"), Font.BOLD, Integer.parseInt(props.getProperty("FONT_SIZE_TITLE"))));
        lblTitulo.setAlignmentX(Component.CENTER_ALIGNMENT);
        panelPrincipal.add(lblTitulo);
        panelPrincipal.add(Box.createRigidArea(new Dimension(0, 30)));

        // Unidad de Medida
        panelPrincipal.add(crearCampo("Unidad de Medida:", crearComboUnidadMedida()));
        lblErrorUnidad = crearLabelError();
        panelPrincipal.add(lblErrorUnidad);
        panelPrincipal.add(Box.createRigidArea(new Dimension(0, 15)));

        // Descripción
        txtDescripcion = new JTextField();
        txtDescripcion.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        txtDescripcion.getDocument().addDocumentListener(new ValidationDocumentListener(() -> validarDescripcion()));
        panelPrincipal.add(crearCampo("Descripción:", txtDescripcion));
        lblErrorDescripcion = crearLabelError();
        panelPrincipal.add(lblErrorDescripcion);
        panelPrincipal.add(Box.createRigidArea(new Dimension(0, 15)));

        // Precio de Compra
        txtPrecioCompra = new JTextField();
        txtPrecioCompra.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        txtPrecioCompra.getDocument().addDocumentListener(new ValidationDocumentListener(() -> validarPrecio()));
        panelPrincipal.add(crearCampo("Precio de Compra:", txtPrecioCompra));
        lblErrorPrecio = crearLabelError();
        panelPrincipal.add(lblErrorPrecio);
        panelPrincipal.add(Box.createRigidArea(new Dimension(0, 15)));

        // Cantidad
        SpinnerNumberModel spinnerModel = new SpinnerNumberModel(0, 0, Integer.MAX_VALUE, 1);
        spnCantidad = new JSpinner(spinnerModel);
        spnCantidad.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        spnCantidad.addChangeListener(e -> validarCantidad());
        panelPrincipal.add(crearCampo("Cantidad:", spnCantidad));
        lblErrorCantidad = crearLabelError();
        panelPrincipal.add(lblErrorCantidad);
        panelPrincipal.add(Box.createRigidArea(new Dimension(0, 15)));

        // Prioridad
        String[] prioridades = {"Seleccione...", "FIFO", "LIFO"};
        cmbPrioridad = new JComboBox<>(prioridades);
        cmbPrioridad.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        cmbPrioridad.addActionListener(e -> validarPrioridad());
        panelPrincipal.add(crearCampo("Prioridad:", cmbPrioridad));
        lblErrorPrioridad = crearLabelError();
        panelPrincipal.add(lblErrorPrioridad);
        panelPrincipal.add(Box.createRigidArea(new Dimension(0, 30)));

        // Panel de botones
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        panelBotones.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        panelBotones.setBackground(Color.WHITE);

        btnGuardar = new JButton("Guardar");
        btnGuardar.setPreferredSize(new Dimension(120, 35));
        btnGuardar.setBackground(new Color(76, 175, 80));
        btnGuardar.setForeground(Color.WHITE);
        btnGuardar.setFocusPainted(false);
        btnGuardar.addActionListener(e -> guardarMateriaPrima());
        panelBotones.add(btnGuardar);

        btnCancelar = new JButton("Cancelar");
        btnCancelar.setPreferredSize(new Dimension(120, 35));
        btnCancelar.setBackground(new Color(244, 67, 54));
        btnCancelar.setForeground(Color.WHITE);
        btnCancelar.setFocusPainted(false);
        btnCancelar.addActionListener(e -> cancelar());
        panelBotones.add(btnCancelar);

        panelPrincipal.add(panelBotones);

        JScrollPane scrollPane = new JScrollPane(panelPrincipal);
        add(scrollPane, BorderLayout.CENTER);
    }

    private JComboBox<UnidadMedida> crearComboUnidadMedida() {
        cmbUnidadMedida = new JComboBox<>();
        cmbUnidadMedida.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        cmbUnidadMedida.addItem(null); // Opción vacía

        List<UnidadMedida> unidades = unidadMedidaDP.getAllDP();
        for (UnidadMedida um : unidades) {
            cmbUnidadMedida.addItem(um);
        }

        cmbUnidadMedida.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value == null) {
                    setText("Seleccione una unidad...");
                } else {
                    setText(((UnidadMedida) value).getUmDescripcion());
                }
                return this;
            }
        });

        cmbUnidadMedida.addActionListener(e -> validarUnidadMedida());
        return cmbUnidadMedida;
    }

    private JPanel crearCampo(String etiqueta, JComponent componente) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));
        panel.setBackground(Color.WHITE);

        JLabel lbl = new JLabel(etiqueta);
        lbl.setFont(new Font(props.getProperty("FONT_FAMILY"), Font.PLAIN, Integer.parseInt(props.getProperty("FONT_SIZE"))));
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
        lbl.setFont(new Font(props.getProperty("FONT_FAMILY"), Font.PLAIN, Integer.parseInt(props.getProperty("FONT_SIZE"))));
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        lbl.setMaximumSize(new Dimension(Integer.MAX_VALUE, 20));
        return lbl;
    }

    private void validarUnidadMedida() {
        if (cmbUnidadMedida.getSelectedItem() == null) {
            lblErrorUnidad.setText("Debe seleccionar una unidad de medida");
        } else {
            lblErrorUnidad.setText(" ");
        }
    }

    private void validarDescripcion() {
        String texto = txtDescripcion.getText().trim();
        if (texto.isEmpty()) {
            lblErrorDescripcion.setText("La descripción es obligatoria");
        } else if (texto.length() > 60) {
            lblErrorDescripcion.setText("La descripción no puede exceder 60 caracteres");
        } else {
            lblErrorDescripcion.setText(" ");
        }
    }

    private void validarPrecio() {
        String texto = txtPrecioCompra.getText().trim();
        if (texto.isEmpty()) {
            lblErrorPrecio.setText("El precio es obligatorio");
        } else {
            try {
                double precio = Double.parseDouble(texto);
                if (precio < 0) {
                    lblErrorPrecio.setText("El precio debe ser un valor positivo");
                } else {
                    lblErrorPrecio.setText(" ");
                }
            } catch (NumberFormatException e) {
                lblErrorPrecio.setText("El precio debe ser un número válido");
            }
        }
    }

    private void validarCantidad() {
        int cantidad = (Integer) spnCantidad.getValue();
        if (cantidad < 0) {
            lblErrorCantidad.setText("La cantidad debe ser un valor positivo");
        } else {
            lblErrorCantidad.setText(" ");
        }
    }

    private void validarPrioridad() {
        if (cmbPrioridad.getSelectedIndex() == 0) {
            lblErrorPrioridad.setText("Debe seleccionar una prioridad");
        } else {
            lblErrorPrioridad.setText(" ");
        }
    }

    private void cargarDatos(MateriaPrima mp) {
        // Seleccionar la unidad de medida
        for (int i = 0; i < cmbUnidadMedida.getItemCount(); i++) {
            UnidadMedida um = cmbUnidadMedida.getItemAt(i);
            if (um != null && um.getUmCodigo().equals(mp.getUmCompra())) {
                cmbUnidadMedida.setSelectedIndex(i);
                break;
            }
        }

        txtDescripcion.setText(mp.getMpDescripcion());
        txtPrecioCompra.setText(String.valueOf(mp.getMpPrecioCompra()));
        spnCantidad.setValue(mp.getMpCantidad());

        // Seleccionar prioridad
        if (mp.getMpPrioridad().equals("F")) {
            cmbPrioridad.setSelectedIndex(1);
        } else {
            cmbPrioridad.setSelectedIndex(2);
        }
    }

    private void guardarMateriaPrima() {
        // Validar todos los campos
        validarUnidadMedida();
        validarDescripcion();
        validarPrecio();
        validarCantidad();
        validarPrioridad();

        // Verificar si hay errores
        if (!lblErrorUnidad.getText().trim().isEmpty() ||
            !lblErrorDescripcion.getText().trim().isEmpty() ||
            !lblErrorPrecio.getText().trim().isEmpty() ||
            !lblErrorCantidad.getText().trim().isEmpty() ||
            !lblErrorPrioridad.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Por favor corrija los errores antes de guardar", "Errores de validación", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            MateriaPrima mp;
            if (materiaPrimaActual == null) {
                mp = new MateriaPrima(conn);
            } else {
                mp = materiaPrimaActual;
            }

            UnidadMedida umSeleccionada = (UnidadMedida) cmbUnidadMedida.getSelectedItem();
            mp.setUmCompra(umSeleccionada.getUmCodigo());
            mp.setMpDescripcion(txtDescripcion.getText().trim());
            mp.setMpPrecioCompra(Double.parseDouble(txtPrecioCompra.getText().trim()));
            mp.setMpCantidad((Integer) spnCantidad.getValue());

            String prioridadSeleccionada = (String) cmbPrioridad.getSelectedItem();
            mp.setMpPrioridad(prioridadSeleccionada.startsWith("F") ? "F" : "L");

            // Validar mediante el DP
            String error = mp.validate();
            if (error != null) {
                JOptionPane.showMessageDialog(this, error, "Error de validación", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Guardar
            if (mp.saveDP()) {
                JOptionPane.showMessageDialog(this, "Materia prima guardada exitosamente");
                vistaAnterior.refrescarDatos();
                ((CardLayout)vistaAnterior.getParent().getLayout()).show(vistaAnterior.getParent(), "MATERIA_PRIMA");
            } else {
                JOptionPane.showMessageDialog(this, "Error al guardar la materia prima", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void cancelar() {
        ((CardLayout)vistaAnterior.getParent().getLayout()).show(vistaAnterior.getParent(), "MATERIA_PRIMA");
    }

    // Clase interna para validación en tiempo real
    class ValidationDocumentListener implements DocumentListener {
        private Runnable validationAction;

        public ValidationDocumentListener(Runnable validationAction) {
            this.validationAction = validationAction;
        }

        @Override
        public void insertUpdate(DocumentEvent e) {
            validationAction.run();
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            validationAction.run();
        }

        @Override
        public void changedUpdate(DocumentEvent e) {
            validationAction.run();
        }
    }
}