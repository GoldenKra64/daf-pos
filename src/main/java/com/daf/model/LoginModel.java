package com.daf.model;

import java.sql.Connection;
import java.sql.SQLException;

import com.daf.database.PostgreSQLConnection;

public class LoginModel {

    private PostgreSQLConnection db;

    public LoginModel() {
        this.db = new PostgreSQLConnection();
    }

    public Connection autenticar(String usuario, String password) throws SQLException {
        return db.conectar(usuario, password);
    }
}