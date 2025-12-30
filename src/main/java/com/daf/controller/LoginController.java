package com.daf.controller;

import java.sql.Connection;
import java.sql.SQLException;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import com.daf.model.LoginModel;
import com.daf.view.LoginView;
import com.daf.view.MenuPrincipal;

public class LoginController {

    private LoginView view;
    private LoginModel model;

    public LoginController(LoginView view) {
        this.view = view;
        this.model = new LoginModel();

        this.view.getBtnIngresar().addActionListener(e -> autenticar());
    }

    private void autenticar() {
        String usuario = view.getUsuario();
        String password = view.getPassword();

        try {
            Connection conn = model.autenticar(usuario, password);

            view.mostrarMensaje(
                "Bienvenido al sistema",
                JOptionPane.INFORMATION_MESSAGE
            );

            // Pasar la conexión al resto del sistema
            MenuPrincipal menuPrincipal = new MenuPrincipal(conn);

            JFrame frame = view.getFrame();
            frame.setContentPane(menuPrincipal);
            frame.revalidate();
            frame.repaint();

        } catch (SQLException ex) {
            view.mostrarMensaje(
                "Usuario o contraseña incorrectos",
                JOptionPane.ERROR_MESSAGE
            );
        }
    }
}