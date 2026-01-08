package com.daf.controller;

import com.daf.model.ClienteModel;
import java.sql.Connection;
import java.util.List;

public class Cliente {
    private String cliCodigo, ctCodigo, cliCedula, cliNombre, cliApellido, cliTelefono, cliCelular, cliDireccion, cliEmail, cliEstado, cliFechaAlta;
    private Connection conn;
    private ClienteModel model;

    public Cliente(Connection conn) {
        this.conn = conn;
        this.model = new ClienteModel(conn);
    }

    public Cliente(String cod, String city, String ced, String nom, String ape, String tlf, String cel, String dir, String em, String est, String fecha, Connection conn) {
        this.cliCodigo = cod; this.ctCodigo = city; this.cliCedula = ced;
        this.cliNombre = nom; this.cliApellido = ape; this.cliTelefono = tlf; 
        this.cliCelular = cel; this.cliDireccion = dir; this.cliEmail = em; 
        this.cliEstado = est; this.cliFechaAlta = fecha;
        this.conn = conn; this.model = new ClienteModel(conn);
    }

    public String getCliCodigo() { return cliCodigo; }
    public void setCliCodigo(String c) { this.cliCodigo = c; }
    public String getCtCodigo() { return ctCodigo; }
    public void setCtCodigo(String c) { this.ctCodigo = c; }
    public String getCliCedula() { return cliCedula; }
    public void setCliCedula(String c) { this.cliCedula = c; }
    public String getCliNombre() { return cliNombre; }
    public void setCliNombre(String c) { this.cliNombre = c; }
    public String getCliApellido() { return cliApellido; }
    public void setCliApellido(String a) { this.cliApellido = a; }
    public String getCliTelefono() { return cliTelefono; }
    public void setCliTelefono(String c) { this.cliTelefono = c; }
    public String getCliCelular() { return cliCelular; }
    public void setCliCelular(String c) { this.cliCelular = c; }
    public String getCliDireccion() { return cliDireccion; }
    public void setCliDireccion(String c) { this.cliDireccion = c; }
    public String getCliEmail() { return cliEmail; }
    public void setCliEmail(String c) { this.cliEmail = c; }
    public String getCliEstado() { return cliEstado; }
    public void setCliEstado(String c) { this.cliEstado = c; }
    public String getCliFechaAlta() { return cliFechaAlta; }
    public void setCliFechaAlta(String f) { this.cliFechaAlta = f; }

    public static class CiudadItem {
        private String codigo, nombre;
        public CiudadItem(String c, String n) { this.codigo = c; this.nombre = n; }
        public String getCodigo() { return codigo; }
        @Override public String toString() { return nombre; }
    }

    public boolean saveDP() { return model.save(cliCodigo, ctCodigo, cliCedula, cliNombre, cliApellido, cliTelefono, cliCelular, cliDireccion, cliEmail); }
    public boolean updateDP() { return model.update(cliCodigo, ctCodigo, cliCedula, cliNombre, cliApellido, cliTelefono, cliCelular, cliDireccion, cliEmail); }
    public boolean deleteDP() { return model.delete(this.cliCodigo); }
    public String generateCodeDP() { return model.getNextCode(); }
    public List<Cliente> getAllDP() { return model.getAllList(); }
    public List<Cliente> getByNameDP(String b) { return model.getBySearch(b); }
    public List<CiudadItem> getListaCiudades() { return model.obtenerCiudades(); }
}