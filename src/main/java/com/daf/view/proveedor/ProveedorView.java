package com.daf.view.proveedor;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;

import com.daf.controller.Proveedor;
import com.daf.view.MenuPrincipal;

public class ProveedorView extends JPanel {

    private final Connection conn;
    private final MenuPrincipal menu;
    private final Proveedor proveedorDP;

    private JTable table;
    private DefaultTableModel model;
    private JTextField txtBuscar;

    private List<Proveedor> data = new ArrayList<>();

    public ProveedorView(Connection conn, MenuPrincipal menu) {
        this.conn = conn;
        this.menu = menu;
        this.proveedorDP = new Proveedor(conn);

        setLayout(new BorderLayout(10, 10));
        setOpaque(false);

        crearBarraSuperior();
        crearTabla();
        cargar();
    }

    /* ================= BARRA SUPERIOR ================= */

    private void crearBarraSuperior() {

        JPanel barra = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        barra.setBackground(new Color(255, 178, 102));

        JButton btnRegresar = new JButton("Regresar");
        btnRegresar.addActionListener(e -> menu.mostrarVista("MENU"));


        txtBuscar = new JTextField(25);

        JButton btnBuscar = new JButton("Buscar");
        btnBuscar.addActionListener(e -> buscar());

        JButton btnNuevo = new JButton("Nuevo");
        btnNuevo.addActionListener(e -> abrirForm(null));

        JButton btnEliminar = new JButton("Eliminar");
        btnEliminar.addActionListener(e -> eliminar());

        barra.add(btnRegresar);
        barra.add(txtBuscar);
        barra.add(btnBuscar);
        barra.add(btnNuevo);
        barra.add(btnEliminar);

        add(barra, BorderLayout.NORTH);
    }

    /* ================= TABLA ================= */

    private void crearTabla() {

        model = new DefaultTableModel(
                new String[]{
                        "Razón Social",
                        "RUC",
                        "Teléfono",
                        "Celular",
                        "Correo",
                        "Dirección"
                }, 0
        ) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };

        table = new JTable(model);
        table.setRowHeight(32);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int row = table.getSelectedRow();
                    if (row >= 0 && row < data.size()) {
                        abrirForm(data.get(row));
                    }
                }
            }
        });

        add(new JScrollPane(table), BorderLayout.CENTER);
    }

    /* ================= DATOS ================= */

    private void cargar() {
        data = proveedorDP.getAllDP();
        refrescarTabla();
    }


    private void refrescarTabla() {
        model.setRowCount(0);
        for (Proveedor p : data) {
            model.addRow(new Object[]{
                    p.getPrvRazonSocial(),
                    p.getPrvRuc(),
                    p.getPrvTelefono(),
                    p.getPrvCelular(),
                    p.getPrvMail(),
                    p.getPrvDireccion()
            });
        }
    }

    private void buscar() {
    String texto = txtBuscar.getText().trim();
    data = texto.isEmpty()
            ? proveedorDP.getAllDP()
            : proveedorDP.getByFiltroDP(texto);
    refrescarTabla();
    }



    /* ================= ACCIONES ================= */

    private void abrirForm(Proveedor proveedor) {

        ProveedorForm form = new ProveedorForm(
                conn,
                menu,
                this,
                proveedor
        );

        menu.registrarVista("PROVEEDOR_FORM", form);
        menu.mostrarVista("PROVEEDOR_FORM");
    }

    private void eliminar() {

        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(
                    this,
                    "Seleccione un proveedor",
                    "Aviso",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        if (JOptionPane.showConfirmDialog(
                this,
                "¿Está seguro de eliminar este proveedor?",
                "Confirmar",
                JOptionPane.YES_NO_OPTION
        ) == JOptionPane.YES_OPTION) {

            if (data.get(row).deleteDP()) {
                cargar();
            } else {
                JOptionPane.showMessageDialog(
                        this,
                        "No se pudo eliminar el proveedor",
                        "Error",
                        JOptionPane.ERROR_MESSAGE
                );
            }
        }
    }

    /* ================= API ================= */

    public void refrescarDatos() {
        cargar();
    }
}
