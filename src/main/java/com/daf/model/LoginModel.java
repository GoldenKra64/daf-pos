package com.daf.model;

import java.sql.Connection;
import java.sql.SQLException;

import com.daf.database.PostgreSQLConnection;

public class LoginModel {

    private PostgreSQLConnection db;

    public LoginModel() {
        this.db = new PostgreSQLConnection();
    }

    public Connection autenticar(String usuario, String password) {

    System.out.println("=== LOGIN DEBUG ===");
    System.out.println("USUARIO = [" + usuario + "]");
    System.out.println("PASSWORD LENGTH = " + (password == null ? "null" : password.length()));
    System.out.println("===================");

    try {
        return db.conectar(usuario, password);
    } catch (SQLException e) {
        e.printStackTrace();
        return null;
    }
}
}