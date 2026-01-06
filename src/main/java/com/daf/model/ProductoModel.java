package com.daf.model;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.daf.controller.Producto;

public class ProductoModel {
    private final Connection conn;

    public ProductoModel(Connection conn) {
        this.conn = conn;
    }

    public String generateNextCode() {
        String sql = "SELECT prd_codigo FROM producto ORDER BY prd_codigo DESC LIMIT 1";
        try (PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            if (rs.next()) {
                String last = rs.getString("prd_codigo");
                if (last == null) return "PRD0000001";
                last = last.trim(); // por ser CHAR(10) puede venir con espacios
                if (last.length() < 4) return "PRD0000001";

                String numStr = last.substring(3).replace(" ", "");
                int num = Integer.parseInt(numStr) + 1;
                return String.format("PRD%07d", num);
            }
            return "PRD0000001";
        } catch (Exception e) {
            System.err.println("Error al generar código producto: " + e.getMessage());
            return "PRD0000001";
        }
    }

    public boolean add(Producto p) {
        String sql =
            "INSERT INTO producto (prd_codigo, um_venta, cat_codigo, prd_descripcion, prd_precio_venta, " +
            "prd_stock, prd_prioridad, prd_img, prd_promocion, prd_estado, prd_fecha_alta) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, p.getPrdCodigo());
            pstmt.setString(2, p.getUmVenta());

            // cat_codigo puede ser null
            if (p.getCatCodigo() == null || p.getCatCodigo().trim().isEmpty()) {
                pstmt.setNull(3, Types.CHAR);
            } else {
                pstmt.setString(3, p.getCatCodigo());
            }

            pstmt.setString(4, p.getPrdDescripcion());
            pstmt.setDouble(5, p.getPrdPrecioVenta());
            pstmt.setInt(6, p.getPrdStock());

            if (p.getPrdPrioridad() == null || p.getPrdPrioridad().trim().isEmpty()) {
                pstmt.setNull(7, Types.CHAR);
            } else {
                pstmt.setString(7, p.getPrdPrioridad());
            }

            if (p.getPrdImg() == null || p.getPrdImg().trim().isEmpty()) {
                pstmt.setNull(8, Types.VARCHAR);
            } else {
                pstmt.setString(8, p.getPrdImg());
            }

            if (p.getPrdPromocion() == null) {
                pstmt.setNull(9, Types.INTEGER);
            } else {
                pstmt.setInt(9, p.getPrdPromocion());
            }

            pstmt.setString(10, p.getPrdEstado());

            // fecha alta: normalmente null al crear; se setea al borrar lógico
            if (p.getPrdFechaAlta() == null) {
                pstmt.setNull(11, Types.DATE);
            } else {
                pstmt.setDate(11, Date.valueOf(p.getPrdFechaAlta()));
            }

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al agregar producto: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // Borrado lógico: estado=INA y fecha_alta=fecha del sistema
    public boolean delete(String prdCodigo) {
        String sql = "UPDATE producto SET prd_estado = 'INA', prd_fecha_alta = CURRENT_DATE WHERE prd_codigo = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, prdCodigo);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al eliminar producto: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public boolean update(String prdCodigo, Producto p) {
        String sql =
            "UPDATE producto SET um_venta = ?, cat_codigo = ?, prd_descripcion = ?, prd_precio_venta = ?, " +
            "prd_stock = ?, prd_prioridad = ?, prd_img = ?, prd_promocion = ?, prd_estado = ? " +
            "WHERE prd_codigo = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, p.getUmVenta());

            if (p.getCatCodigo() == null || p.getCatCodigo().trim().isEmpty()) {
                pstmt.setNull(2, Types.CHAR);
            } else {
                pstmt.setString(2, p.getCatCodigo());
            }

            pstmt.setString(3, p.getPrdDescripcion());
            pstmt.setDouble(4, p.getPrdPrecioVenta());
            pstmt.setInt(5, p.getPrdStock());

            if (p.getPrdPrioridad() == null || p.getPrdPrioridad().trim().isEmpty()) {
                pstmt.setNull(6, Types.CHAR);
            } else {
                pstmt.setString(6, p.getPrdPrioridad());
            }

            if (p.getPrdImg() == null || p.getPrdImg().trim().isEmpty()) {
                pstmt.setNull(7, Types.VARCHAR);
            } else {
                pstmt.setString(7, p.getPrdImg());
            }

            if (p.getPrdPromocion() == null) {
                pstmt.setNull(8, Types.INTEGER);
            } else {
                pstmt.setInt(8, p.getPrdPromocion());
            }

            pstmt.setString(9, p.getPrdEstado());
            pstmt.setString(10, prdCodigo);

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al actualizar producto: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public List<Producto> getAll() {
        List<Producto> productos = new ArrayList<>();
        String sql =
            "SELECT prd_codigo, um_venta, cat_codigo, prd_descripcion, prd_precio_venta, prd_stock, " +
            "prd_prioridad, prd_img, prd_promocion, prd_estado, prd_fecha_alta " +
            "FROM producto WHERE prd_estado = 'ACT' ORDER BY prd_codigo";

        try (PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                Date fecha = rs.getDate("prd_fecha_alta");
                LocalDate fechaAlta = (fecha == null) ? null : fecha.toLocalDate();

                Producto p = new Producto(
                    rs.getString("prd_codigo"),
                    rs.getString("um_venta"),
                    rs.getString("cat_codigo"),
                    rs.getString("prd_descripcion"),
                    rs.getDouble("prd_precio_venta"),
                    rs.getInt("prd_stock"),
                    rs.getString("prd_prioridad"),
                    rs.getString("prd_img"),
                    (Integer) rs.getObject("prd_promocion"),
                    rs.getString("prd_estado"),
                    fechaAlta,
                    conn
                );
                productos.add(p);
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener productos: " + e.getMessage());
            e.printStackTrace();
        }

        return productos;
    }

    public List<Producto> getByName(String desc) {
        List<Producto> productos = new ArrayList<>();
        String sql =
            "SELECT prd_codigo, um_venta, cat_codigo, prd_descripcion, prd_precio_venta, prd_stock, " +
            "prd_prioridad, prd_img, prd_promocion, prd_estado, prd_fecha_alta " +
            "FROM producto " +
            "WHERE prd_estado = 'ACT' AND LOWER(prd_descripcion) LIKE LOWER(?) " +
            "ORDER BY prd_codigo";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, "%" + desc + "%");
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Date fecha = rs.getDate("prd_fecha_alta");
                LocalDate fechaAlta = (fecha == null) ? null : fecha.toLocalDate();

                Producto p = new Producto(
                    rs.getString("prd_codigo"),
                    rs.getString("um_venta"),
                    rs.getString("cat_codigo"),
                    rs.getString("prd_descripcion"),
                    rs.getDouble("prd_precio_venta"),
                    rs.getInt("prd_stock"),
                    rs.getString("prd_prioridad"),
                    rs.getString("prd_img"),
                    (Integer) rs.getObject("prd_promocion"),
                    rs.getString("prd_estado"),
                    fechaAlta,
                    conn
                );
                productos.add(p);
            }
        } catch (SQLException e) {
            System.err.println("Error al buscar productos: " + e.getMessage());
            e.printStackTrace();
        }

        return productos;
    }
    public List<Producto> getForComboBox() {
        List<Producto> productos = new ArrayList<>();
        String sql = "SELECT prd_codigo, prd_descripcion FROM producto WHERE prd_estado = 'ACT' ORDER BY prd_descripcion";

        try (PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                Producto p = new Producto(
                    rs.getString("prd_codigo"),
                    rs.getString("prd_descripcion"),
                    conn
                );
                productos.add(p);
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener productos: " + e.getMessage());
            e.printStackTrace();
        }

        return productos;
    }
    public String getNombreByCodigo(String prdCodigo) {
        String sql = "SELECT prd_descripcion FROM producto WHERE prd_codigo = ?";
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, prdCodigo);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return rs.getString("prd_descripcion");
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener nombre de producto: " + e.getMessage());
        }
        
        return null;
    }
}