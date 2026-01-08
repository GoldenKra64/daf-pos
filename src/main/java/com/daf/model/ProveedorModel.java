package com.daf.model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;

import com.daf.controller.Proveedor;

public class ProveedorModel {

    private final Connection conn;

    public ProveedorModel(Connection conn) {
        this.conn = conn;
    }

    /* =========================================================
       GENERAR CÓDIGO  PRV001
       ========================================================= */
    public String generateNextCode() {

        final String sql =
            "SELECT TRIM(prv_codigo) AS codigo " +
            "FROM proveedor " +
            "ORDER BY TRIM(prv_codigo) DESC " +
            "LIMIT 1";

        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                String ultimo = rs.getString("codigo");
                if (ultimo == null || ultimo.isBlank()) return "PRV001";

                String numStr = ultimo.replace("PRV", "").trim();
                int num = Integer.parseInt(numStr);
                return String.format("PRV%03d", num + 1);
            }

            return "PRV001";

        } catch (Exception e) {
            e.printStackTrace();
            return "PRV001";
        }
    }

    /* =========================================================
       INSERT
       ========================================================= */
    public boolean add(Proveedor p) {

        final String sql =
            "INSERT INTO proveedor (" +
            "  prv_codigo, ct_codigo, prv_razonsocial, prv_ruc, prv_telefono, " +
            "  prv_celular, prv_mail, prv_direccion, prv_estado, prv_fecha_alta" +
            ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, CURRENT_DATE)";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, p.getPrvCodigo());
            ps.setString(2, p.getCtCodigo());
            ps.setString(3, p.getPrvRazonSocial());
            ps.setString(4, p.getPrvRuc());
            ps.setString(5, p.getPrvTelefono());
            ps.setString(6, p.getPrvCelular());
            ps.setString(7, p.getPrvMail());
            ps.setString(8, p.getPrvDireccion());
            ps.setString(9, p.getPrvEstado());

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, e.getMessage(), "ERROR SQL REAL", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
            return false;
        }
    }

    /* =========================================================
       UPDATE GENERAL
       ========================================================= */
    public boolean update(String codigo, Proveedor p) {

        final String sql =
            "UPDATE proveedor SET " +
            " ct_codigo = ?, " +
            " prv_razonsocial = ?, " +
            " prv_ruc = ?, " +
            " prv_telefono = ?, " +
            " prv_mail = ?, " +
            " prv_celular = ?, " +
            " prv_direccion = ?, " +
            " prv_estado = ? " +
            "WHERE prv_codigo = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, p.getCtCodigo());
            ps.setString(2, p.getPrvRazonSocial());
            ps.setString(3, p.getPrvRuc());
            ps.setString(4, p.getPrvTelefono());
            ps.setString(5, p.getPrvMail());
            ps.setString(6, p.getPrvCelular());
            ps.setString(7, p.getPrvDireccion());
            ps.setString(8, p.getPrvEstado());
            ps.setString(9, codigo);

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, e.getMessage(), "ERROR SQL REAL", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
            return false;
        }
    }

    /* =========================================================
       VALIDAR RUC DUPLICADO (ACTIVOS)
       ========================================================= */
    public boolean existsByRuc(String ruc, String codigoActual) {

        String sql = """
            SELECT 1
            FROM proveedor
            WHERE prv_ruc = ?
              AND prv_estado = 'ACT'
              AND prv_codigo <> ?
            LIMIT 1
        """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, ruc);
            ps.setString(2, codigoActual);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }

        } catch (SQLException e) {
            e.printStackTrace();
            return true;
        }
    }

    /* =========================================================
       BAJA LÓGICA
       ========================================================= */
    public boolean delete(String codigo) {

        final String sql = "UPDATE proveedor SET prv_estado = 'INA' WHERE prv_codigo = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, codigo);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /* =========================================================
       LISTAR ACTIVOS
       ========================================================= */
    public List<Proveedor> getAllActivos() {

        List<Proveedor> list = new ArrayList<>();

        final String sql =
            "SELECT " +
            " TRIM(p.prv_codigo)      AS prv_codigo, " +
            " TRIM(p.ct_codigo)       AS ct_codigo, " +
            " TRIM(p.prv_razonsocial) AS prv_razonsocial, " +
            " TRIM(p.prv_ruc)         AS prv_ruc, " +
            " TRIM(p.prv_telefono)    AS prv_telefono, " +
            " TRIM(p.prv_celular)     AS prv_celular, " +
            " TRIM(p.prv_mail)        AS prv_mail, " +
            " TRIM(p.prv_direccion)   AS prv_direccion, " +
            " TRIM(p.prv_estado)      AS prv_estado, " +
            " TRIM(c.ct_descripcion)  AS ciudad " +
            "FROM proveedor p " +
            "JOIN ciudad c ON c.ct_codigo = p.ct_codigo " +
            "WHERE p.prv_estado = 'ACT' " +
            "ORDER BY p.prv_razonsocial";

        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {

                Proveedor p = new Proveedor(
                    rs.getString("prv_codigo"),
                    rs.getString("ct_codigo"),
                    rs.getString("prv_razonsocial"),
                    rs.getString("prv_ruc"),
                    rs.getString("prv_telefono"),
                    rs.getString("prv_celular"),
                    rs.getString("prv_mail"),
                    rs.getString("prv_direccion"),
                    rs.getString("prv_estado"),
                    conn
                );

                p.setCiudadDescripcion(rs.getString("ciudad"));
                list.add(p);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }

    /* =========================================================
       BUSCAR ACTIVOS POR FILTRO
       ========================================================= */
    public List<Proveedor> getByFiltroActivos(String texto) {

        List<Proveedor> list = new ArrayList<>();

    final String sql =
        "SELECT " +
            " TRIM(p.prv_codigo)      AS prv_codigo, " +
            " TRIM(p.ct_codigo)       AS ct_codigo, " +
            " TRIM(p.prv_razonsocial) AS prv_razonsocial, " +
            " TRIM(p.prv_ruc)         AS prv_ruc, " +
            " TRIM(p.prv_telefono)    AS prv_telefono, " +
            " TRIM(p.prv_celular)     AS prv_celular, " +
            " TRIM(p.prv_mail)        AS prv_mail, " +
            " TRIM(p.prv_direccion)   AS prv_direccion, " +
            " TRIM(p.prv_estado)      AS prv_estado, " +
            " TRIM(c.ct_descripcion)  AS ciudad " +
        "FROM proveedor p " +
        "JOIN ciudad c ON c.ct_codigo = p.ct_codigo " +
        "WHERE p.prv_estado = 'ACT' " +
            "AND ( " +
            "   TRIM(p.prv_razonsocial) ILIKE ? OR " +
            "   TRIM(p.prv_ruc)         ILIKE ? OR " +
            "   CAST(p.prv_telefono AS TEXT) ILIKE ? OR " +
            "   CAST(p.prv_celular  AS TEXT) ILIKE ? OR " +
            "   TRIM(p.prv_mail)        ILIKE ? OR " +
            "   TRIM(p.prv_direccion)  ILIKE ? OR " +
            "   CAST(p.prv_codigo   AS TEXT) ILIKE ? " +
            ") " +
        "ORDER BY p.prv_razonsocial";


        try (PreparedStatement ps = conn.prepareStatement(sql)) {

            String filtro = "%" + texto.trim() + "%";
            for (int i = 1; i <= 7; i++) {
                ps.setString(i, filtro);
            }


            try (ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {

                    Proveedor p = new Proveedor(
                        rs.getString("prv_codigo"),
                        rs.getString("ct_codigo"),
                        rs.getString("prv_razonsocial"),
                        rs.getString("prv_ruc"),
                        rs.getString("prv_telefono"),
                        rs.getString("prv_celular"),
                        rs.getString("prv_mail"),
                        rs.getString("prv_direccion"),
                        rs.getString("prv_estado"),
                        conn
                    );

                    p.setCiudadDescripcion(rs.getString("ciudad"));
                    list.add(p);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }
}
