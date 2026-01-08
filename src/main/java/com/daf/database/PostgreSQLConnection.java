package com.daf.database;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class PostgreSQLConnection {
    Properties props = new Properties();

    public PostgreSQLConnection(){
        loadProperties();
    }

    private void loadProperties(){
        try (FileInputStream in = new FileInputStream("src/main/resources/config.properties")) {
            props.load(in);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Connection conectar(String usuario, String password) throws SQLException {
        String connectionString = props.getProperty("CONNECTION_STRING") +
                props.getProperty("IP") + ":" +
                props.getProperty("PORT") + "/" +
                props.getProperty("DATABASE");
        return DriverManager.getConnection(connectionString, usuario, password);
    }
}