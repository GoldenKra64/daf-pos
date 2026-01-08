package com.daf.controller;

import com.daf.model.CategoriaModel;
import java.sql.Connection;
import java.util.List;

public class Categoria {
    private String catCodigo;
    private String catDescripcion;
    private CategoriaModel model;

    public Categoria(Connection conn) {
        this.model = new CategoriaModel(conn);
    }

    public Categoria(String catCodigo, String catDescripcion, Connection conn) {
        this.catCodigo = catCodigo;
        this.catDescripcion = catDescripcion;
        this.model = new CategoriaModel(conn);
    }

    public String getCatCodigo() { return catCodigo; }
    public void setCatCodigo(String catCodigo) { this.catCodigo = catCodigo; }

    public String getCatDescripcion() { return catDescripcion; }
    public void setCatDescripcion(String catDescripcion) { this.catDescripcion = catDescripcion; }

    public List<Categoria> getAllDP() {
        return model.getAll();
    }

    @Override
    public String toString() {
        return catDescripcion;
    }
}
