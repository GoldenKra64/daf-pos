package com.daf.controller;

import com.daf.database.PostgreSQLConnection;
import com.daf.view.LoginView;

import java.sql.Connection;
import java.sql.SQLException;

import javax.swing.*;

import com.daf.database.PostgreSQLConnection;

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
                    "Conexión exitosa a la base de datos 🎉",
                    JOptionPane.INFORMATION_MESSAGE
            );

            // Aquí podrías abrir otra ventana
            // view.dispose();

        } catch (SQLException ex) {
            view.mostrarMensaje(
                    "Error de conexión:\nUsuario o contraseña incorrectos",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }
}