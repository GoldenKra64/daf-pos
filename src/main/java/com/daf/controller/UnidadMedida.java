package com.daf.controller;

import com.daf.model.UnidadMedidaModel;
import java.sql.Connection;
import java.util.List;

public class UnidadMedida {
    private String umCodigo;
    private String umDescripcion;
    private UnidadMedidaModel model;

    public UnidadMedida(Connection conn) {
        this.model = new UnidadMedidaModel(conn);
    }

    public UnidadMedida(String umCodigo, String umDescripcion, Connection conn) {
        this.umCodigo = umCodigo;
        this.umDescripcion = umDescripcion;
        this.model = new UnidadMedidaModel(conn);
    }

    // Getters y Setters
    public String getUmCodigo() {
        return umCodigo;
    }

    public void setUmCodigo(String umCodigo) {
        this.umCodigo = umCodigo;
    }

    public String getUmDescripcion() {
        return umDescripcion;
    }

    public void setUmDescripcion(String umDescripcion) {
        this.umDescripcion = umDescripcion;
    }

    // Método para obtener todas las unidades de medida
    public List<UnidadMedida> getAllDP() {
        return model.getAll();
    }

    @Override
    public String toString() {
        return umDescripcion;
    }
}