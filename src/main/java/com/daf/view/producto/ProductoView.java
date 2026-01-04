package com.daf.view.producto;

import java.awt.*;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.util.List;
import java.util.Properties;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;

import com.daf.controller.Producto;
import com.daf.view.MenuPrincipal;

public class ProductoView extends JPanel {
    private Properties props;

    private Connection conn;
    private MenuPrincipal menuPrincipal;
    private Producto productoDP;

    private JTextField txtBuscar;
    private JButton btnRegresar, btnBuscar, btnCrear;
    private JTable table;
    private DefaultTableModel tableModel;

    private JLabel lblPaginacion;
    private JButton btnAnterior, btnSiguiente;

    private int paginaActual = 1;
    private int REGISTROS_POR_PAGINA;

    private List<Producto> todosLosRegistros;
    private List<Producto> registrosFiltrados;

    public ProductoView(Connection conn, MenuPrincipal menuPrincipal) {
        this.conn = conn;
        this.menuPrincipal = menuPrincipal;
        this.productoDP = new Producto(conn);

        loadProperties();
        REGISTROS_POR_PAGINA = Integer.parseInt(props.getProperty("REGISTROS_POR_PAGINA"));

        initComponents();
        cargarDatos();
    }

    private void loadProperties() {
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

        JPanel panelSuperior = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        panelSuperior.setBackground(new Color(255, 178, 102));

        btnRegresar = new JButton("Regresar");
        btnRegresar.setPreferredSize(new Dimension(120, 35));
        btnRegresar.addActionListener(e -> regresarMenu());
        panelSuperior.add(btnRegresar);

        add(panelSuperior, BorderLayout.NORTH);

        JPanel panelBusqueda = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        panelBusqueda.setBackground(new Color(255, 239, 204));

        txtBuscar = new JTextField(40);
        txtBuscar.setPreferredSize(new Dimension(400, 30));
        panelBusqueda.add(txtBuscar);

        btnBuscar = new JButton("Buscar");
        btnBuscar.setBackground(new Color(255, 178, 102));
        btnBuscar.addActionListener(e -> buscarProducto());
        panelBusqueda.add(btnBuscar);

        btnCrear = new JButton("Crear");
        btnCrear.setBackground(new Color(255, 178, 102));
        btnCrear.addActionListener(e -> abrirFormularioCrear());
        panelBusqueda.add(btnCrear);

        JPanel panelPrincipal = new JPanel(new BorderLayout(10, 10));
        panelPrincipal.setBackground(new Color(255, 239, 204));
        panelPrincipal.add(panelBusqueda, BorderLayout.NORTH);

        String[] columnas = {"UM", "Categoría", "Descripción", "Precio", "Stock", "Prioridad", "Estado", "Acciones"};
        tableModel = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 7; // Acciones
            }
        };

        table = new JTable(tableModel);
        table.setRowHeight(40);
        table.getColumnModel().getColumn(7).setCellRenderer(new ButtonRenderer());
        table.getColumnModel().getColumn(7).setCellEditor(new ButtonEditor(new JCheckBox()));

        JScrollPane scrollPane = new JScrollPane(table);
        panelPrincipal.add(scrollPane, BorderLayout.CENTER);

        add(panelPrincipal, BorderLayout.CENTER);

        JPanel panelPaginacion = new JPanel(new FlowLayout(FlowLayout.CENTER));
        panelPaginacion.setBackground(new Color(255, 239, 204));

        btnAnterior = new JButton("< Anterior");
        btnAnterior.addActionListener(e -> paginaAnterior());
        panelPaginacion.add(btnAnterior);

        lblPaginacion = new JLabel("1/1");
        panelPaginacion.add(lblPaginacion);

        btnSiguiente = new JButton("Siguiente >");
        btnSiguiente.addActionListener(e -> paginaSiguiente());
        panelPaginacion.add(btnSiguiente);

        add(panelPaginacion, BorderLayout.SOUTH);
    }

    private void cargarDatos() {
        todosLosRegistros = productoDP.getAllDP();
        registrosFiltrados = todosLosRegistros;
        paginaActual = 1;
        actualizarTabla();
    }

    private void actualizarTabla() {
        tableModel.setRowCount(0);

        int totalPaginas = (int) Math.ceil((double) registrosFiltrados.size() / REGISTROS_POR_PAGINA);
        if (totalPaginas == 0) totalPaginas = 1;

        int inicio = (paginaActual - 1) * REGISTROS_POR_PAGINA;
        int fin = Math.min(inicio + REGISTROS_POR_PAGINA, registrosFiltrados.size());

        for (int i = inicio; i < fin; i++) {
            Producto p = registrosFiltrados.get(i);

            String cat = (p.getCatCodigo() == null || p.getCatCodigo().trim().isEmpty())
                    ? "-" : p.getCatCodigo().trim();

            Object[] fila = {
                    p.getUmVenta() == null ? "" : p.getUmVenta().trim(),
                    cat,
                    p.getPrdDescripcion() == null ? "" : p.getPrdDescripcion().trim(),
                    String.format("$%.2f", p.getPrdPrecioVenta()),
                    p.getPrdStock(),
                    p.getPrdPrioridad().equals("F") ? "F (FIFO)" : "L (LIFO)",
                    p.getPrdEstado(),
                    p.getPrdCodigo()
            };
            tableModel.addRow(fila);
        }

        lblPaginacion.setText(paginaActual + "/" + totalPaginas);
        btnAnterior.setEnabled(paginaActual > 1);
        btnSiguiente.setEnabled(paginaActual < totalPaginas);
    }

    private void buscarProducto() {
        String busqueda = txtBuscar.getText().trim();
        if (busqueda.isEmpty()) {
            registrosFiltrados = todosLosRegistros;
        } else {
            registrosFiltrados = productoDP.getByNameDP(busqueda);
        }
        paginaActual = 1;
        actualizarTabla();
    }

    private void paginaAnterior() {
        if (paginaActual > 1) {
            paginaActual--;
            actualizarTabla();
        }
    }

    private void paginaSiguiente() {
        int totalPaginas = (int) Math.ceil((double) registrosFiltrados.size() / REGISTROS_POR_PAGINA);
        if (paginaActual < totalPaginas) {
            paginaActual++;
            actualizarTabla();
        }
    }

    private void abrirFormularioCrear() {
        ProductoForm form = new ProductoForm(conn, this, null);
        menuPrincipal.registrarVista("PRODUCTO_FORM", form);
        menuPrincipal.mostrarVista("PRODUCTO_FORM");
    }

    private void abrirFormularioEditar(String prdCodigo) {
        Producto p = null;
        for (Producto x : todosLosRegistros) {
            if (x.getPrdCodigo().trim().equals(prdCodigo.trim())) {
                p = x;
                break;
            }
        }

        if (p != null) {
            ProductoForm form = new ProductoForm(conn, this, p);
            menuPrincipal.registrarVista("PRODUCTO_FORM", form);
            menuPrincipal.mostrarVista("PRODUCTO_FORM");
        }
    }

    private void eliminarProducto(String prdCodigo) {
        int confirmacion = JOptionPane.showConfirmDialog(
                this,
                "¿Está seguro de eliminar este producto?",
                "Confirmar eliminación",
                JOptionPane.YES_NO_OPTION
        );

        if (confirmacion == JOptionPane.YES_OPTION) {
            Producto p = null;
            for (Producto x : todosLosRegistros) {
                if (x.getPrdCodigo().trim().equals(prdCodigo.trim())) {
                    p = x;
                    break;
                }
            }

            if (p != null && p.deleteDP()) {
                JOptionPane.showMessageDialog(this, "Producto eliminado exitosamente");
                cargarDatos();
            } else {
                JOptionPane.showMessageDialog(this, "Error al eliminar el producto", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void regresarMenu() {
        menuPrincipal.regresarMenu();
    }

    public void refrescarDatos() {
        cargarDatos();
    }

    /* Botones en tabla */
    class ButtonRenderer extends JPanel implements TableCellRenderer {
        private JButton btnActualizar;
        private JButton btnEliminar;

        public ButtonRenderer() {
            setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
            setBackground(new Color(255, 178, 102));

            btnActualizar = new JButton("Actualizar");
            btnActualizar.setBackground(new Color(255, 178, 102));
            add(btnActualizar);

            btnEliminar = new JButton("Eliminar");
            btnEliminar.setBackground(new Color(255, 178, 102));
            add(btnEliminar);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                      boolean isSelected, boolean hasFocus, int row, int column) {
            return this;
        }
    }

    class ButtonEditor extends DefaultCellEditor {
        private JPanel panel;
        private JButton btnActualizar;
        private JButton btnEliminar;
        private String prdCodigo;

        public ButtonEditor(JCheckBox checkBox) {
            super(checkBox);
            panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 5));
            panel.setBackground(new Color(255, 178, 102));

            btnActualizar = new JButton("Actualizar");
            btnActualizar.setBackground(new Color(255, 178, 102));
            btnActualizar.addActionListener(e -> {
                fireEditingStopped();
                abrirFormularioEditar(prdCodigo);
            });
            panel.add(btnActualizar);

            btnEliminar = new JButton("Eliminar");
            btnEliminar.setBackground(new Color(255, 178, 102));
            btnEliminar.addActionListener(e -> {
                fireEditingStopped();
                eliminarProducto(prdCodigo);
            });
            panel.add(btnEliminar);
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value,
                                                     boolean isSelected, int row, int column) {
            prdCodigo = (String) value;
            return panel;
        }

        @Override
        public Object getCellEditorValue() {
            return prdCodigo;
        }
    }
}
