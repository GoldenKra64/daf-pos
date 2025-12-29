package com.daf.controller;

import java.sql.Connection;
import java.sql.SQLException;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import com.daf.database.PostgreSQLConnection;
import com.daf.view.LoginView;
import com.daf.view.MenuPrincipal;

public class LoginController {

    private LoginView view;
    private PostgreSQLConnection model;

    public LoginController(LoginView view, PostgreSQLConnection model) {
        this.view = view;
        this.model = model;

        this.view.getBtnIngresar().addActionListener(e -> autenticar());
    }

    private void autenticar() {
        String usuario = view.getUsuario();
        String password = view.getPassword();

        try (Connection conn = model.conectar(usuario, password)) {

            view.mostrarMensaje(
                    "Conexión exitosa a la base de datos",
                    JOptionPane.INFORMATION_MESSAGE
            );

            
            MenuPrincipal menuPrincipal = new MenuPrincipal();
            new MenuController(menuPrincipal);

            JFrame frame = view.getFrame();
            frame.setContentPane(menuPrincipal);
            frame.revalidate();
            frame.repaint();

        } catch (SQLException ex) {
            view.mostrarMensaje(
                    "Error de conexión:\nUsuario o contraseña incorrectos",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }
}