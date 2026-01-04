package com.daf.controller;

import java.io.FileInputStream;
import java.sql.Connection;
import java.time.LocalDate;
import java.util.List;
import java.util.Properties;

import com.daf.model.ProductoModel;

public class Producto {
    private String prdCodigo;
    private String umVenta;
    private String catCodigo;          // puede ser null
    private String prdDescripcion;
    private Double prdPrecioVenta;
    private Integer prdStock;
    private String prdPrioridad;       // "F" o "L"
    private String prdImg;             // puede ser null
    private Integer prdPromocion;      // puede ser null
    private String prdEstado;          // "ACT" o "INA"
    private LocalDate prdFechaAlta;    // fecha de borrado lógico (cuando pasa a INA)

    private ProductoModel model;
    private Properties props = new Properties();

    public Producto(Connection conn) {
        this.model = new ProductoModel(conn);
        loadProperties();

        this.prdEstado = props.getProperty("ESTADO_INDEPENDIENTE_INICIAL");
        this.prdFechaAlta = null;
    }

    public Producto(String prdCodigo, String umVenta, String catCodigo, String prdDescripcion,
                    Double prdPrecioVenta, Integer prdStock, String prdPrioridad,
                    String prdImg, Integer prdPromocion, String prdEstado, LocalDate prdFechaAlta,
                    Connection conn) {
        this.prdCodigo = prdCodigo;
        this.umVenta = umVenta;
        loadProperties();
        this.catCodigo = catCodigo;
        this.prdDescripcion = prdDescripcion;
        this.prdPrecioVenta = prdPrecioVenta;
        this.prdStock = prdStock;
        this.prdPrioridad = prdPrioridad;
        this.prdImg = prdImg;
        this.prdPromocion = prdPromocion;
        this.prdEstado = prdEstado;
        this.prdFechaAlta = prdFechaAlta;
        this.model = new ProductoModel(conn);
    }

    private void loadProperties() {
        try (FileInputStream fis = new FileInputStream("src/main/resources/config.properties")) {
            props.load(fis);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Producto(String prdCodigo, String prdDescripcion, Connection conn) {
        this.prdCodigo = prdCodigo;
        this.prdDescripcion = prdDescripcion;
        this.model = new ProductoModel(conn);
    }

    // Getters / Setters
    public String getPrdCodigo() { return prdCodigo; }
    public void setPrdCodigo(String prdCodigo) { this.prdCodigo = prdCodigo; }

    public String getUmVenta() { return umVenta; }
    public void setUmVenta(String umVenta) { this.umVenta = umVenta; }

    public String getCatCodigo() { return catCodigo; }
    public void setCatCodigo(String catCodigo) { this.catCodigo = catCodigo; }

    public String getPrdDescripcion() { return prdDescripcion; }
    public void setPrdDescripcion(String prdDescripcion) { this.prdDescripcion = prdDescripcion; }

    public Double getPrdPrecioVenta() { return prdPrecioVenta; }
    public void setPrdPrecioVenta(Double prdPrecioVenta) { this.prdPrecioVenta = prdPrecioVenta; }

    public Integer getPrdStock() { return prdStock; }
    public void setPrdStock(Integer prdStock) { this.prdStock = prdStock; }

    public String getPrdPrioridad() { return prdPrioridad; }
    public void setPrdPrioridad(String prdPrioridad) { this.prdPrioridad = prdPrioridad; }

    public String getPrdImg() { return prdImg; }
    public void setPrdImg(String prdImg) { this.prdImg = prdImg; }

    public Integer getPrdPromocion() { return prdPromocion; }
    public void setPrdPromocion(Integer prdPromocion) { this.prdPromocion = prdPromocion; }

    public String getPrdEstado() { return prdEstado; }
    public void setPrdEstado(String prdEstado) { this.prdEstado = prdEstado; }

    public LocalDate getPrdFechaAlta() { return prdFechaAlta; }
    public void setPrdFechaAlta(LocalDate prdFechaAlta) { this.prdFechaAlta = prdFechaAlta; }

    // CRUD “DP”
    public boolean saveDP() {
        if (this.prdCodigo == null || this.prdCodigo.trim().isEmpty()) {
            this.prdCodigo = model.generateNextCode();
            return model.add(this);
        } else {
            return model.update(this.prdCodigo, this);
        }
    }

    public boolean deleteDP() {
        this.prdEstado = props.getProperty("ESTADO_INDEPENDIENTE_ELIMINADO");
        this.prdFechaAlta = LocalDate.now();
        return model.delete(this.prdCodigo);
    }

    public List<Producto> getAllDP() {
        return model.getAll();
    }

    public List<Producto> getByNameDP(String desc) {
        return model.getByName(desc);
    }

    // Validación
    public String validate() {
        if (umVenta == null || umVenta.trim().isEmpty()) {
            return "Debe seleccionar una unidad de medida (UM_VENTA)";
        }
        if (prdDescripcion == null || prdDescripcion.trim().isEmpty()) {
            return "La descripción es obligatoria";
        }
        if (prdDescripcion.length() > 60) {
            return "La descripción no puede exceder 60 caracteres";
        }
        if (prdPrecioVenta == null || prdPrecioVenta < 0) {
            return "El precio de venta debe ser un valor positivo";
        }
        if (prdStock == null || prdStock < 0) {
            return "El stock debe ser un valor positivo";
        }
        if (prdPrioridad != null && !prdPrioridad.trim().isEmpty()) {
            if (!prdPrioridad.equals("F") && !prdPrioridad.equals("L")) {
                return "La prioridad debe ser F (FIFO) o L (LIFO)";
            }
        }
        return null;
    }

    public List<Producto> getForComboBoxDP() {
        return model.getForComboBox();
    }
}
