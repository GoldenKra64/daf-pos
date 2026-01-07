package com.daf.controller;

import java.sql.Connection;
import java.sql.SQLException;

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

        try {
            // 1️⃣ Autenticación
            Connection conn = model.autenticar(usuario, password);

            // 2️⃣ Mensaje OK
            JOptionPane.showMessageDialog(
                view.getFrame(),
                "Bienvenido al sistema",
                "Acceso correcto",
                JOptionPane.INFORMATION_MESSAGE
            );

            // 3️⃣ Crear menú principal
            MenuPrincipal menuPrincipal = new MenuPrincipal(conn);

            // 4️⃣ Reemplazar contenido del JFrame (ESTE ERA EL BLOQUEO)
            JFrame frame = view.getFrame();
            frame.setContentPane(menuPrincipal);
            frame.revalidate();
            frame.repaint();

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(
                view.getFrame(),
                "Usuario o contraseña incorrectos",
                "Error",
                JOptionPane.ERROR_MESSAGE
            );
        }
    }
}
