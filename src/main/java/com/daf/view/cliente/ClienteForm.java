package com.daf.view.cliente;

import com.daf.controller.Cliente;
import com.daf.controller.Cliente.CiudadItem;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.util.Properties;

public class ClienteForm extends JPanel {

    private Properties props;
    private Connection conn;
    private ClienteView vistaAnterior;
    private Cliente clienteActual;

    // Campos de texto
    private JTextField txtCedula, txtNombre, txtApellido, txtTelefono, txtCelular, txtEmail, txtDireccion;
    private JComboBox<CiudadItem> cmbCiudad;
    private JButton btnGuardar, btnCancelar;

    // Labels de error (estilo ProductoForm)
    private JLabel lblErrorCedula, lblErrorNombre, lblErrorApellido, lblErrorCelular, lblErrorDireccion;

    public ClienteForm(Connection conn, ClienteView vistaAnterior, Cliente cliente) {
        this.conn = conn;
        this.vistaAnterior = vistaAnterior;
        this.clienteActual = cliente;

        loadProperties();
        initComponents();
        if (cliente != null) {
            cargarDatos(cliente);
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

    // =================== CONFIGURACIÓN DE FUENTES ===================
    private Font fontPlain() { return new Font(props.getProperty("FONT_FAMILY", "Arial"), Font.PLAIN, 14); }
    private Font fontBold() { return new Font(props.getProperty("FONT_FAMILY", "Arial"), Font.BOLD, 14); }
    private Font fontTitle() { return new Font(props.getProperty("FONT_FAMILY", "Arial"), Font.BOLD, 20); }
    private Font fontError() { return new Font(props.getProperty("FONT_FAMILY", "Arial"), Font.PLAIN, 12); }

    // =================== HELPERS UI (Estilo ProductoForm) ===================
    private JTextField buildTextField(int columns) {
        JTextField tf = new JTextField(columns);
        tf.setFont(fontPlain());
        Dimension pref = tf.getPreferredSize();
        pref.height = 30;
        tf.setPreferredSize(pref);
        tf.setMaximumSize(pref); 
        return tf;
    }

    private JPanel wrapInput(JComponent input) {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        row.setBackground(Color.WHITE);
        row.add(input);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        return row;
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        setOpaque(false);

        JPanel panelPrincipal = new JPanel();
        panelPrincipal.setLayout(new BoxLayout(panelPrincipal, BoxLayout.Y_AXIS));
        panelPrincipal.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));
        panelPrincipal.setBackground(Color.WHITE);

        JLabel lblTitulo = new JLabel(clienteActual == null ? "NUEVO CLIENTE" : "EDITAR CLIENTE");
        lblTitulo.setFont(fontTitle());
        lblTitulo.setAlignmentX(Component.CENTER_ALIGNMENT);
        panelPrincipal.add(lblTitulo);
        panelPrincipal.add(Box.createRigidArea(new Dimension(0, 30)));

        // Cédula/RUC
        txtCedula = buildTextField(20);
        txtCedula.getDocument().addDocumentListener(new ValidationDocumentListener(this::validarCedula));
        panelPrincipal.add(crearCampo("Cédula/RUC *:", wrapInput(txtCedula)));
        lblErrorCedula = crearLabelError();
        panelPrincipal.add(lblErrorCedula);
        panelPrincipal.add(Box.createRigidArea(new Dimension(0, 15)));

        // Ciudad
        cmbCiudad = new JComboBox<>();
        cargarCiudades();
        cmbCiudad.setFont(fontPlain());
        cmbCiudad.setPreferredSize(new Dimension(300, 30));
        cmbCiudad.setMaximumSize(new Dimension(300, 30));
        panelPrincipal.add(crearCampo("Ciudad *:", wrapInput(cmbCiudad)));
        panelPrincipal.add(Box.createRigidArea(new Dimension(0, 15)));

        // Nombre
        txtNombre = buildTextField(30);
        txtNombre.getDocument().addDocumentListener(new ValidationDocumentListener(this::validarNombre));
        panelPrincipal.add(crearCampo("Nombre *:", wrapInput(txtNombre)));
        lblErrorNombre = crearLabelError();
        panelPrincipal.add(lblErrorNombre);
        panelPrincipal.add(Box.createRigidArea(new Dimension(0, 15)));

        // Apellido
        txtApellido = buildTextField(30);
        txtApellido.getDocument().addDocumentListener(new ValidationDocumentListener(this::validarApellido));
        panelPrincipal.add(crearCampo("Apellido *:", wrapInput(txtApellido)));
        lblErrorApellido = crearLabelError();
        panelPrincipal.add(lblErrorApellido);
        panelPrincipal.add(Box.createRigidArea(new Dimension(0, 15)));

        // Celular
        txtCelular = buildTextField(15);
        txtCelular.getDocument().addDocumentListener(new ValidationDocumentListener(this::validarCelular));
        panelPrincipal.add(crearCampo("Celular (10 dígitos) *:", wrapInput(txtCelular)));
        lblErrorCelular = crearLabelError();
        panelPrincipal.add(lblErrorCelular);
        panelPrincipal.add(Box.createRigidArea(new Dimension(0, 15)));

        // Dirección
        txtDireccion = buildTextField(40);
        txtDireccion.getDocument().addDocumentListener(new ValidationDocumentListener(this::validarDireccion));
        panelPrincipal.add(crearCampo("Dirección *:", wrapInput(txtDireccion)));
        lblErrorDireccion = crearLabelError();
        panelPrincipal.add(lblErrorDireccion);
        panelPrincipal.add(Box.createRigidArea(new Dimension(0, 15)));

        // Otros campos (Opcionales)
        txtTelefono = buildTextField(15);
        panelPrincipal.add(crearCampo("Teléfono Fijo (Opcional):", wrapInput(txtTelefono)));
        panelPrincipal.add(Box.createRigidArea(new Dimension(0, 15)));

        txtEmail = buildTextField(30);
        panelPrincipal.add(crearCampo("Email (Opcional):", wrapInput(txtEmail)));
        panelPrincipal.add(Box.createRigidArea(new Dimension(0, 30)));

        // Botones
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        panelBotones.setBackground(Color.WHITE);

        btnGuardar = new JButton("Guardar");
        btnGuardar.setBackground(new Color(76, 175, 80)); // Verde
        btnGuardar.setForeground(Color.WHITE);
        btnGuardar.setPreferredSize(new Dimension(120, 35));
        btnGuardar.setFont(fontBold());
        btnGuardar.addActionListener(e -> guardar());

        btnCancelar = new JButton("Cancelar");
        btnCancelar.setBackground(new Color(244, 67, 54)); // Rojo
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

    private JPanel crearCampo(String etiqueta, JComponent componente) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Color.WHITE);
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);

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
        return lbl;
    }

    // =================== LÓGICA Y VALIDACIONES ===================

    private void cargarCiudades() {
        cmbCiudad.removeAllItems();
        for (CiudadItem i : new Cliente(conn).getListaCiudades()) {
            cmbCiudad.addItem(i);
        }
    }

    private void validarCedula() {
        String t = txtCedula.getText().trim();
        lblErrorCedula.setText(t.isEmpty() ? "La cédula es obligatoria" : (t.length() < 10 ? "Mínimo 10 dígitos" : " "));
    }

    private void validarNombre() {
        lblErrorNombre.setText(txtNombre.getText().trim().isEmpty() ? "El nombre es obligatorio" : " ");
    }

    private void validarApellido() {
        lblErrorApellido.setText(txtApellido.getText().trim().isEmpty() ? "El apellido es obligatorio" : " ");
    }

    private void validarCelular() {
        String t = txtCelular.getText().trim();
        lblErrorCelular.setText(t.length() != 10 ? "Debe tener 10 dígitos" : " ");
    }

    private void validarDireccion() {
        lblErrorDireccion.setText(txtDireccion.getText().trim().isEmpty() ? "La dirección es obligatoria" : " ");
    }

    private void guardar() {
        validarCedula(); validarNombre(); validarApellido(); validarCelular(); validarDireccion();

        if (!lblErrorCedula.getText().equals(" ") || !lblErrorNombre.getText().equals(" ") || 
            !lblErrorApellido.getText().equals(" ") || !lblErrorCelular.getText().equals(" ") ||
            !lblErrorDireccion.getText().equals(" ")) {
            JOptionPane.showMessageDialog(this, "Corrija los errores marcados en rojo", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            Cliente c = (clienteActual == null) ? new Cliente(conn) : clienteActual;
            if (clienteActual == null) c.setCliCodigo(c.generateCodeDP());

            c.setCtCodigo(((CiudadItem) cmbCiudad.getSelectedItem()).getCodigo());
            c.setCliCedula(txtCedula.getText().trim()); 
            c.setCliNombre(txtNombre.getText().trim().toUpperCase());
            c.setCliApellido(txtApellido.getText().trim().toUpperCase());
            c.setCliCelular(txtCelular.getText().trim()); 
            c.setCliTelefono(txtTelefono.getText().trim());
            c.setCliEmail(txtEmail.getText().trim()); 
            c.setCliDireccion(txtDireccion.getText().trim());
            c.setCliEstado("ACT");

            if (clienteActual == null ? c.saveDP() : c.updateDP()) {
                JOptionPane.showMessageDialog(this, "Cliente guardado exitosamente");
                vistaAnterior.refrescarDatos();
                cancelar();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void cargarDatos(Cliente c) {
        txtCedula.setText(c.getCliCedula()); 
        txtNombre.setText(c.getCliNombre());
        txtApellido.setText(c.getCliApellido()); 
        txtCelular.setText(c.getCliCelular()); 
        txtTelefono.setText(c.getCliTelefono());
        txtEmail.setText(c.getCliEmail()); 
        txtDireccion.setText(c.getCliDireccion());
        
        for (int i = 0; i < cmbCiudad.getItemCount(); i++) {
            if (cmbCiudad.getItemAt(i).getCodigo().equals(c.getCtCodigo())) {
                cmbCiudad.setSelectedIndex(i);
                break;
            }
        }
    }

    private void cancelar() {
        ((CardLayout) vistaAnterior.getParent().getLayout()).show(vistaAnterior.getParent(), "CLIENTE");
    }

    class ValidationDocumentListener implements DocumentListener {
        private Runnable action;
        public ValidationDocumentListener(Runnable action) { this.action = action; }
        public void insertUpdate(DocumentEvent e) { action.run(); }
        public void removeUpdate(DocumentEvent e) { action.run(); }
        public void changedUpdate(DocumentEvent e) { action.run(); }
    }
}