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
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import com.daf.controller.Proveedor;
import com.daf.model.Ciudad;
import com.daf.model.CiudadModel;
import com.daf.model.ProveedorModel;
import com.daf.view.MenuPrincipal;

public class ProveedorForm extends JPanel {

    private final Connection conn;
    private final MenuPrincipal menu;
    private final ProveedorView vistaAnterior;
    private Proveedor proveedorActual;

    private JComboBox<Ciudad> cboCiudad;
    private JTextField txtRazonSocial;
    private JTextField txtRuc;
    private JTextField txtTelefono;
    private JTextField txtCelular;
    private JTextField txtMail;
    private JTextField txtDireccion;

    /* ================= CONSTRUCTOR ================= */

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

        cargarCiudades();

        if (proveedorActual != null) {
            cargarDatos();
            seleccionarCiudadProveedor(proveedorActual.getCtCodigo());
            System.out.println("DEBUG EDITAR");
        }
    }

    /* ================= PANEL ================= */

    private JPanel crearPanelFormulario() {

        cboCiudad = new JComboBox<>();

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setPreferredSize(new Dimension(580, 520));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(180, 180, 180)),
                BorderFactory.createEmptyBorder(25, 35, 30, 35)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

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
        agregarCampo(panel, gbc, fila++, "Ciudad:", cboCiudad);

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

    /* ================= CIUDADES ================= */

    private void cargarCiudades() {
        CiudadModel model = new CiudadModel(conn);
        cboCiudad.removeAllItems();
        for (Ciudad c : model.getAll()) {
            cboCiudad.addItem(c);
        }
    }

    private void seleccionarCiudadProveedor(String ctCodigo) {
        if (ctCodigo == null) return;
        for (int i = 0; i < cboCiudad.getItemCount(); i++) {
            Ciudad c = cboCiudad.getItemAt(i);
            if (c.getCtCodigo().equals(ctCodigo)) {
                cboCiudad.setSelectedIndex(i);
                break;
            }
        }
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
            java.awt.Component campo
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

        Ciudad ciudad = (Ciudad) cboCiudad.getSelectedItem();
        if (ciudad == null) {
            JOptionPane.showMessageDialog(
                    this,
                    "Debe seleccionar una ciudad",
                    "Validación",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        p.setCtCodigo(ciudad.getCtCodigo());
        p.setPrvRazonSocial(txtRazonSocial.getText().trim());
        p.setPrvRuc(txtRuc.getText().trim());
        p.setPrvTelefono(txtTelefono.getText().trim());
        p.setPrvCelular(txtCelular.getText().trim());
        p.setPrvMail(txtMail.getText().trim());
        p.setPrvDireccion(txtDireccion.getText().trim());
        

        String error = p.validate();
        if (error != null) {
            JOptionPane.showMessageDialog(this, error, "Validación", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // ================= VALIDAR RUC DUPLICADO (CREAR) =================
        if (proveedorActual == null) {

            ProveedorModel proveedorModel = new ProveedorModel(conn);
            String ruc = p.getPrvRuc();

            if (proveedorModel.existsByRuc(ruc)) {
                JOptionPane.showMessageDialog(
                        this,
                        "El RUC ingresado ya se encuentra registrado.\nNo se permiten proveedores duplicados.",
                        "RUC duplicado",
                        JOptionPane.ERROR_MESSAGE
                );
                return;
            }
        }

    // ================= VALIDAR RUC DUPLICADO (EDITAR) =================
    if (proveedorActual != null) {

        ProveedorModel proveedorModel = new ProveedorModel(conn);
        String ruc = p.getPrvRuc();
        String codigoActual = p.getPrvCodigo();

        // DEBUG CORRECTO (AQUÍ SÍ EXISTEN)
        System.out.println("DEBUG EDITAR");
        System.out.println("RUC = " + ruc);
        System.out.println("CODIGO ACTUAL = [" + codigoActual + "]");

        if (proveedorModel.existsByRucExceptCodigo(ruc, codigoActual)) {
            JOptionPane.showMessageDialog(
                    this,
                    "El RUC ingresado ya pertenece a otro proveedor.\nNo se permiten duplicados.",
                    "RUC duplicado",
                    JOptionPane.ERROR_MESSAGE
            );
            return;
        }
    }



        if (p.saveDP()) {
            JOptionPane.showMessageDialog(this, "Proveedor guardado correctamente");
            vistaAnterior.refrescarDatos();
            volver();
        } else {
            JOptionPane.showMessageDialog(this, "Error al guardar proveedor", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void volver() {
        menu.mostrarVista("PROVEEDOR");
    }
}
