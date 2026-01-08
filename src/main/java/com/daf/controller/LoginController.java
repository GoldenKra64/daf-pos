package com.daf.controller;

import java.sql.Connection;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import com.daf.model.LoginModel;
import com.daf.view.LoginView;
import com.daf.view.MenuPrincipal;

public class LoginController {

    private final LoginView view;
    private final LoginModel model;

    public LoginController(LoginView view) {
        this.view = view;
        this.model = new LoginModel();

        this.view.getBtnIngresar().addActionListener(e -> autenticar());
    }

    private void autenticar() {

        String usuario = view.getUsuario();
        String password = view.getPassword();

        // 🔎 Autenticación JDBC (ya no lanza SQLException)
        Connection conn = model.autenticar(usuario, password);

        // ❌ Si falla la conexión
        if (conn == null) {
            JOptionPane.showMessageDialog(
                view.getFrame(),
                "Usuario o contraseña incorrectos",
                "Error",
                JOptionPane.ERROR_MESSAGE
            );
            return;
        }

        // ✅ Acceso correcto
        JOptionPane.showMessageDialog(
            view.getFrame(),
            "Bienvenido al sistema",
            "Acceso correcto",
            JOptionPane.INFORMATION_MESSAGE
        );

        // Crear menú principal
        MenuPrincipal menuPrincipal = new MenuPrincipal(conn);

        // Reemplazar contenido del JFrame
        JFrame frame = view.getFrame();
        frame.setContentPane(menuPrincipal);
        frame.revalidate();
        frame.repaint();
    }
}
