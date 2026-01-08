package com.daf.model;

import com.daf.controller.Cliente;
import com.daf.controller.Cliente.CiudadItem;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ClienteModel {
    private Connection conn;
    public ClienteModel(Connection conn) { this.conn = conn; }

    public List<CiudadItem> obtenerCiudades() {
        List<CiudadItem> lista = new ArrayList<>();
        String sql = "SELECT ct_codigo, ct_descripcion FROM public.ciudad ORDER BY ct_descripcion ASC";
        try (PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) lista.add(new CiudadItem(rs.getString(1).trim(), rs.getString(2).trim()));
        } catch (SQLException e) { e.printStackTrace(); }
        return lista;
    }

    public boolean save(String cod, String ct, String ced, String nom, String tlf, String cel, String dir, String em) {
        String sql = "INSERT INTO public.cliente (cli_codigo, ct_codigo, cli_ruc_ced, cli_nombre, cli_telefono, cli_celular, cli_direccion, cli_mail, cli_estado) VALUES (?,?,?,?,?,?,?,?,'ACT')";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, cod); ps.setString(2, ct); ps.setString(3, ced);
            ps.setString(4, nom.toUpperCase()); ps.setString(5, tlf);
            ps.setString(6, cel); ps.setString(7, dir); ps.setString(8, em);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public boolean update(String cod, String ct, String ced, String nom, String tlf, String cel, String dir, String em) {
        String sql = "UPDATE public.cliente SET ct_codigo=?, cli_ruc_ced=?, cli_nombre=?, cli_telefono=?, cli_celular=?, cli_direccion=?, cli_mail=? WHERE cli_codigo=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, ct); ps.setString(2, ced); ps.setString(3, nom.toUpperCase());
            ps.setString(4, tlf); ps.setString(5, cel); ps.setString(6, dir);
            ps.setString(7, em); ps.setString(8, cod);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public boolean delete(String codigo) {
        String sql = "UPDATE public.cliente SET cli_estado = 'INA' WHERE cli_codigo = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, codigo);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public List<Cliente> getAllList() {
        List<Cliente> lista = new ArrayList<>();
        String sql = "SELECT * FROM public.cliente WHERE cli_estado = 'ACT' ORDER BY cli_nombre ASC";
        try (Statement st = conn.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) lista.add(mapRow(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return lista;
    }

    public List<Cliente> getBySearch(String texto) {
        List<Cliente> lista = new ArrayList<>();
        String sql = "SELECT * FROM public.cliente WHERE cli_estado = 'ACT' AND (cli_nombre ILIKE ? OR cli_ruc_ced LIKE ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, "%" + texto + "%"); ps.setString(2, "%" + texto + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) lista.add(mapRow(rs));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return lista;
    }

    private Cliente mapRow(ResultSet rs) throws SQLException {
        return new Cliente(
            rs.getString("cli_codigo").trim(), rs.getString("ct_codigo").trim(),
            rs.getString("cli_ruc_ced").trim(), rs.getString("cli_nombre").trim(),
            rs.getString("cli_telefono"), rs.getString("cli_celular"),
            rs.getString("cli_direccion"), rs.getString("cli_mail"),
            rs.getString("cli_estado").trim(),
            rs.getString("cli_fecha_alta"), // Campo de fecha añadido
            conn
        );
    }

    public String getNextCode() {
        try (Statement st = conn.createStatement(); ResultSet rs = st.executeQuery("SELECT MAX(cli_codigo) FROM public.cliente")) {
            if (rs.next() && rs.getString(1) != null) {
                int num = Integer.parseInt(rs.getString(1).trim().substring(3)) + 1;
                return String.format("CLI%07d", num);
            }
        } catch (Exception e) {}
        return "CLI0000001";
    }
}