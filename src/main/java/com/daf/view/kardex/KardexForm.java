package com.daf.view.kardex;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.Properties;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.SpinnerNumberModel;

import com.daf.controller.Kardex;

public class KardexForm extends JPanel {
    private Properties props;
    private Connection conn;
    private KardexView vistaAnterior;
    private Kardex kardexActual;

    // Componentes del formulario
    private JLabel lblCodigoValor;
    private JLabel lblOrigenValor;
    private JLabel lblCantidadValor;
    private JSpinner spnQtyTotal;
    private JLabel lblFechaValor;
    private JTextArea txtAccion;
    private JLabel lblUsuarioValor;
    private JButton btnGuardar, btnCancelar;

    // Labels de error
    private JLabel lblErrorQtyTotal;
    
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");

    public KardexForm(Connection conn, KardexView vistaAnterior, Kardex kardex) {
        this.conn = conn;
        this.vistaAnterior = vistaAnterior;
        this.kardexActual = kardex;

        loadProperties();
        initComponents();
        cargarDatos(kardex);
    }

    private void loadProperties(){
        props = new Properties();
        try (FileInputStream fis = new FileInputStream("src/main/resources/config.properties")) {
            props.load(fis);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        setOpaque(false);

        // Panel y padding
        JPanel panelPrincipal = new JPanel();
        panelPrincipal.setLayout(new BoxLayout(panelPrincipal, BoxLayout.Y_AXIS));
        panelPrincipal.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));
        panelPrincipal.setBackground(Color.WHITE);

        // Título
        JLabel lblTitulo = new JLabel("ACTUALIZAR REGISTRO DE KARDEX");
        lblTitulo.setFont(new Font(props.getProperty("FONT_FAMILY"), Font.BOLD, Integer.parseInt(props.getProperty("FONT_SIZE_TITLE"))));
        lblTitulo.setAlignmentX(Component.CENTER_ALIGNMENT);
        panelPrincipal.add(lblTitulo);
        panelPrincipal.add(Box.createRigidArea(new Dimension(0, 20)));
    
        // Código
        lblCodigoValor = new JLabel();
        lblCodigoValor.setFont(new Font(props.getProperty("FONT_FAMILY"), Font.PLAIN, Integer.parseInt(props.getProperty("FONT_SIZE"))));
        panelPrincipal.add(crearCampoLectura("Código:", lblCodigoValor));
        panelPrincipal.add(Box.createRigidArea(new Dimension(0, 15)));

        // Origen
        lblOrigenValor = new JLabel();
        lblOrigenValor.setFont(new Font(props.getProperty("FONT_FAMILY"), Font.PLAIN, Integer.parseInt(props.getProperty("FONT_SIZE"))));
        panelPrincipal.add(crearCampoLectura("Origen:", lblOrigenValor));
        panelPrincipal.add(Box.createRigidArea(new Dimension(0, 15)));

        // Cantidad del movimiento
        lblCantidadValor = new JLabel();
        lblCantidadValor.setFont(new Font(props.getProperty("FONT_FAMILY"), Font.PLAIN, Integer.parseInt(props.getProperty("FONT_SIZE"))));
        panelPrincipal.add(crearCampoLectura("Cantidad del Movimiento:", lblCantidadValor));
        panelPrincipal.add(Box.createRigidArea(new Dimension(0, 15)));

        // Cantidad Total
        SpinnerNumberModel spinnerModel = new SpinnerNumberModel(0, 0, Integer.MAX_VALUE, 1);
        spnQtyTotal = new JSpinner(spinnerModel);
        spnQtyTotal.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        ((JSpinner.DefaultEditor) spnQtyTotal.getEditor()).getTextField().setFont(new Font(props.getProperty("FONT_FAMILY"), Font.PLAIN, Integer.parseInt(props.getProperty("FONT_SIZE"))));
        spnQtyTotal.addChangeListener(e -> validarQtyTotal());
        panelPrincipal.add(crearCampo("Cantidad Total en Bodega:", spnQtyTotal));
        lblErrorQtyTotal = crearLabelError();
        panelPrincipal.add(lblErrorQtyTotal);
        panelPrincipal.add(Box.createRigidArea(new Dimension(0, 15)));

        // Fecha
        lblFechaValor = new JLabel();
        lblFechaValor.setFont(new Font(props.getProperty("FONT_FAMILY"), Font.PLAIN, Integer.parseInt(props.getProperty("FONT_SIZE"))));
        panelPrincipal.add(crearCampoLectura("Fecha y Hora:", lblFechaValor));
        panelPrincipal.add(Box.createRigidArea(new Dimension(0, 15)));

        // Acción
        txtAccion = new JTextArea();
        txtAccion.setEditable(false);
        txtAccion.setLineWrap(true);
        txtAccion.setWrapStyleWord(true);
        txtAccion.setFont(new Font(props.getProperty("FONT_FAMILY"), Font.PLAIN, Integer.parseInt(props.getProperty("FONT_SIZE"))));
        txtAccion.setBackground(new Color(240, 240, 240));
        txtAccion.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        JScrollPane scrollAccion = new JScrollPane(txtAccion);
        scrollAccion.setPreferredSize(new Dimension(0, 80));
        scrollAccion.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));
        panelPrincipal.add(crearCampo("Acción:", scrollAccion));
        panelPrincipal.add(Box.createRigidArea(new Dimension(0, 15)));

        // Usuario (solo lectura)
        lblUsuarioValor = new JLabel();
        lblUsuarioValor.setFont(new Font(props.getProperty("FONT_FAMILY"), Font.PLAIN, Integer.parseInt(props.getProperty("FONT_SIZE"))));
        panelPrincipal.add(crearCampoLectura("Usuario:", lblUsuarioValor));
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
        btnGuardar.addActionListener(e -> guardarKardex());
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
        scrollPane.setBorder(null);
        add(scrollPane, BorderLayout.CENTER);
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

    private JPanel crearCampoLectura(String etiqueta, JLabel valorLabel) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        panel.setBackground(Color.WHITE);

        JLabel lbl = new JLabel(etiqueta);
        lbl.setFont(new Font(props.getProperty("FONT_FAMILY"), Font.PLAIN, Integer.parseInt(props.getProperty("FONT_SIZE"))));
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(lbl);
        panel.add(Box.createRigidArea(new Dimension(0, 5)));

        valorLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(valorLabel);

        return panel;
    }

    private JLabel crearLabelError() {
        JLabel lbl = new JLabel(" ");
        lbl.setForeground(Color.RED);
        lbl.setFont(new Font(props.getProperty("FONT_FAMILY"), Font.PLAIN, Integer.parseInt(props.getProperty("FONT_SIZE_ERROR"))));
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        lbl.setMaximumSize(new Dimension(Integer.MAX_VALUE, 20));
        return lbl;
    }

    private void validarQtyTotal() {
        int qtyTotal = (Integer) spnQtyTotal.getValue();
        if (qtyTotal < 0) {
            lblErrorQtyTotal.setText("La cantidad total debe ser un valor positivo");
        } else {
            lblErrorQtyTotal.setText(" ");
        }
    }
    private void cargarDatos(Kardex kardex) {
        lblCodigoValor.setText(kardex.getKrdCodigo());
        lblOrigenValor.setText(kardex.getOrigen());
        lblCantidadValor.setText(String.valueOf(kardex.getKrdCantidad()));
        spnQtyTotal.setValue(kardex.getKrdQtyTotal());
        lblFechaValor.setText(kardex.getKrdFechahora() != null ? 
                              dateFormat.format(kardex.getKrdFechahora()) : "N/A");
        txtAccion.setText(kardex.getKrdAccion());
        lblUsuarioValor.setText(kardex.getUsrId());
    }

    private void guardarKardex() {
        // Validar campo
        validarQtyTotal();

        // Verificar si hay errores
        if (!lblErrorQtyTotal.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "Por favor corrija los errores antes de guardar", 
                "Errores de validación", 
                JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            kardexActual.setKrdQtyTotal((Integer) spnQtyTotal.getValue());

            // Validar mediante el DP
            String error = kardexActual.validate();
            if (error != null) {
                JOptionPane.showMessageDialog(this, error, "Error de validación", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Guardar
            if (kardexActual.updateDP()) {
                JOptionPane.showMessageDialog(this, "Registro de kardex actualizado exitosamente");
                vistaAnterior.refrescarDatos();
                ((CardLayout)vistaAnterior.getParent().getLayout()).show(vistaAnterior.getParent(), "KARDEX");
            } else {
                JOptionPane.showMessageDialog(this, 
                    "Error al actualizar el registro de kardex", 
                    "Error", 
                    JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void cancelar() {
        ((CardLayout)vistaAnterior.getParent().getLayout()).show(vistaAnterior.getParent(), "KARDEX");
    }
}