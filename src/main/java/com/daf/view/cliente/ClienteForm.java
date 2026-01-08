package com.daf.view.cliente;

import com.daf.controller.Cliente;
import com.daf.controller.Cliente.CiudadItem;
import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.sql.Connection;
import java.util.List;

public class ClienteForm extends JPanel {
    private Connection conn;
    private ClienteView vistaAnterior;
    private Cliente clienteActual;
    private JTextField txtCedula, txtNombre, txtTelefono, txtCelular, txtEmail, txtDireccion;
    private JComboBox<CiudadItem> cmbCiudad;
    private JButton btnGuardar, btnCancelar;
    private JLabel lblFechaAlta; // Etiqueta para mostrar la fecha

    public ClienteForm(Connection conn, ClienteView vistaAnterior, Cliente cliente) {
        this.conn = conn; this.vistaAnterior = vistaAnterior; this.clienteActual = cliente;
        initComponents();
        cargarCiudades();
        configurarRestricciones();
        if (cliente != null) cargarDatos(cliente);
    }

    private void configurarRestricciones() {
        aplicarCandado(txtCedula, 13, true, false); 
        aplicarCandado(txtCelular, 10, true, false); // 10 dígitos obligatorio
        aplicarCandado(txtTelefono, 10, true, false); 
        aplicarCandado(txtNombre, 120, false, true); // Solo letras
    }

    private void aplicarCandado(JTextField txt, int max, boolean num, boolean let) {
        txt.addKeyListener(new KeyAdapter() {
            @Override public void keyTyped(KeyEvent e) {
                char c = e.getKeyChar();
                if (num && !Character.isDigit(c)) e.consume();
                if (let && !Character.isLetter(c) && !Character.isSpaceChar(c)) e.consume();
                if (txt.getText().length() >= max) {
                    if (c != KeyEvent.VK_BACK_SPACE && c != KeyEvent.VK_DELETE) e.consume();
                }
            }
        });
    }

    private void initComponents() {
        setLayout(new BorderLayout()); setBackground(Color.WHITE);
        JPanel p = new JPanel(); p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30)); p.setBackground(Color.WHITE);

        JLabel titulo = new JLabel(clienteActual == null ? "NUEVO CLIENTE" : "EDITAR CLIENTE");
        titulo.setFont(new Font("Arial", Font.BOLD, 18)); titulo.setAlignmentX(CENTER_ALIGNMENT);
        p.add(titulo); p.add(Box.createRigidArea(new Dimension(0, 20)));

        txtCedula = new JTextField(20); p.add(crearCampo("Cédula/RUC *", txtCedula));
        cmbCiudad = new JComboBox<>(); p.add(crearCampo("Ciudad *", cmbCiudad));
        txtNombre = new JTextField(20); p.add(crearCampo("Nombre Completo *", txtNombre));
        txtCelular = new JTextField(20); p.add(crearCampo("Celular (10 dígitos) *", txtCelular));
        txtTelefono = new JTextField(20); p.add(crearCampo("Teléfono Fijo (Opcional)", txtTelefono));
        txtEmail = new JTextField(20); p.add(crearCampo("Email (Opcional)", txtEmail));
        txtDireccion = new JTextField(20); p.add(crearCampo("Dirección *", txtDireccion));

        // Etiqueta de Fecha de Alta
        lblFechaAlta = new JLabel(" ");
        lblFechaAlta.setFont(new Font("Arial", Font.ITALIC, 11));
        lblFechaAlta.setForeground(Color.GRAY);
        p.add(lblFechaAlta);
        p.add(Box.createRigidArea(new Dimension(0, 20)));

        JPanel bp = new JPanel(new FlowLayout()); bp.setBackground(Color.WHITE);
        btnGuardar = new JButton("Guardar"); btnGuardar.addActionListener(e -> guardar());
        btnCancelar = new JButton("Cancelar"); btnCancelar.addActionListener(e -> cancelar());
        bp.add(btnGuardar); bp.add(btnCancelar); p.add(bp);

        add(new JScrollPane(p));
    }

    private JPanel crearCampo(String l, JComponent c) {
        JPanel pan = new JPanel(new FlowLayout(FlowLayout.LEFT)); pan.setBackground(Color.WHITE);
        JLabel lbl = new JLabel(l); lbl.setPreferredSize(new Dimension(200, 20));
        c.setPreferredSize(new Dimension(300, 25)); pan.add(lbl); pan.add(c); return pan;
    }

    private void cargarCiudades() {
        for (CiudadItem i : new Cliente(conn).getListaCiudades()) cmbCiudad.addItem(i);
    }

    private void guardar() {
        if (txtCedula.getText().trim().isEmpty() || txtNombre.getText().trim().isEmpty() || 
            txtCelular.getText().trim().length() != 10 || txtDireccion.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Complete Cédula, Nombre, Celular (10 dígitos) y Dirección.");
            return;
        }
        try {
            Cliente c = (clienteActual == null) ? new Cliente(conn) : clienteActual;
            if (clienteActual == null) c.setCliCodigo(c.generateCodeDP());
            c.setCtCodigo(((CiudadItem)cmbCiudad.getSelectedItem()).getCodigo());
            c.setCliCedula(txtCedula.getText().trim()); 
            c.setCliNombre(txtNombre.getText().trim().toUpperCase());
            c.setCliCelular(txtCelular.getText().trim()); 
            c.setCliTelefono(txtTelefono.getText().trim());
            c.setCliEmail(txtEmail.getText().trim()); 
            c.setCliDireccion(txtDireccion.getText().trim());
            c.setCliEstado("ACT");
            if (clienteActual == null ? c.saveDP() : c.updateDP()) {
                JOptionPane.showMessageDialog(this, "Guardado exitosamente"); 
                vistaAnterior.refrescarDatos(); 
                cancelar();
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void cargarDatos(Cliente c) {
        txtCedula.setText(c.getCliCedula()); txtNombre.setText(c.getCliNombre());
        txtCelular.setText(c.getCliCelular()); txtTelefono.setText(c.getCliTelefono());
        txtEmail.setText(c.getCliEmail()); txtDireccion.setText(c.getCliDireccion());
        if (c.getCliFechaAlta() != null) {
            lblFechaAlta.setText("Registrado el: " + c.getCliFechaAlta());
        }
    }

    private void cancelar() { ((CardLayout) vistaAnterior.getParent().getLayout()).show(vistaAnterior.getParent(), "CLIENTE"); }
}