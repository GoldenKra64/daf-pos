package com.daf.model;

public class Ciudad {

    private String ctCodigo;
    private String ctNombre;

    public Ciudad(String ctCodigo, String ctNombre) {
        this.ctCodigo = ctCodigo;
        this.ctNombre = ctNombre;
    }

    public String getCtCodigo() {
        return ctCodigo;
    }

    public String getCtNombre() {
        return ctNombre;
    }

    @Override
    public String toString() {
        return ctNombre; // lo que se muestra en el combo
    }
}
