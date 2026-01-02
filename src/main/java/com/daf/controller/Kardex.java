package com.daf.controller;

import com.daf.model.KardexModel;
import java.sql.Connection;
import java.util.Date;
import java.util.List;

public class Kardex {
    private String krdCodigo;
    private String facCodigo;
    private String ocCodigo;
    private Integer krdCantidad;
    private Integer krdQtyTotal;
    private Date krdFechahora;
    private String krdAccion;
    private String usrId;
    private KardexModel model;

    public Kardex(Connection conn) {
        this.model = new KardexModel(conn);
    }

    public Kardex(String krdCodigo, String facCodigo, String ocCodigo,
                  Integer krdCantidad, Integer krdQtyTotal, Date krdFechahora,
                  String krdAccion, String usrId, Connection conn) {
        this.krdCodigo = krdCodigo;
        this.facCodigo = facCodigo;
        this.ocCodigo = ocCodigo;
        this.krdCantidad = krdCantidad;
        this.krdQtyTotal = krdQtyTotal;
        this.krdFechahora = krdFechahora;
        this.krdAccion = krdAccion;
        this.usrId = usrId;
        this.model = new KardexModel(conn);
    }

    // Getters y Setters
    public String getKrdCodigo() {
        return krdCodigo;
    }

    public void setKrdCodigo(String krdCodigo) {
        this.krdCodigo = krdCodigo;
    }

    public String getFacCodigo() {
        return facCodigo;
    }

    public void setFacCodigo(String facCodigo) {
        this.facCodigo = facCodigo;
    }

    public String getOcCodigo() {
        return ocCodigo;
    }

    public void setOcCodigo(String ocCodigo) {
        this.ocCodigo = ocCodigo;
    }

    public Integer getKrdCantidad() {
        return krdCantidad;
    }

    public void setKrdCantidad(Integer krdCantidad) {
        this.krdCantidad = krdCantidad;
    }

    public Integer getKrdQtyTotal() {
        return krdQtyTotal;
    }

    public void setKrdQtyTotal(Integer krdQtyTotal) {
        this.krdQtyTotal = krdQtyTotal;
    }

    public Date getKrdFechahora() {
        return krdFechahora;
    }

    public void setKrdFechahora(Date krdFechahora) {
        this.krdFechahora = krdFechahora;
    }

    public String getKrdAccion() {
        return krdAccion;
    }

    public void setKrdAccion(String krdAccion) {
        this.krdAccion = krdAccion;
    }

    public String getUsrId() {
        return usrId;
    }

    public void setUsrId(String usrId) {
        this.usrId = usrId;
    }

    // Método para actualizar el registro
    public boolean updateDP() {
        return model.update(this.krdCodigo, this);
    }

    // Método para obtener todos los registros
    public List<Kardex> getAllDP() {
        return model.getAll();
    }

    // Método para buscar por acción o código
    public List<Kardex> getByNameDP(String busqueda) {
        return model.getByName(busqueda);
    }

    // Método de validación
    public String validate() {
        if (krdQtyTotal == null || krdQtyTotal < 0) {
            return "La cantidad total debe ser un valor positivo";
        }
        if (krdQtyTotal < krdCantidad) {
            return "La cantidad total no puede ser menor que la cantidad del movimiento";
        }
        return null;
    }
    public String getOrigen() {
        if (facCodigo != null && !facCodigo.trim().isEmpty()) {
            return "Factura: " + facCodigo;
        } else if (ocCodigo != null && !ocCodigo.trim().isEmpty()) {
            return "Orden Compra: " + ocCodigo;
        } else {
            return "Sin origen";
        }
    }
}