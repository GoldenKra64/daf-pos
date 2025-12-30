package com.daf.controller;

import java.sql.Connection;
import java.util.List;

import com.daf.model.MateriaPrimaModel;

public class MateriaPrima {
    private String mpCodigo;
    private String umCompra;
    private String mpDescripcion;
    private Double mpPrecioCompra;
    private Integer mpCantidad;
    private String mpPrioridad;
    private String mpEstado;
    private MateriaPrimaModel model;

    public MateriaPrima(Connection conn) {
        this.model = new MateriaPrimaModel(conn);
        this.mpEstado = "ACT";
    }

    public MateriaPrima(String mpCodigo, String umCompra, String mpDescripcion,
                        Double mpPrecioCompra, Integer mpCantidad, String mpPrioridad,
                        String mpEstado, Connection conn) {
        this.mpCodigo = mpCodigo;
        this.umCompra = umCompra;
        this.mpDescripcion = mpDescripcion;
        this.mpPrecioCompra = mpPrecioCompra;
        this.mpCantidad = mpCantidad;
        this.mpPrioridad = mpPrioridad;
        this.mpEstado = mpEstado;
        this.model = new MateriaPrimaModel(conn);
    }

    // Getters y Setters
    public String getMpCodigo() {
        return mpCodigo;
    }

    public void setMpCodigo(String mpCodigo) {
        this.mpCodigo = mpCodigo;
    }

    public String getUmCompra() {
        return umCompra;
    }

    public void setUmCompra(String umCompra) {
        this.umCompra = umCompra;
    }

    public String getMpDescripcion() {
        return mpDescripcion;
    }

    public void setMpDescripcion(String mpDescripcion) {
        this.mpDescripcion = mpDescripcion;
    }

    public Double getMpPrecioCompra() {
        return mpPrecioCompra;
    }

    public void setMpPrecioCompra(Double mpPrecioCompra) {
        this.mpPrecioCompra = mpPrecioCompra;
    }

    public Integer getMpCantidad() {
        return mpCantidad;
    }

    public void setMpCantidad(Integer mpCantidad) {
        this.mpCantidad = mpCantidad;
    }

    public String getMpPrioridad() {
        return mpPrioridad;
    }

    public void setMpPrioridad(String mpPrioridad) {
        this.mpPrioridad = mpPrioridad;
    }

    public String getMpEstado() {
        return mpEstado;
    }

    public void setMpEstado(String mpEstado) {
        this.mpEstado = mpEstado;
    }

    public boolean saveDP() {
        if (this.mpCodigo == null || this.mpCodigo.isEmpty()) {
            this.mpCodigo = model.generateNextCode();
            return model.add(this);
        } else {
            return model.update(this.mpCodigo, this);
        }
    }

    public boolean deleteDP() {
        this.mpEstado = "INA";
        return model.delete(this.mpCodigo);
    }

    public List<MateriaPrima> getAllDP() {
        return model.getAll();
    }

    public List<MateriaPrima> getByNameDP(String mpDesc) {
        return model.getByName(mpDesc);
    }

    public String validate() {
        if (umCompra == null || umCompra.trim().isEmpty()) {
            return "Debe seleccionar una unidad de medida";
        }
        if (mpDescripcion == null || mpDescripcion.trim().isEmpty()) {
            return "La descripción es obligatoria";
        }
        if (mpDescripcion.length() > 60) {
            return "La descripción no puede exceder 60 caracteres";
        }
        if (mpPrecioCompra == null || mpPrecioCompra < 0) {
            return "El precio de compra debe ser un valor positivo";
        }
        if (mpCantidad == null || mpCantidad < 0) {
            return "La cantidad debe ser un valor positivo";
        }
        if (mpPrioridad == null || (!mpPrioridad.equals("F") && !mpPrioridad.equals("L"))) {
            return "La prioridad debe ser F (FIFO) o L (LIFO)";
        }
        return null;
    }
}