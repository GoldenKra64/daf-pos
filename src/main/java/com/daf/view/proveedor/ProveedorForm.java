package com.daf.view.proveedor;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.sql.Connection;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import com.daf.controller.Proveedor;
import com.daf.view.MenuPrincipal;

public class ProveedorForm extends JPanel {

    private final Connection conn;
    private final MenuPrincipal menu;
    private final ProveedorView vistaAnterior;
    private Proveedor proveedorActual;

    private JTextField txtRazonSocial;
    private JTextField txtRuc;
    private JTextField txtTelefono;
    private JTextField txtCelular;
    private JTextField txtMail;
    private JTextField txtDireccion;

    // ⚠ Ciudad por defecto (hasta implementar selector)
    private static final String CIUDAD_DEFAULT = "CT01";

    public ProveedorForm(
            Connection conn,
            MenuPrincipal menu,
            ProveedorView vistaAnterior,
            Proveedor proveedor
    ) {
        this.conn = conn;
        this.menu = menu;
        this.vistaAnterior = vistaAnterior;
        this.proveedorActual = proveedor;

        setLayout(new GridBagLayout());
        setOpaque(false);

        add(crearPanelFormulario());

        if (proveedorActual != null) {
            cargarDatos();
        }
    }

    /* ================= PANEL ================= */

    private JPanel crearPanelFormulario() {

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setPreferredSize(new Dimension(580, 480));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(180, 180, 180)),
                BorderFactory.createEmptyBorder(25, 35, 30, 35)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        /* ===== TÍTULO ===== */
        JLabel lblTitulo = new JLabel(
                proveedorActual == null ? "NUEVO PROVEEDOR" : "EDITAR PROVEEDOR",
                SwingConstants.CENTER
        );
        lblTitulo.setFont(new Font("Georgia", Font.BOLD, 20));

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        panel.add(lblTitulo, gbc);
        gbc.gridwidth = 1;

        /* ===== CAMPOS ===== */
        txtRazonSocial = crearCampoTexto();
        txtRuc = crearCampoTexto();
        txtTelefono = crearCampoTexto();
        txtCelular = crearCampoTexto();
        txtMail = crearCampoTexto();
        txtDireccion = crearCampoTexto();

        int fila = 1;
        agregarCampo(panel, gbc, fila++, "Razón Social:", txtRazonSocial);
        agregarCampo(panel, gbc, fila++, "RUC:", txtRuc);
        agregarCampo(panel, gbc, fila++, "Teléfono:", txtTelefono);
        agregarCampo(panel, gbc, fila++, "Celular:", txtCelular);
        agregarCampo(panel, gbc, fila++, "Correo electrónico:", txtMail);
        agregarCampo(panel, gbc, fila++, "Dirección:", txtDireccion);

        /* ===== BOTONES ===== */
        JButton btnGuardar = new JButton("Guardar");
        btnGuardar.setBackground(new Color(76, 175, 80));
        btnGuardar.setForeground(Color.WHITE);
        btnGuardar.addActionListener(e -> guardar());

        JButton btnCancelar = new JButton("Cancelar");
        btnCancelar.setBackground(new Color(244, 67, 54));
        btnCancelar.setForeground(Color.WHITE);
        btnCancelar.addActionListener(e -> volver());

        JPanel panelBotones = new JPanel();
        panelBotones.setBackground(Color.WHITE);
        panelBotones.add(btnGuardar);
        panelBotones.add(btnCancelar);

        gbc.gridx = 0;
        gbc.gridy = fila;
        gbc.gridwidth = 2;
        panel.add(panelBotones, gbc);

        return panel;
    }

    /* ================= CAMPOS ================= */

    private JTextField crearCampoTexto() {
        JTextField txt = new JTextField();
        txt.setPreferredSize(new Dimension(320, 32));
        txt.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(160, 160, 160)),
                BorderFactory.createEmptyBorder(6, 8, 6, 8)
        ));
        return txt;
    }

    private void agregarCampo(
            JPanel panel,
            GridBagConstraints gbc,
            int fila,
            String etiqueta,
            JTextField campo
    ) {
        gbc.gridx = 0;
        gbc.gridy = fila;
        panel.add(new JLabel(etiqueta), gbc);

        gbc.gridx = 1;
        panel.add(campo, gbc);
    }

    /* ================= DATOS ================= */

    private void cargarDatos() {
        txtRazonSocial.setText(proveedorActual.getPrvRazonSocial());
        txtRuc.setText(proveedorActual.getPrvRuc());
        txtTelefono.setText(proveedorActual.getPrvTelefono());
        txtCelular.setText(proveedorActual.getPrvCelular());
        txtMail.setText(proveedorActual.getPrvMail());
        txtDireccion.setText(proveedorActual.getPrvDireccion());
    }

    /* ================= GUARDAR ================= */

    private void guardar() {

        Proveedor p = (proveedorActual == null)
                ? new Proveedor(conn)
                : proveedorActual;

        // ✅ ciudad obligatoria según controller
        p.setCtCodigo(CIUDAD_DEFAULT);

        p.setPrvRazonSocial(txtRazonSocial.getText().trim());
        p.setPrvRuc(txtRuc.getText().trim());
        p.setPrvTelefono(txtTelefono.getText().trim());
        p.setPrvCelular(txtCelular.getText().trim());
        p.setPrvMail(txtMail.getText().trim());
        p.setPrvDireccion(txtDireccion.getText().trim());

        String error = p.validate();
        if (error != null) {
            JOptionPane.showMessageDialog(
                    this,
                    error,
                    "Validación",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        if (p.saveDP()) {
            JOptionPane.showMessageDialog(this, "Proveedor guardado correctamente");
            vistaAnterior.refrescarDatos();
            volver();
        } else {
            JOptionPane.showMessageDialog(
                    this,
                    "Error al guardar proveedor",
                    "Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

 private void volver() {
        menu.mostrarVista("PROVEEDOR");
    }
}
