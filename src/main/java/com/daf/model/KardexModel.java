package com.daf.model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.daf.controller.Kardex;

public class KardexModel {
    private Connection conn;

    public KardexModel(Connection conn) {
        this.conn = conn;
    }

    // Actualizar registro de kardex
    public boolean update(String krdCodigo, Kardex kardex) {
        String sql = "UPDATE kardex SET krd_qty_total = ? WHERE krd_codigo = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, kardex.getKrdQtyTotal());
            pstmt.setString(2, krdCodigo);

            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error al actualizar kardex: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // Obtener todos los registros de kardex
    public List<Kardex> getAll() {
        List<Kardex> registros = new ArrayList<>();
        String sql = "SELECT * FROM kardex ORDER BY krd_fechahora DESC, krd_codigo DESC";

        try (PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                Kardex kardex = new Kardex(
                    rs.getString("krd_codigo"),
                    rs.getString("fac_codigo"),
                    rs.getString("oc_codigo"),
                    rs.getInt("krd_cantidad"),
                    rs.getInt("krd_qty_total"),
                    rs.getDate("krd_fechahora"),
                    rs.getString("krd_accion"),
                    rs.getString("usr_id"),
                    conn
                );
                registros.add(kardex);
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener registros de kardex: " + e.getMessage());
            e.printStackTrace();
        }

        return registros;
    }

    // Buscar por código, acción o usuario
    public List<Kardex> getByName(String busqueda) {
        List<Kardex> registros = new ArrayList<>();
        String sql = "SELECT krd_codigo, fac_codigo, oc_codigo, krd_cantidad, " +
                     "krd_qty_total, krd_fechahora, krd_accion, usr_id " +
                     "FROM kardex " +
                     "WHERE LOWER(usr_id) LIKE LOWER(?) " +
                     "ORDER BY krd_fechahora DESC, krd_codigo DESC";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            String param = "%" + busqueda + "%";
            pstmt.setString(1, param);
            
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Kardex kardex = new Kardex(
                    rs.getString("krd_codigo"),
                    rs.getString("fac_codigo"),
                    rs.getString("oc_codigo"),
                    rs.getInt("krd_cantidad"),
                    rs.getInt("krd_qty_total"),
                    rs.getDate("krd_fechahora"),
                    rs.getString("krd_accion"),
                    rs.getString("usr_id"),
                    conn
                );
                registros.add(kardex);
            }
        } catch (SQLException e) {
            System.err.println("Error al buscar registros de kardex: " + e.getMessage());
            e.printStackTrace();
        }

        return registros;
    }
}