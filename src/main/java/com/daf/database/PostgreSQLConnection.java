package com.daf.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class PostgreSQLConnection {

    private static final String URL =
            "jdbc:postgresql://localhost:5432/tu_base_datos";

    public Connection conectar(String usuario, String password) throws SQLException {
        return DriverManager.getConnection(URL, usuario, password);
    }
}
