package com.daf;

import com.daf.controller.LoginController;
import com.daf.view.LoginView;

public class Main {

    public static void main(String[] args) {
        LoginView view = new LoginView();
        LoginController controller = new LoginController(view);
        view.setVisible(true);
    }
}
