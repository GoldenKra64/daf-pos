package com.daf.view;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;

public class LoginView extends JFrame {

    private JTextField txtUsuario;
    private JPasswordField txtPassword;
    private JButton btnIngresar;

    public LoginView() {
        setTitle("Ingreso de Usuarios");
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        JPanel panelLogin = new JPanel();
        panelLogin.setPreferredSize(new Dimension(
                (int) (Toolkit.getDefaultToolkit().getScreenSize().width * 0.25),
                Toolkit.getDefaultToolkit().getScreenSize().height
        ));
        panelLogin.setLayout(new GridBagLayout());

        panelLogin.setBackground(new Color(255, 165, 0, 204));
        panelLogin.setOpaque(true);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 20, 10, 20);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel lblTitulo = new JLabel("Ingreso de Usuarios");
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 22));
        lblTitulo.setHorizontalAlignment(SwingConstants.CENTER);

        txtUsuario = new JTextField();
        txtPassword = new JPasswordField();

        btnIngresar = new JButton("INGRESAR");
        btnIngresar.setFont(new Font("Arial", Font.BOLD, 16));
        btnIngresar.setBackground(new Color(255, 140, 0));
        btnIngresar.setForeground(Color.BLACK);
        btnIngresar.setBorder(new LineBorder(Color.BLACK, 2));
        btnIngresar.setFocusPainted(true);
        btnIngresar.setPreferredSize(new Dimension(200, 45));

        txtUsuario.setNextFocusableComponent(txtPassword);
        txtPassword.setNextFocusableComponent(btnIngresar);
        btnIngresar.setNextFocusableComponent(txtUsuario);

        gbc.gridy = 0;
        panelLogin.add(lblTitulo, gbc);

        gbc.gridy++;
        panelLogin.add(new JLabel("Login"), gbc);

        gbc.gridy++;
        panelLogin.add(txtUsuario, gbc);

        gbc.gridy++;
        panelLogin.add(new JLabel("Contraseña"), gbc);

        gbc.gridy++;
        panelLogin.add(txtPassword, gbc);

        gbc.gridy++;
        gbc.insets = new Insets(30, 20, 10, 20);
        panelLogin.add(btnIngresar, gbc);

        JLabel lblImagen = new JLabel();
        lblImagen.setIcon(new ImageIcon(
                getClass().getResource("/images/flores-ingreso.jpg")
        ));
        lblImagen.setHorizontalAlignment(SwingConstants.CENTER);
        lblImagen.setVerticalAlignment(SwingConstants.CENTER);

        add(panelLogin, BorderLayout.WEST);
        add(lblImagen, BorderLayout.CENTER);
    }

    public String getUsuario() {
        return txtUsuario.getText();
    }

    public String getPassword() {
        return new String(txtPassword.getPassword());
    }

    public JButton getBtnIngresar() {
        return btnIngresar;
    }

    public void mostrarMensaje(String mensaje, int tipo) {
        JOptionPane.showMessageDialog(this, mensaje, "Mensaje", tipo);
    }
}