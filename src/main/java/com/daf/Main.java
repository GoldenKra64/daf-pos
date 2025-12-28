package com.daf;

import com.daf.controller.LoginController;
import com.daf.database.PostgreSQLConnection;
import com.daf.view.LoginView;

public class Main {
    public static void main(String[] args) {
        LoginView view = new LoginView();
        PostgreSQLConnection model = new PostgreSQLConnection();
        new LoginController(view, model);

        view.setVisible(true);
    }
}