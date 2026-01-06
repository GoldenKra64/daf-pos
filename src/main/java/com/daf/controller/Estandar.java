package com.daf.controller;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import com.daf.model.EstandarModel;
import com.daf.model.MateriaPrimaModel;
import com.daf.model.ProductoModel;

public class Estandar {
    private String estCod;
    private String prdCodigo;
    private Long estQtyTotal;
    private Double estPrecioTotal;
    private String estEstado;
    private List<DetalleEstandar> detalles;
    
    private EstandarModel model;
    private ProductoModel productoModel;
    private MateriaPrimaModel materiaPrimaModel;
    private Connection conn;

    private Properties props;

    public Estandar(Connection conn) {
        loadProperties();

        this.conn = conn;
        this.model = new EstandarModel(conn);
        this.productoModel = new ProductoModel(conn);
        this.materiaPrimaModel = new MateriaPrimaModel(conn);
        this.detalles = new ArrayList<>();
        this.estEstado = props.getProperty("ESTADO_DEPENDIENTE_PENDIENTE");
    }

    public Estandar(String estCod, String prdCodigo, Long estQtyTotal,
                    Double estPrecioTotal, String estEstado, Connection conn) {
        this.estCod = estCod;
        this.prdCodigo = prdCodigo;
        this.estQtyTotal = estQtyTotal;
        this.estPrecioTotal = estPrecioTotal;
        this.estEstado = estEstado;
        loadProperties();
        this.conn = conn;
        this.model = new EstandarModel(conn);
        this.productoModel = new ProductoModel(conn);
        this.materiaPrimaModel = new MateriaPrimaModel(conn);
        this.detalles = new ArrayList<>();
    }

    private void loadProperties() {
        props = new Properties();
        try (FileInputStream fis = new FileInputStream("src/main/resources/config.properties")) {
            props.load(fis);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Getters y Setters
    public String getEstCod() {
        return estCod;
    }

    public void setEstCod(String estCod) {
        this.estCod = estCod;
    }

    public String getPrdCodigo() {
        return prdCodigo;
    }

    public void setPrdCodigo(String prdCodigo) {
        this.prdCodigo = prdCodigo;
    }

    public Long getEstQtyTotal() {
        return estQtyTotal;
    }

    public void setEstQtyTotal(Long estQtyTotal) {
        this.estQtyTotal = estQtyTotal;
    }

    public Double getEstPrecioTotal() {
        return estPrecioTotal;
    }

    public void setEstPrecioTotal(Double estPrecioTotal) {
        this.estPrecioTotal = estPrecioTotal;
    }

    public String getEstEstado() {
        return estEstado;
    }

    public void setEstEstado(String estEstado) {
        this.estEstado = estEstado;
    }

    public List<DetalleEstandar> getDetalles() {
        return detalles;
    }

    public void setDetalles(List<DetalleEstandar> detalles) {
        this.detalles = detalles;
    }

    public void addDetalle(DetalleEstandar detalle) {
        detalle.setEstCod(this.estCod);
        this.detalles.add(detalle);
        recalcularTotales();
    }

    public void removeDetalle(int index) {
        if (index >= 0 && index < detalles.size()) {
            detalles.remove(index);
            recalcularTotales();
        }
    }

    // Recalcular totales
    public void recalcularTotales() {
        long totalCantidad = 0;
        double totalPrecio = 0.0;

        for (DetalleEstandar detalle : detalles) {
            if (detalle.getMxpCantidad() != null) {
                totalCantidad += detalle.getMxpCantidad();
            }
            totalPrecio += detalle.getCostoDetalle();
        }

        this.estQtyTotal = totalCantidad;
        this.estPrecioTotal = totalPrecio;
    }

    // Guardar estándar con sus detalles
    public boolean saveDP() {
        String error = validate();
        if (error != null) {
            System.err.println("Error de validación: " + error);
            return false;
        }

        recalcularTotales();

        // Si no tiene código, es nuevo
        if (this.estCod == null || this.estCod.trim().isEmpty()) {
            this.estCod = model.generateNextCode();
            return model.add(this);
        } else {
            return model.update(this);
        }
    }

    // Eliminar detalles
    public boolean deleteDP() {
        return model.delete(this.estCod);
    }

    // Obtener todos los estándares
    public List<Estandar> getAllDP() {
        return model.getAll();
    }

    // Buscar por nombre de producto
    public List<Estandar> getByNameDP(String nombreProducto) {
        return model.getByName(nombreProducto);
    }

    // Cargar detalles de un estándar
    public void cargarDetallesDP() {
        if (this.estCod != null && !this.estCod.trim().isEmpty()) {
            this.detalles = model.getDetalles(this.estCod);
            
            // Cargar información adicional de materias primas
            for (DetalleEstandar detalle : detalles) {
                MateriaPrima mp = obtenerMateriaPrimaPorCodigo(detalle.getMpCodigo());
                if (mp != null) {
                    detalle.setMpPrecioCompra(mp.getMpPrecioCompra());
                    detalle.setMpDescripcion(mp.getMpDescripcion());
                }
            }
        }
    }

    // Obtener nombre del producto
    public String getNombreProducto() {
        if (prdCodigo != null) {
            return productoModel.getNombreByCodigo(prdCodigo);
        }
        return null;
    }

    // Obtener materia prima por código
    private MateriaPrima obtenerMateriaPrimaPorCodigo(String mpCodigo) {
        List<MateriaPrima> todas = materiaPrimaModel.getAll();
        for (MateriaPrima mp : todas) {
            if (mp.getMpCodigo().equals(mpCodigo)) {
                return mp;
            }
        }
        return null;
    }

    // Validación completa
    public String validate() {
        if (prdCodigo == null || prdCodigo.trim().isEmpty()) {
            return "Debe seleccionar un producto";
        }

        if (detalles == null || detalles.isEmpty()) {
            return "Debe agregar al menos un detalle";
        }

        // Validar cada detalle
        for (int i = 0; i < detalles.size(); i++) {
            String errorDetalle = detalles.get(i).validate();
            if (errorDetalle != null) {
                return "Error en detalle " + (i + 1) + ": " + errorDetalle;
            }
        }

        return null;
    }

    public String getEstadoDescripcion() {
        switch (estEstado) {
            case "PEN" : return props.getProperty("ESTADO_DEPENDIENTE_PENDIENTE");
            case "APR" : return props.getProperty("ESTADO_DEPENDIENTE_APROBADO");
            case "ANU" : return props.getProperty("ESTADO_DEPENDIENTE_ANULADO");
            default: return estEstado;
        }
    }

    public boolean esEditable() {
        return props.getProperty("ESTADO_DEPENDIENTE_PENDIENTE").toString().equals(estEstado);
    }
}