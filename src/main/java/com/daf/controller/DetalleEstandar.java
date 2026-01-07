package com.daf.controller;

public class DetalleEstandar {
    private String mpCodigo;
    private String estCod;
    private Integer mxpCantidad;
    private Double mpPrecioCompra;
    private String mpDescripcion;

    public DetalleEstandar() {
    }

    public DetalleEstandar(String mpCodigo, String estCod, Integer mxpCantidad) {
        this.mpCodigo = mpCodigo;
        this.estCod = estCod;
        this.mxpCantidad = mxpCantidad;
    }

    // Getters y Setters
    public String getMpCodigo() {
        return mpCodigo;
    }

    public void setMpCodigo(String mpCodigo) {
        this.mpCodigo = mpCodigo;
    }

    public String getEstCod() {
        return estCod;
    }

    public void setEstCod(String estCod) {
        this.estCod = estCod;
    }

    public Integer getMxpCantidad() {
        return mxpCantidad;
    }

    public void setMxpCantidad(Integer mxpCantidad) {
        this.mxpCantidad = mxpCantidad;
    }

    public Double getMpPrecioCompra() {
        return mpPrecioCompra;
    }

    public void setMpPrecioCompra(Double mpPrecioCompra) {
        this.mpPrecioCompra = mpPrecioCompra;
    }

    public String getMpDescripcion() {
        return mpDescripcion;
    }

    public void setMpDescripcion(String mpDescripcion) {
        this.mpDescripcion = mpDescripcion;
    }

    // Calcular costo de este detalle
    public Double getCostoDetalle() {
        if (mxpCantidad != null && mpPrecioCompra != null) {
            return mxpCantidad * mpPrecioCompra;
        }
        return 0.0;
    }

    // Validación
    public String validate() {
        if (mpCodigo == null || mpCodigo.trim().isEmpty()) {
            return "Debe seleccionar una materia prima";
        }
        if (mxpCantidad == null || mxpCantidad <= 0) {
            return "La cantidad debe ser mayor a 0";
        }
        return null;
    }
}