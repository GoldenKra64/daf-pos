package com.daf.model;

import com.daf.controller.MateriaPrima;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MateriaPrimaModel {
    private Connection conn;

    public MateriaPrimaModel(Connection conn) {
        this.conn = conn;
    }

    // Generar el siguiente código MP####
    public String generateNextCode() {
        String sql = "SELECT mp_codigo FROM materia_prima ORDER BY mp_codigo DESC LIMIT 1";
        try (PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            if (rs.next()) {
                String lastCode = rs.getString("mp_codigo").substring(2).replace(" ", "");
                int number = Integer.parseInt(lastCode) + 1;
                return String.format("MP%04d", number);
            } else {
                return "MP0001";
            }
        } catch (SQLException e) {
            System.err.println("Error al generar código: " + e.getMessage());
            return "MP0001";
        }
    }

    // Agregar nueva materia prima
    public boolean add(MateriaPrima mp) {
        String sql = "INSERT INTO materia_prima (mp_codigo, um_compra, mp_descripcion, " +
                     "mp_precio_compra, mp_cantidad, mp_prioridad, mp_estado) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, mp.getMpCodigo());
            pstmt.setString(2, mp.getUmCompra());
            pstmt.setString(3, mp.getMpDescripcion());
            pstmt.setDouble(4, mp.getMpPrecioCompra());
            pstmt.setInt(5, mp.getMpCantidad());
            pstmt.setString(6, mp.getMpPrioridad());
            pstmt.setString(7, mp.getMpEstado());

            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error al agregar materia prima: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // Eliminar (cambiar estado a INA)
    public boolean delete(String mpCod) {
        String sql = "UPDATE materia_prima SET mp_estado = 'INA' WHERE mp_codigo = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, mpCod);
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error al eliminar materia prima: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // Actualizar materia prima
    public boolean update(String mpCod, MateriaPrima mp) {
        String sql = "UPDATE materia_prima SET um_compra = ?, mp_descripcion = ?, " +
                     "mp_precio_compra = ?, mp_cantidad = ?, mp_prioridad = ?, mp_estado = ? " +
                     "WHERE mp_codigo = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, mp.getUmCompra());
            pstmt.setString(2, mp.getMpDescripcion());
            pstmt.setDouble(3, mp.getMpPrecioCompra());
            pstmt.setInt(4, mp.getMpCantidad());
            pstmt.setString(5, mp.getMpPrioridad());
            pstmt.setString(6, mp.getMpEstado());
            pstmt.setString(7, mpCod);

            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error al actualizar materia prima: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // Obtener todas las materias primas
    public List<MateriaPrima> getAll() {
        List<MateriaPrima> materias = new ArrayList<>();
        String sql = "SELECT mp_codigo, um_compra, mp_descripcion, mp_precio_compra, " +
                     "mp_cantidad, mp_prioridad, mp_estado FROM materia_prima " +
                     "WHERE mp_estado = 'ACT' ORDER BY mp_codigo";

        try (PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                MateriaPrima mp = new MateriaPrima(
                    rs.getString("mp_codigo"),
                    rs.getString("um_compra"),
                    rs.getString("mp_descripcion"),
                    rs.getDouble("mp_precio_compra"),
                    rs.getInt("mp_cantidad"),
                    rs.getString("mp_prioridad"),
                    rs.getString("mp_estado"),
                    conn
                );
                materias.add(mp);
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener materias primas: " + e.getMessage());
            e.printStackTrace();
        }

        return materias;
    }

    // Buscar por nombre
    public List<MateriaPrima> getByName(String mpDesc) {
        List<MateriaPrima> materias = new ArrayList<>();
        String sql = "SELECT mp_codigo, um_compra, mp_descripcion, mp_precio_compra, " +
                     "mp_cantidad, mp_prioridad, mp_estado FROM materia_prima " +
                     "WHERE mp_estado = 'ACT' AND LOWER(mp_descripcion) LIKE LOWER(?) " +
                     "ORDER BY mp_codigo";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, "%" + mpDesc + "%");
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                MateriaPrima mp = new MateriaPrima(
                    rs.getString("mp_codigo"),
                    rs.getString("um_compra"),
                    rs.getString("mp_descripcion"),
                    rs.getDouble("mp_precio_compra"),
                    rs.getInt("mp_cantidad"),
                    rs.getString("mp_prioridad"),
                    rs.getString("mp_estado"),
                    conn
                );
                materias.add(mp);
            }
        } catch (SQLException e) {
            System.err.println("Error al buscar materias primas: " + e.getMessage());
            e.printStackTrace();
        }

        return materias;
    }
}