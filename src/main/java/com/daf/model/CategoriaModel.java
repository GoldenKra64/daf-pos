package com.daf.model;

import com.daf.controller.Categoria;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CategoriaModel {
    private Connection conn;

    public CategoriaModel(Connection conn) {
        this.conn = conn;
    }

    public List<Categoria> getAll() {
        List<Categoria> categorias = new ArrayList<>();
        String sql = "SELECT cat_codigo, cat_descripcion FROM categoria ORDER BY cat_descripcion";
        try (PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                categorias.add(new Categoria(
                    rs.getString("cat_codigo"),
                    rs.getString("cat_descripcion"),
                    conn
                ));
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener categorías: " + e.getMessage());
            e.printStackTrace();
        }
        return categorias;
    }
}
