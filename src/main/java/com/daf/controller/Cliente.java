package com.daf.controller;

import com.daf.model.ClienteModel;
import java.sql.Connection;
import java.util.List;

public class Cliente {
    private String cliCodigo, cliCedula, cliNombre, cliApellido, cliTelefono, cliDireccion, cliEmail, cliEstado;
    private Connection conn;
    private ClienteModel model;

    public Cliente(Connection conn) {
        this.conn = conn;
        this.model = new ClienteModel(conn);
    }

    public Cliente(String cod, String ced, String nom, String ape, String tlf, String dir, String em, String est, Connection conn) {
        this.cliCodigo = cod;
        this.cliCedula = ced;
        this.cliNombre = nom;
        this.cliApellido = ape;
        this.cliTelefono = tlf;
        this.cliDireccion = dir;
        this.cliEmail = em;
        this.cliEstado = est;
        this.conn = conn;
        this.model = new ClienteModel(conn);
    }

    // SETTERS (Necesarios para el ClienteForm)
    public void setCliCodigo(String cliCodigo) { this.cliCodigo = cliCodigo; }
    public void setCliCedula(String cliCedula) { this.cliCedula = cliCedula; }
    public void setCliNombre(String cliNombre) { this.cliNombre = cliNombre; }
    public void setCliApellido(String cliApellido) { this.cliApellido = cliApellido; }
    public void setCliTelefono(String cliTelefono) { this.cliTelefono = cliTelefono; }
    public void setCliDireccion(String cliDireccion) { this.cliDireccion = cliDireccion; }
    public void setCliEmail(String cliEmail) { this.cliEmail = cliEmail; }
    public void setCliEstado(String cliEstado) { this.cliEstado = cliEstado; }

    // GETTERS
    public String getCliCodigo() { return cliCodigo; }
    public String getCliCedula() { return cliCedula; }
    public String getCliNombre() { return cliNombre; }
    public String getCliApellido() { return cliApellido; }
    public String getCliTelefono() { return cliTelefono; }
    public String getCliDireccion() { return cliDireccion; }
    public String getCliEmail() { return cliEmail; }
    public String getCliEstado() { return cliEstado; }

    // MÉTODOS DE PERSISTENCIA
    public boolean saveDP() { return model.save(cliCodigo, cliCedula, cliNombre, cliApellido, cliTelefono, cliDireccion, cliEmail); }
    public boolean updateDP() { return model.update(cliCodigo, cliCedula, cliNombre, cliApellido, cliTelefono, cliDireccion, cliEmail); }
    public boolean deleteDP() { return model.delete(this.cliCodigo); }
    public String generateCodeDP() { return model.getNextCode(); }
    public List<Cliente> getAllDP() { return model.getAllList(); }
    public List<Cliente> getByNameDP(String busqueda) { return model.getBySearch(busqueda); }
}