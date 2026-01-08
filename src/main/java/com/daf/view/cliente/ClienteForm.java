package com.daf.view.cliente;

import com.daf.controller.Cliente;
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
    private JTextField txtCedula;
    private JTextField txtNombre;
    private JTextField txtApellido;
    private JTextField txtTelefono;
    private JTextField txtEmail;
    private JTextField txtDireccion;

    private JButton btnGuardar, btnCancelar;

    // Labels de error 
    private JLabel lblErrorCedula, lblErrorNombre, lblErrorApellido, lblErrorEmail;

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

    
    private Font fontPlain() {
        return new Font(props.getProperty("FONT_FAMILY"), Font.PLAIN, Integer.parseInt(props.getProperty("FONT_SIZE")));
    }

    private Font fontBold() {
        return new Font(props.getProperty("FONT_FAMILY"), Font.BOLD, Integer.parseInt(props.getProperty("FONT_SIZE")));
    }

    private Font fontTitle() {
        return new Font(props.getProperty("FONT_FAMILY"), Font.BOLD, Integer.parseInt(props.getProperty("FONT_SIZE_TITLE")));
    }

    private Font fontError() {
        return new Font(props.getProperty("FONT_FAMILY"), Font.PLAIN, Integer.parseInt(props.getProperty("FONT_SIZE_ERROR")));
    }

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

        // Cédula / RUC
        txtCedula = buildTextField(15);
        txtCedula.getDocument().addDocumentListener(new ValidationDocumentListener(this::validarCedula));
        panelPrincipal.add(crearCampo("Cédula / RUC *:", wrapInput(txtCedula)));
        lblErrorCedula = crearLabelError();
        panelPrincipal.add(lblErrorCedula);
        panelPrincipal.add(Box.createRigidArea(new Dimension(0, 15)));

        // Nombre
        txtNombre = buildTextField(35);
        txtNombre.getDocument().addDocumentListener(new ValidationDocumentListener(this::validarNombre));
        panelPrincipal.add(crearCampo("Nombre *:", wrapInput(txtNombre)));
        lblErrorNombre = crearLabelError();
        panelPrincipal.add(lblErrorNombre);
        panelPrincipal.add(Box.createRigidArea(new Dimension(0, 15)));

        // Apellido
        txtApellido = buildTextField(35);
        txtApellido.getDocument().addDocumentListener(new ValidationDocumentListener(this::validarApellido));
        panelPrincipal.add(crearCampo("Apellido *:", wrapInput(txtApellido)));
        lblErrorApellido = crearLabelError();
        panelPrincipal.add(lblErrorApellido);
        panelPrincipal.add(Box.createRigidArea(new Dimension(0, 15)));

        // Teléfono
        txtTelefono = buildTextField(15);
        panelPrincipal.add(crearCampo("Teléfono:", wrapInput(txtTelefono)));
        panelPrincipal.add(Box.createRigidArea(new Dimension(0, 15)));

        // Email
        txtEmail = buildTextField(40);
        txtEmail.getDocument().addDocumentListener(new ValidationDocumentListener(this::validarEmail));
        panelPrincipal.add(crearCampo("Email:", wrapInput(txtEmail)));
        lblErrorEmail = crearLabelError();
        panelPrincipal.add(lblErrorEmail);
        panelPrincipal.add(Box.createRigidArea(new Dimension(0, 15)));

        // Dirección
        txtDireccion = buildTextField(45);
        panelPrincipal.add(crearCampo("Dirección:", wrapInput(txtDireccion)));
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

    
    private void validarCedula() {
        String t = txtCedula.getText().trim();
        if (t.isEmpty()) lblErrorCedula.setText("La cédula es obligatoria");
        else if (t.length() > 13) lblErrorCedula.setText("Máximo 13 caracteres");
        else lblErrorCedula.setText(" ");
    }

    private void validarNombre() {
        String t = txtNombre.getText().trim();
        if (t.isEmpty()) lblErrorNombre.setText("El nombre es obligatorio");
        else lblErrorNombre.setText(" ");
    }

    private void validarApellido() {
        String t = txtApellido.getText().trim();
        if (t.isEmpty()) lblErrorApellido.setText("El apellido es obligatorio");
        else lblErrorApellido.setText(" ");
    }

    private void validarEmail() {
        String t = txtEmail.getText().trim();
        if (!t.isEmpty() && !t.contains("@")) lblErrorEmail.setText("Email inválido");
        else lblErrorEmail.setText(" ");
    }

    private void guardar() {
        // 1. Ejecutar validaciones visuales
        validarCedula();
        validarNombre();
        validarApellido();

        // 2. Verificar si hay mensajes de error en los labels
        if (!lblErrorCedula.getText().trim().isEmpty() || 
            !lblErrorNombre.getText().trim().isEmpty() || 
            !lblErrorApellido.getText().trim().isEmpty()) {
            
            JOptionPane.showMessageDialog(this, "Por favor, corrija los errores marcados en rojo", "Validación", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            // 3. Preparar el objeto Cliente
            Cliente c = (clienteActual == null) ? new Cliente(conn) : clienteActual;
            
            // Si es nuevo, asignamos código automático
            if (clienteActual == null) {
                c.setCliCodigo(c.generateCodeDP());
            }

            // Mapeo de campos del formulario al objeto
            c.setCliCedula(txtCedula.getText().trim());
            c.setCliNombre(txtNombre.getText().trim());
            c.setCliApellido(txtApellido.getText().trim());
            c.setCliTelefono(txtTelefono.getText().trim());
            c.setCliEmail(txtEmail.getText().trim());
            c.setCliDireccion(txtDireccion.getText().trim());
            c.setCliEstado("ACT");

            // 4. Ejecutar la persistencia según el caso (Insert o Update)
            boolean exito;
            if (clienteActual == null) {
                exito = c.saveDP(); // Nuevo registro
            } else {
                exito = c.updateDP(); // Edición de existente
            }

            // 5. Respuesta al usuario
            if (exito) {
                JOptionPane.showMessageDialog(this, "Cliente guardado con éxito", "Sistema", JOptionPane.INFORMATION_MESSAGE);
                vistaAnterior.refrescarDatos(); // Actualiza la tabla principal
                cancelar(); // Regresa a la vista de la tabla
            } else {
                JOptionPane.showMessageDialog(this, "No se pudo guardar el cliente en la base de datos", "Error SQL", JOptionPane.ERROR_MESSAGE);
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error crítico: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void cargarDatos(Cliente c) {
        txtCedula.setText(c.getCliCedula());
        txtNombre.setText(c.getCliNombre());
        txtApellido.setText(c.getCliApellido());
        txtTelefono.setText(c.getCliTelefono() == null ? "" : c.getCliTelefono().trim());
        txtEmail.setText(c.getCliEmail() == null ? "" : c.getCliEmail().trim());
        txtDireccion.setText(c.getCliDireccion() == null ? "" : c.getCliDireccion().trim());
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