package com.daf.model;

import com.daf.controller.UnidadMedida;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UnidadMedidaModel {
    private Connection conn;

    public UnidadMedidaModel(Connection conn) {
        this.conn = conn;
    }

    public List<UnidadMedida> getAll() {
        List<UnidadMedida> unidades = new ArrayList<>();
        String sql = "SELECT um_codigo, um_descripcion FROM unidadmedida ORDER BY um_descripcion";

        try (PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                UnidadMedida um = new UnidadMedida(
                    rs.getString("um_codigo"),
                    rs.getString("um_descripcion"),
                    conn
                );
                unidades.add(um);
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener unidades de medida: " + e.getMessage());
            e.printStackTrace();
        }

        return unidades;
    }
}