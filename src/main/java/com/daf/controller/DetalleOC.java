package com.daf.controller;

import java.math.BigDecimal;

public class DetalleOC {
    private String ocCodigo;
    private String mpCodigo;

    private String mpDescripcion;                 // para UI
    private BigDecimal mpPrecioCompra = BigDecimal.ZERO; // para cálculo

    private BigDecimal pxocCantidad = BigDecimal.ZERO;
    private BigDecimal pxocSubtotal = BigDecimal.ZERO;

    public String getOcCodigo() { return ocCodigo; }
    public void setOcCodigo(String ocCodigo) { this.ocCodigo = trim(ocCodigo); }

    public String getMpCodigo() { return mpCodigo; }
    public void setMpCodigo(String mpCodigo) { this.mpCodigo = trim(mpCodigo); }

    public String getMpDescripcion() { return mpDescripcion; }
    public void setMpDescripcion(String mpDescripcion) { this.mpDescripcion = trim(mpDescripcion); }

    public BigDecimal getMpPrecioCompra() { return mpPrecioCompra; }
    public void setMpPrecioCompra(BigDecimal mpPrecioCompra) {
        this.mpPrecioCompra = (mpPrecioCompra == null) ? BigDecimal.ZERO : mpPrecioCompra;
    }

    public BigDecimal getPxocCantidad() { return pxocCantidad; }
    public void setPxocCantidad(BigDecimal pxocCantidad) {
        this.pxocCantidad = (pxocCantidad == null) ? BigDecimal.ZERO : pxocCantidad;
        recalcular();
    }

    public BigDecimal getPxocSubtotal() { return pxocSubtotal; }

    public void recalcular() {
        if (pxocCantidad == null) pxocCantidad = BigDecimal.ZERO;
        if (mpPrecioCompra == null) mpPrecioCompra = BigDecimal.ZERO;
        pxocSubtotal = mpPrecioCompra.multiply(pxocCantidad);
    }

    private String trim(String v) { return v == null ? null : v.trim(); }
}
