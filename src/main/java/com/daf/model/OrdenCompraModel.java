package com.daf.model;

import com.daf.controller.DetalleOC;
import com.daf.controller.OrdenCompra;

import java.io.FileInputStream;
import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class OrdenCompraModel {

    private final Connection conn;
    private final Properties props = new Properties();

    public OrdenCompraModel(Connection conn) {
        this.conn = conn;
        loadProperties();
    }

    private void loadProperties() {
        try (FileInputStream fis = new FileInputStream("src/main/resources/config.properties")) {
            props.load(fis);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private BigDecimal getIvaRate() {
        String raw = props.getProperty("IVA", "0.12").trim();
        return new BigDecimal(raw);
    }

    // ODC + 7 dígitos => 10 chars
    public String generateNextCode() {
        String sql = "SELECT oc_codigo FROM ordencompra ORDER BY oc_codigo DESC LIMIT 1";
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                String last = rs.getString("oc_codigo");
                if (last == null) return "ODC0000001";
                last = last.trim();
                String numStr = last.substring(3).replace(" ", "");
                int num = Integer.parseInt(numStr) + 1;
                return String.format("ODC%07d", num);
            }
            return "ODC0000001";
        } catch (Exception e) {
            e.printStackTrace();
            return "ODC0000001";
        }
    }

    public void recalcularTotales(OrdenCompra oc) {
        BigDecimal subtotal = BigDecimal.ZERO;

        for (DetalleOC d : oc.getDetalles()) {
            d.recalcular();
            subtotal = subtotal.add(d.getPxocSubtotal());
        }

        BigDecimal ivaRate = getIvaRate();
        BigDecimal iva = subtotal.multiply(ivaRate);
        BigDecimal total = subtotal.add(iva);

        oc.setOcEstado(oc.getOcEstado() == null ? "PEN" : oc.getOcEstado());

        // set doubles (manteniendo tu estilo actual)
        // redondeo simple a 2 decimales:
        ocSubtotalSetter(oc, subtotal);
        ocIvaSetter(oc, iva);
        ocTotalSetter(oc, total);
    }

    private void ocSubtotalSetter(OrdenCompra oc, BigDecimal v) {
        // helper para mantener 2 decimales
        ocSubtotalReflect(oc, v);
    }

    private void ocIvaSetter(OrdenCompra oc, BigDecimal v) { ocIvaReflect(oc, v); }
    private void ocTotalSetter(OrdenCompra oc, BigDecimal v) { ocTotalReflect(oc, v); }

    // setters indirectos (porque en tu OrdenCompra arriba dejé solo getter; aquí lo hago por SQL/actualización)
    private void ocSubtotalReflect(OrdenCompra oc, BigDecimal v) {
        try {
            var f = OrdenCompra.class.getDeclaredField("ocSubtotal");
            f.setAccessible(true);
            f.set(oc, v.setScale(2, java.math.RoundingMode.HALF_UP).doubleValue());
        } catch (Exception ignored) {}
    }
    private void ocIvaReflect(OrdenCompra oc, BigDecimal v) {
        try {
            var f = OrdenCompra.class.getDeclaredField("ocIva");
            f.setAccessible(true);
            f.set(oc, v.setScale(2, java.math.RoundingMode.HALF_UP).doubleValue());
        } catch (Exception ignored) {}
    }
    private void ocTotalReflect(OrdenCompra oc, BigDecimal v) {
        try {
            var f = OrdenCompra.class.getDeclaredField("ocTotal");
            f.setAccessible(true);
            f.set(oc, v.setScale(2, java.math.RoundingMode.HALF_UP).doubleValue());
        } catch (Exception ignored) {}
    }

    public boolean save(OrdenCompra oc) {
        try {
            conn.setAutoCommit(false);

            boolean isNew = (oc.getOcCodigo() == null || oc.getOcCodigo().isBlank());
            if (isNew) {
                oc.setOcCodigo(generateNextCode());
                insertCabecera(oc);
            } else {
                // solo si está PEN
                if (!"PEN".equals(oc.getOcEstado())) {
                    conn.rollback();
                    return false;
                }
                updateCabeceraPEN(oc);
                deleteDetalles(oc.getOcCodigo());
            }

            insertDetalles(oc);

            conn.commit();
            return true;

        } catch (Exception e) {
            try { conn.rollback(); } catch (Exception ignored) {}
            e.printStackTrace();
            return false;
        } finally {
            try { conn.setAutoCommit(true); } catch (Exception ignored) {}
        }
    }

    private void insertCabecera(OrdenCompra oc) throws SQLException {
        String sql = """
            INSERT INTO ordencompra
            (oc_codigo, prv_codigo, oc_fecha, oc_subtotal, oc_iva, oc_total,
             oc_fecha_aprobacion, oc_fecha_eliminacion, oc_estado)
            VALUES (?, ?, CURRENT_DATE, ?, ?, ?, NULL, NULL, 'PEN')
        """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, oc.getOcCodigo());
            ps.setString(2, oc.getPrvCodigo());
            ps.setBigDecimal(3, BigDecimal.valueOf(oc.getOcSubtotal()));
            ps.setBigDecimal(4, BigDecimal.valueOf(oc.getOcIva()));
            ps.setBigDecimal(5, BigDecimal.valueOf(oc.getOcTotal()));
            ps.executeUpdate();
        }
    }

    private void updateCabeceraPEN(OrdenCompra oc) throws SQLException {
        String sql = """
            UPDATE ordencompra
            SET prv_codigo = ?,
                oc_subtotal = ?,
                oc_iva = ?,
                oc_total = ?
            WHERE oc_codigo = ?
              AND oc_estado = 'PEN'
        """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, oc.getPrvCodigo());
            ps.setBigDecimal(2, BigDecimal.valueOf(oc.getOcSubtotal()));
            ps.setBigDecimal(3, BigDecimal.valueOf(oc.getOcIva()));
            ps.setBigDecimal(4, BigDecimal.valueOf(oc.getOcTotal()));
            ps.setString(5, oc.getOcCodigo());
            ps.executeUpdate();
        }
    }

    private void deleteDetalles(String ocCodigo) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("DELETE FROM detalle_oc WHERE oc_codigo = ?")) {
            ps.setString(1, ocCodigo);
            ps.executeUpdate();
        }
    }

    private void insertDetalles(OrdenCompra oc) throws SQLException {
        String sql = """
            INSERT INTO detalle_oc (oc_codigo, mp_codigo, pxoc_cantidad, pxoc_subtotal)
            VALUES (?, ?, ?, ?)
        """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (DetalleOC d : oc.getDetalles()) {
                d.recalcular();
                ps.setString(1, oc.getOcCodigo());
                ps.setString(2, d.getMpCodigo());
                ps.setBigDecimal(3, d.getPxocCantidad());
                ps.setBigDecimal(4, d.getPxocSubtotal());
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    public boolean cambiarEstado(OrdenCompra oc) {
        try {
            String sql = """
                UPDATE ordencompra
                SET oc_estado = ?,
                    oc_fecha_aprobacion = CASE WHEN ? = 'APR' THEN CURRENT_DATE ELSE oc_fecha_aprobacion END,
                    oc_fecha_eliminacion = CASE WHEN ? = 'ANU' THEN CURRENT_DATE ELSE oc_fecha_eliminacion END
                WHERE oc_codigo = ?
                  AND oc_estado = 'PEN'
            """;

            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, oc.getOcEstado());
                ps.setString(2, oc.getOcEstado());
                ps.setString(3, oc.getOcEstado());
                ps.setString(4, oc.getOcCodigo());
                return ps.executeUpdate() > 0;
            }

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // ======= LISTADO =======

    public List<OrdenCompra> getAll() {
        String sql = """
            SELECT
                TRIM(oc.oc_codigo) AS oc_codigo,
                TRIM(oc.prv_codigo) AS prv_codigo,
                oc.oc_fecha,
                oc.oc_subtotal,
                oc.oc_iva,
                oc.oc_total,
                oc.oc_fecha_aprobacion,
                oc.oc_fecha_eliminacion,
                TRIM(oc.oc_estado) AS oc_estado,
                TRIM(p.prv_razonsocial) AS prv_razonsocial
            FROM ordencompra oc
            JOIN proveedor p ON p.prv_codigo = oc.prv_codigo
            ORDER BY oc.oc_codigo DESC
        """;

        List<OrdenCompra> list = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(mapCabecera(rs));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<OrdenCompra> getByProveedor(String filtro) {
        String sql = """
            SELECT
                TRIM(oc.oc_codigo) AS oc_codigo,
                TRIM(oc.prv_codigo) AS prv_codigo,
                oc.oc_fecha,
                oc.oc_subtotal,
                oc.oc_iva,
                oc.oc_total,
                oc.oc_fecha_aprobacion,
                oc.oc_fecha_eliminacion,
                TRIM(oc.oc_estado) AS oc_estado,
                TRIM(p.prv_razonsocial) AS prv_razonsocial
            FROM ordencompra oc
            JOIN proveedor p ON p.prv_codigo = oc.prv_codigo
            WHERE LOWER(p.prv_razonsocial) LIKE LOWER(?)
            ORDER BY oc.oc_codigo DESC
        """;

        List<OrdenCompra> list = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, "%" + filtro.trim() + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapCabecera(rs));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    private OrdenCompra mapCabecera(ResultSet rs) throws SQLException {
        Date f = rs.getDate("oc_fecha");
        Date fa = rs.getDate("oc_fecha_aprobacion");
        Date fe = rs.getDate("oc_fecha_eliminacion");

        LocalDate ocFecha = (f == null) ? null : f.toLocalDate();
        LocalDate apr = (fa == null) ? null : fa.toLocalDate();
        LocalDate anu = (fe == null) ? null : fe.toLocalDate();

        return new OrdenCompra(
            rs.getString("oc_codigo"),
            rs.getString("prv_codigo"),
            ocFecha,
            rs.getBigDecimal("oc_subtotal") == null ? 0.0 : rs.getBigDecimal("oc_subtotal").doubleValue(),
            rs.getBigDecimal("oc_iva") == null ? 0.0 : rs.getBigDecimal("oc_iva").doubleValue(),
            rs.getBigDecimal("oc_total") == null ? 0.0 : rs.getBigDecimal("oc_total").doubleValue(),
            apr, anu,
            rs.getString("oc_estado"),
            rs.getString("prv_razonsocial"),
            conn
        );
    }

    // ======= CARGAR CABECERA + DETALLES PARA EDITAR/VER =======

    public OrdenCompra getByCodigoConDetalles(String codigo) {
        OrdenCompra oc = null;

        String cabSql = """
            SELECT
                TRIM(oc.oc_codigo) AS oc_codigo,
                TRIM(oc.prv_codigo) AS prv_codigo,
                oc.oc_fecha,
                oc.oc_subtotal,
                oc.oc_iva,
                oc.oc_total,
                oc.oc_fecha_aprobacion,
                oc.oc_fecha_eliminacion,
                TRIM(oc.oc_estado) AS oc_estado,
                TRIM(p.prv_razonsocial) AS prv_razonsocial
            FROM ordencompra oc
            JOIN proveedor p ON p.prv_codigo = oc.prv_codigo
            WHERE oc.oc_codigo = ?
        """;

        String detSql = """
            SELECT
                TRIM(d.oc_codigo) AS oc_codigo,
                TRIM(d.mp_codigo) AS mp_codigo,
                d.pxoc_cantidad,
                d.pxoc_subtotal,
                TRIM(mp.mp_descripcion) AS mp_descripcion,
                mp.mp_precio_compra
            FROM detalle_oc d
            JOIN materia_prima mp ON mp.mp_codigo = d.mp_codigo
            WHERE d.oc_codigo = ?
            ORDER BY mp.mp_descripcion
        """;

        try (PreparedStatement ps = conn.prepareStatement(cabSql)) {
            ps.setString(1, codigo);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) oc = mapCabecera(rs);
            }

            if (oc == null) return null;

            List<DetalleOC> dets = new ArrayList<>();
            try (PreparedStatement pdet = conn.prepareStatement(detSql)) {
                pdet.setString(1, codigo);
                try (ResultSet rd = pdet.executeQuery()) {
                    while (rd.next()) {
                        DetalleOC d = new DetalleOC();
                        d.setOcCodigo(rd.getString("oc_codigo"));
                        d.setMpCodigo(rd.getString("mp_codigo"));
                        d.setMpDescripcion(rd.getString("mp_descripcion"));
                        d.setMpPrecioCompra(rd.getBigDecimal("mp_precio_compra"));
                        d.setPxocCantidad(rd.getBigDecimal("pxoc_cantidad"));
                        dets.add(d);
                    }
                }
            }

            oc.setDetalles(dets);
            return oc;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
