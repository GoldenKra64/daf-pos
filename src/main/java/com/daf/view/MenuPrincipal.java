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
import java.util.HashMap;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.border.LineBorder;

public class MenuPrincipal extends JPanel {

    private CardLayout cardLayout;
    private JPanel panelCentral;
    private JPanel panelMenuBotones;

    private Map<String, JPanel> vistas;

    public MenuPrincipal() {
        vistas = new HashMap<>();

        configurarUIManager();
        setLayout(new BorderLayout());
        setBackground(new Color(255, 220, 170));
        crearBarraMenu();
        crearPanelCentral();
        crearMenuBotones();
    }

    private void configurarUIManager() {
        UIManager.put(
                "Button.defaultButtonFollowsFocus",
                Boolean.TRUE
        );
    }

    /* Menu Bar */

    private void crearBarraMenu() {
        JMenuBar menuBar = new JMenuBar();
        menuBar.setBackground(new Color(255, 165, 0));
        menuBar.setOpaque(true);

        menuBar.add(crearMenu("Archivo"));
        menuBar.add(crearMenu("Catalogo"));
        menuBar.add(crearMenu("Proceso"));
        menuBar.add(crearMenu("Salir"));

        add(menuBar, BorderLayout.NORTH);
    }

    private JMenu crearMenu(String titulo) {
        JMenu menu = new JMenu(titulo);
        menu.setFont(new Font("Arial", Font.BOLD, 14));
        return menu;
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

        panelMenuBotones.add(crearBotonMenu("Sistema Independiente", "INDEPENDIENTE"));
        panelMenuBotones.add(crearBotonMenu("Sistema Dependiente", "DEPENDIENTE"));
        panelMenuBotones.add(crearBotonMenu("Otro", "OTRO_1"));
        panelMenuBotones.add(crearBotonMenu("Otro", "OTRO_2"));

        panelCentral.add(panelMenuBotones, "MENU");
        cardLayout.show(panelCentral, "MENU");
    }

    /* Creación de botones */

    private JButton crearBotonMenu(String texto, String accion) {
        JButton boton = new JButton(texto);
        boton.setActionCommand(accion);

        boton.setPreferredSize(new Dimension(100, 50)); // 🔹 50x100
        boton.setFocusable(true);
        boton.setBackground(new Color(255, 165, 0));
        boton.setBorder(new LineBorder(Color.BLACK, 2));
        boton.setFont(new Font("Arial", Font.BOLD, 14));

        return boton;
    }

    /* Métodos del controller */

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
}
