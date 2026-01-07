package com.daf.view;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionListener;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.border.LineBorder;

import com.daf.view.kardex.KardexView;
import com.daf.view.materiaprima.MateriaPrimaView;
import com.daf.view.producto.ProductoView;
import com.daf.view.cliente.ClienteView;

public class MenuPrincipal extends JPanel {

    private Properties props = new Properties();
    private CardLayout cardLayout;
    private JMenuBar menuBar;
    private JPanel panelCentral;
    private JPanel panelMenuBotones;

    private Map<String, JPanel> vistas;

    private Connection conn;

    public MenuPrincipal(Connection connection) {
        vistas = new HashMap<>();
        this.conn = connection;

        loadProperties();
        configurarUIManager();
        setLayout(new BorderLayout());
        setBackground(new Color(255, 220, 170));
        crearBarraMenu();
        crearPanelCentral();
        crearMenuBotones();
    }

    private void loadProperties() {
        try (FileInputStream fis = new FileInputStream("src/main/resources/config.properties")) {
            props.load(fis);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void configurarUIManager() {
        UIManager.put(
                "Button.defaultButtonFollowsFocus",
                Boolean.TRUE
        );
    }

    /* Menu Bar */
    private void crearBarraMenu() {
        menuBar = new JMenuBar();
        menuBar.setBackground(new Color(255, 165, 0));
        menuBar.setOpaque(true);

        JMenu menuArchivo = crearMenu("Archivo");
        JMenu menuCatalogo = crearMenu("Catalogo");
        JMenu menuProceso = crearMenu("Proceso");
        JMenu menuSalir = crearMenu("Salir");

        menuArchivo.setToolTipText("Regresa al menu principal");
        menuCatalogo.setToolTipText("Acceder a las clases independientes");
        menuProceso.setToolTipText("Acceder a las clases dependientes");
        menuSalir.setToolTipText("Salir del sistema");

        // ARCHIVO
        menuArchivo.add(crearMenuItem("Menú Principal", e -> regresarMenu()));

        // CATALOGOS
        menuCatalogo.add(crearMenuItem("Materia Prima", e -> abrirMateriaPrima()));
        menuCatalogo.add(crearMenuItem("Producto", e -> abrirProducto()));
        menuCatalogo.add(crearMenuItem("Proveedor", e -> abrirProveedor()));
        menuCatalogo.add(crearMenuItem("Cliente", e -> abrirCliente()));

        // PROCESOS
        menuProceso.add(crearMenuItem("Bodega", e -> abrirBodega()));
        menuProceso.add(crearMenuItem("Orden de Compra", e -> abrirOrdenCompra()));
        menuProceso.add(crearMenuItem("Factura", e -> abrirFactura()));
        menuProceso.add(crearMenuItem("Estandar", e -> abrirEstandar()));

        // SALIR
        menuSalir.add(crearMenuItem("Salir del Sistema", e -> System.exit(0)));

        menuBar.add(menuArchivo);
        menuBar.add(menuCatalogo);
        menuBar.add(menuProceso);
        menuBar.add(menuSalir);

        add(menuBar, BorderLayout.NORTH);
    }


    private JMenu crearMenu(String titulo) {
        JMenu menu = new JMenu(titulo);
        menu.setFont(new Font(props.getProperty("FONT_FAMILY"), Font.BOLD, Integer.parseInt(props.getProperty("FONT_SIZE"))));
        return 
                menu;
    }

    /* Panel central */
    private void crearPanelCentral() {
        cardLayout = new CardLayout();
        panelCentral = new JPanel(cardLayout);
        panelCentral.setOpaque(false);

        add(panelCentral, BorderLayout.CENTER);
    }

    private void crearMenuBotones() {
        panelMenuBotones = new JPanel(new GridLayout(2, 2, 40, 40));
        panelMenuBotones.setOpaque(false);
        panelMenuBotones.setBorder(
                BorderFactory.createEmptyBorder(50, 50, 50, 50)
        );

        // Botones de la interfaz principal
        panelMenuBotones.add(crearBotonMenu("Materia Prima", e -> abrirMateriaPrima()));
        panelMenuBotones.add(crearBotonMenu("Producto", e -> abrirProducto()));
        panelMenuBotones.add(crearBotonMenu("Proveedor", e -> abrirProveedor()));
        panelMenuBotones.add(crearBotonMenu("Cliente", e -> abrirCliente()));

        panelMenuBotones.add(crearBotonMenu("Factura", e -> abrirFactura()));
        panelMenuBotones.add(crearBotonMenu("Órden de Compra", e -> abrirOrdenCompra()));
        panelMenuBotones.add(crearBotonMenu("Estándar", e -> abrirEstandar()));
        panelMenuBotones.add(crearBotonMenu("Bodega", e -> abrirBodega()));


        panelCentral.add(panelMenuBotones, "MENU");
        cardLayout.show(panelCentral, "MENU");
    }

    /* Creación de botones */
    private JButton crearBotonMenu(String texto, ActionListener action) {
        JButton boton = new JButton(texto);
        boton.setPreferredSize(new Dimension(100, 50));
        boton.setFocusable(true);
        boton.setBackground(new Color(255, 165, 0));
        boton.setBorder(new LineBorder(Color.BLACK, 2));
        boton.setFont(new Font(props.getProperty("FONT_FAMILY"), Font.BOLD, Integer.parseInt(props.getProperty("FONT_SIZE"))));
        boton.addActionListener(action);
        return boton;
    }

    public void registrarVista(String nombre, JPanel vista) {
        vista.setOpaque(false);
        vistas.put(nombre, vista);
        panelCentral.add(vista, nombre);
    }

    public void mostrarVista(String nombre) {
        if (vistas.containsKey(nombre)) {
            cardLayout.show(panelCentral, nombre);
        }
    }

    public void setController(ActionListener controller) {
        recorrerComponentes(this, controller);
    }

    private void recorrerComponentes(Container c, ActionListener l) {
        for (Component comp : c.getComponents()) {
            if (comp instanceof JButton) {
                ((JButton) comp).addActionListener(l);
            }
            if (comp instanceof Container) {
                recorrerComponentes((Container) comp, l);
            }
        }
    }

    private JMenuItem crearMenuItem(String texto, ActionListener action) {
        JMenuItem item = new JMenuItem(texto);
        item.addActionListener(action);
        return item;
    }

    // Funcionalidades del menú
    private void abrirMateriaPrima() {
        if (!vistas.containsKey("MATERIA_PRIMA")) {
            registrarVista("MATERIA_PRIMA", new MateriaPrimaView(conn, this));
        }
        mostrarVista("MATERIA_PRIMA");
    }

    private void abrirProducto() {
        if (!vistas.containsKey("PRODUCTO")) {
            registrarVista("PRODUCTO", new ProductoView(conn, this));
        }
        mostrarVista("PRODUCTO");
    }

    private void abrirProveedor() {
        mostrarNoImplementado("Proveedor");
    }

    private void abrirCliente() {
        // Si la vista aún no ha sido creada y guardada en el Map 'vistas'
        if (!vistas.containsKey("CLIENTE")) {
            // Creamos la instancia de tu ClienteView y la registramos
            registrarVista("CLIENTE", new ClienteView(conn, this));
        }
        // Mostramos la vista usando el nombre que le dimos
        mostrarVista("CLIENTE");
    }

    private void abrirBodega() {
        if (!vistas.containsKey("KARDEX")) {
            registrarVista("KARDEX", new KardexView(conn, this));
        }
        mostrarVista("KARDEX");
    }

    private void abrirOrdenCompra() {
        mostrarNoImplementado("Orden de Compra");
    }

    private void abrirFactura() {
        mostrarNoImplementado("Factura");
    }

    private void abrirEstandar() {
        mostrarNoImplementado("Estandar");
    }

    public void regresarMenu() {
        if (!vistas.containsKey("MENU")) {
            registrarVista("MENU", new MenuPrincipal(conn));
        }
        remove(menuBar);
        mostrarVista("MENU");
    }

    private void mostrarNoImplementado(String modulo) {
        JOptionPane.showMessageDialog(
            this,
            "La funcionalidad de " + modulo + " no está implementada.",
            "Funcionalidad no disponible",
            JOptionPane.WARNING_MESSAGE
        );
    }

    public Connection getConnection() {
        return this.conn;
    }
}