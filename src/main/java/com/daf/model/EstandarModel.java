package com.daf.model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.daf.controller.DetalleEstandar;
import com.daf.controller.Estandar;

public class EstandarModel {
    private Connection conn;

    public EstandarModel(Connection conn) {
        this.conn = conn;
    }

    // Generar siguiente código ES######## que detecte
    public String generateNextCode() {
        String sql = "SELECT est_cod FROM estandar ORDER BY est_cod DESC LIMIT 1";
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            if (rs.next()) {
                String lastCode = rs.getString("est_cod");
                long number = Long.parseLong(lastCode.substring(2)) + 1;
                return String.format("ES%08d", number);
            } else {
                return "ES00000001";
            }
        } catch (SQLException e) {
            System.err.println("Error al generar código: " + e.getMessage());
            return "ES00000001";
        }
    }

    // Agregar estándar con sus detalles
    public boolean add(Estandar estandar) {
        String sqlCabecera = "INSERT INTO estandar (est_cod, prd_codigo, est_qty_total, " +
                            "est_precio_total, est_estado) VALUES (?, ?, ?, ?, ?)";

        try {
            conn.setAutoCommit(false);

            // Insertar cabecera
            try (PreparedStatement pstmt = conn.prepareStatement(sqlCabecera)) {
                pstmt.setString(1, estandar.getEstCod());
                pstmt.setString(2, estandar.getPrdCodigo());
                pstmt.setLong(3, estandar.getEstQtyTotal());
                pstmt.setDouble(4, estandar.getEstPrecioTotal());
                pstmt.setString(5, estandar.getEstEstado());
                pstmt.executeUpdate();
            }

            // Insertar detalles
            if (!insertarDetalles(estandar)) {
                conn.rollback();
                conn.setAutoCommit(true);
                return false;
            }

            conn.commit();
            conn.setAutoCommit(true);
            return true;

        } catch (SQLException e) {
            try {
                conn.rollback();
                conn.setAutoCommit(true);
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            System.err.println("Error al agregar estándar: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // Actualizar estándar con sus detalles
    public boolean update(Estandar estandar) {
        String sqlCabecera = "UPDATE estandar SET prd_codigo = ?, est_qty_total = ?, " +
                            "est_precio_total = ?, est_estado = ? WHERE est_cod = ?";

        try {
            conn.setAutoCommit(false);

            // Actualizar cabecera
            try (PreparedStatement pstmt = conn.prepareStatement(sqlCabecera)) {
                pstmt.setString(1, estandar.getPrdCodigo());
                pstmt.setLong(2, estandar.getEstQtyTotal());
                pstmt.setDouble(3, estandar.getEstPrecioTotal());
                pstmt.setString(4, estandar.getEstEstado());
                pstmt.setString(5, estandar.getEstCod());
                pstmt.executeUpdate();
            }

            // Eliminar detalles existentes
            deleteDetalles(estandar.getEstCod());

            // Insertar nuevos detalles
            if (!insertarDetalles(estandar)) {
                conn.rollback();
                conn.setAutoCommit(true);
                return false;
            }

            conn.commit();
            conn.setAutoCommit(true);
            return true;

        } catch (SQLException e) {
            try {
                conn.rollback();
                conn.setAutoCommit(true);
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            System.err.println("Error al actualizar estándar: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // Eliminar estándar y sus detalles
    public boolean delete(String estCod) {
        try {
            conn.setAutoCommit(false);

            // Eliminar detalles
            deleteDetalles(estCod);

            // Eliminar cabecera
            String sql = "DELETE FROM estandar WHERE est_cod = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, estCod);
                pstmt.executeUpdate();
            }

            conn.commit();
            conn.setAutoCommit(true);
            return true;

        } catch (SQLException e) {
            try {
                conn.rollback();
                conn.setAutoCommit(true);
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            System.err.println("Error al eliminar estándar: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // Obtener todos los estándares
    public List<Estandar> getAll() {
        List<Estandar> estandares = new ArrayList<>();
        String sql = "SELECT est_cod, prd_codigo, est_qty_total, est_precio_total, est_estado " +
                     "FROM estandar ORDER BY est_cod DESC";

        try (PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                Estandar e = new Estandar(
                    rs.getString("est_cod"),
                    rs.getString("prd_codigo"),
                    rs.getLong("est_qty_total"),
                    rs.getDouble("est_precio_total"),
                    rs.getString("est_estado"),
                    conn
                );
                estandares.add(e);
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener estándares: " + e.getMessage());
            e.printStackTrace();
        }

        return estandares;
    }

    // Buscar por nombre de producto
    public List<Estandar> getByName(String nombreProducto) {
        List<Estandar> estandares = new ArrayList<>();
        String sql = "SELECT e.est_cod, e.prd_codigo, e.est_qty_total, e.est_precio_total, e.est_estado " +
                     "FROM estandar e " +
                     "INNER JOIN producto p ON e.prd_codigo = p.prd_codigo " +
                     "WHERE LOWER(p.prd_descripcion) LIKE LOWER(?) " +
                     "ORDER BY e.est_cod DESC";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, "%" + nombreProducto + "%");
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Estandar e = new Estandar(
                    rs.getString("est_cod"),
                    rs.getString("prd_codigo"),
                    rs.getLong("est_qty_total"),
                    rs.getDouble("est_precio_total"),
                    rs.getString("est_estado"),
                    conn
                );
                estandares.add(e);
            }
        } catch (SQLException e) {
            System.err.println("Error al buscar estándares: " + e.getMessage());
            e.printStackTrace();
        }

        return estandares;
    }

    // Obtener detalles de un estándar
    public List<DetalleEstandar> getDetalles(String estCod) {
        List<DetalleEstandar> detalles = new ArrayList<>();
        String sql = "SELECT mp_codigo, est_cod, mxp_cantidad FROM detalle_estandar WHERE est_cod = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, estCod);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                DetalleEstandar detalle = new DetalleEstandar(
                    rs.getString("mp_codigo"),
                    rs.getString("est_cod"),
                    rs.getInt("mxp_cantidad")
                );
                detalles.add(detalle);
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener detalles: " + e.getMessage());
            e.printStackTrace();
        }

        return detalles;
    }

    // Insertar detalles
    private boolean insertarDetalles(Estandar estandar) {
        String sql = "INSERT INTO detalle_estandar (mp_codigo, est_cod, mxp_cantidad) VALUES (?, ?, ?)";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            for (DetalleEstandar detalle : estandar.getDetalles()) {
                pstmt.setString(1, detalle.getMpCodigo());
                pstmt.setString(2, estandar.getEstCod());
                pstmt.setInt(3, detalle.getMxpCantidad());
                pstmt.addBatch();
            }
            pstmt.executeBatch();
            return true;
        } catch (SQLException e) {
            System.err.println("Error al insertar detalles: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // Eliminar detalles de un estándar
    private void deleteDetalles(String estCod) throws SQLException {
        String sql = "DELETE FROM detalle_estandar WHERE est_cod = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, estCod);
            pstmt.executeUpdate();
        }
    }
}