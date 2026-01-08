package com.daf.model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CiudadModel {

    private final Connection conn;

    public CiudadModel(Connection conn) {
        this.conn = conn;
    }

    public List<Ciudad> getAll() {

        List<Ciudad> lista = new ArrayList<>();

        String sql = "SELECT ct_codigo, ct_descripcion FROM ciudad ORDER BY ct_descripcion";

        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(
                    new Ciudad(
                        rs.getString("ct_codigo"),
                        rs.getString("ct_descripcion")
                    )
                );
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return lista;
    }
}
