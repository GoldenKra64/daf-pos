package com.daf.model;

import com.daf.controller.Cliente;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ClienteModel {
    private Connection conn;

    public ClienteModel(Connection conn) {
        this.conn = conn;
    }

    // RF6.1: Crear un registro de cliente
    public boolean save(String codigo, String cedula, String nombre, String apellido, String tlf, String dir, String email) {
        String sql = "INSERT INTO public.cliente (cli_codigo, cli_cedula, cli_nombre, cli_apellido, cli_telefono, cli_direccion, cli_email, cli_estado) VALUES (?, ?, ?, ?, ?, ?, ?, 'ACT')";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, codigo);
            ps.setString(2, cedula);
            ps.setString(3, nombre);
            ps.setString(4, apellido);
            ps.setString(5, tlf);
            ps.setString(6, dir);
            ps.setString(7, email);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // RF6.2: Modificar un registro de cliente
    public boolean update(String codigo, String cedula, String nombre, String apellido, String tlf, String dir, String email) {
        String sql = "UPDATE public.cliente SET cli_cedula=?, cli_nombre=?, cli_apellido=?, cli_telefono=?, cli_direccion=?, cli_email=? WHERE cli_codigo=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, cedula);
            ps.setString(2, nombre);
            ps.setString(3, apellido);
            ps.setString(4, tlf);
            ps.setString(5, dir);
            ps.setString(6, email);
            ps.setString(7, codigo);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // RF6.4: Eliminar un registro de cliente (Borrado Lógico)
    public boolean delete(String codigo) {
        String sql = "UPDATE public.cliente SET cli_estado = 'INA' WHERE cli_codigo = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, codigo);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // RF6.3.2: Consulta General
    public List<Cliente> getAllList() {
        List<Cliente> lista = new ArrayList<>();
        String sql = "SELECT * FROM public.cliente WHERE cli_estado = 'ACT' ORDER BY cli_codigo ASC";
        try (Statement st = conn.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                lista.add(new Cliente(
                    rs.getString("cli_codigo"), 
                    rs.getString("cli_cedula"), 
                    rs.getString("cli_nombre"),
                    rs.getString("cli_apellido"), 
                    rs.getString("cli_telefono"), 
                    rs.getString("cli_direccion"),
                    rs.getString("cli_email"), 
                    rs.getString("cli_estado"), 
                    conn
                ));
            }
        } catch (SQLException e) { 
            e.printStackTrace(); 
        }
        return lista;
    }

    // RF6.3.1: Consulta Parametrizada (Buscador)
    public List<Cliente> getBySearch(String texto) {
        List<Cliente> lista = new ArrayList<>();
        // ILIKE es para que no importe mayúsculas/minúsculas en PostgreSQL
        String sql = "SELECT * FROM public.cliente WHERE cli_estado = 'ACT' AND (cli_nombre ILIKE ? OR cli_cedula LIKE ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, "%" + texto + "%");
            ps.setString(2, "%" + texto + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(new Cliente(
                        rs.getString("cli_codigo"), 
                        rs.getString("cli_cedula"), 
                        rs.getString("cli_nombre"),
                        rs.getString("cli_apellido"), 
                        rs.getString("cli_telefono"), 
                        rs.getString("cli_direccion"),
                        rs.getString("cli_email"), 
                        rs.getString("cli_estado"), 
                        conn
                    ));
                }
            }
        } catch (SQLException e) { 
            e.printStackTrace(); 
        }
        return lista;
    }

    // Generador de códigos automáticos
    public String getNextCode() {
        String sql = "SELECT cli_codigo FROM public.cliente ORDER BY cli_codigo DESC LIMIT 1";
        try (Statement st = conn.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) {
                String lastCode = rs.getString(1);
                // Extrae el número después de "CLI" y le suma 1
                int num = Integer.parseInt(lastCode.substring(3)) + 1;
                return String.format("CLI%07d", num);
            }
        } catch (Exception e) { 
            e.printStackTrace(); 
        }
        return "CLI0000001"; // Si la tabla está vacía
    }
}