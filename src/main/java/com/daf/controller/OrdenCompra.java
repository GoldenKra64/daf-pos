package com.daf.controller;

import java.sql.Connection;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.daf.model.OrdenCompraModel;

public class OrdenCompra {

    private String ocCodigo;
    private String prvCodigo;

    private LocalDate ocFecha;
    private Double ocSubtotal;
    private Double ocIva;
    private Double ocTotal;

    private LocalDate ocFechaAprobacion;
    private LocalDate ocFechaEliminacion;

    private String ocEstado; // PEN / APR / ANU

    // para UI
    private String prvRazonSocial;

    private List<DetalleOC> detalles = new ArrayList<>();

    private final OrdenCompraModel model;

    public OrdenCompra(Connection conn) {
        this.model = new OrdenCompraModel(conn);
        this.ocEstado = "PEN";
    }

    public OrdenCompra(String ocCodigo, String prvCodigo, LocalDate ocFecha,
                       Double ocSubtotal, Double ocIva, Double ocTotal,
                       LocalDate ocFechaAprobacion, LocalDate ocFechaEliminacion,
                       String ocEstado, String prvRazonSocial,
                       Connection conn) {
        this.ocCodigo = trim(ocCodigo);
        this.prvCodigo = trim(prvCodigo);
        this.ocFecha = ocFecha;
        this.ocSubtotal = ocSubtotal;
        this.ocIva = ocIva;
        this.ocTotal = ocTotal;
        this.ocFechaAprobacion = ocFechaAprobacion;
        this.ocFechaEliminacion = ocFechaEliminacion;
        this.ocEstado = trim(ocEstado);
        this.prvRazonSocial = trim(prvRazonSocial);
        this.model = new OrdenCompraModel(conn);
    }

    public boolean esEditable() {
        return ocCodigo == null || ocCodigo.isBlank() || "PEN".equals(ocEstado);
    }

    public void addDetalle(DetalleOC d) { detalles.add(d); }
    public void removeDetalle(int idx) { detalles.remove(idx); }

    public void recalcularTotales() { model.recalcularTotales(this); }

    public boolean saveDP() {
        // si es nueva, fecha del sistema y PEN
        if (ocCodigo == null || ocCodigo.isBlank()) {
            ocFecha = LocalDate.now();
            ocEstado = "PEN";
        }
        // totales antes de guardar
        recalcularTotales();
        return model.save(this);
    }

    public boolean aprobarDP() {
        if (!"PEN".equals(ocEstado)) return false;
        ocEstado = "APR";
        ocFechaAprobacion = LocalDate.now();
        return model.cambiarEstado(this);
    }

    public boolean anularDP() {
        if (!"PEN".equals(ocEstado)) return false;
        ocEstado = "ANU";
        ocFechaEliminacion = LocalDate.now();
        return model.cambiarEstado(this);
    }

    public List<OrdenCompra> getAllDP() { return model.getAll(); }
    public List<OrdenCompra> getByProveedorDP(String filtro) { return model.getByProveedor(filtro); }

    public OrdenCompra getByCodigoDP(String codigo) { return model.getByCodigoConDetalles(codigo); }

    // Getters/Setters
    public String getOcCodigo() { return ocCodigo; }
    public void setOcCodigo(String ocCodigo) { this.ocCodigo = trim(ocCodigo); }

    public String getPrvCodigo() { return prvCodigo; }
    public void setPrvCodigo(String prvCodigo) { this.prvCodigo = trim(prvCodigo); }

    public LocalDate getOcFecha() { return ocFecha; }
    public Double getOcSubtotal() { return ocSubtotal; }
    public Double getOcIva() { return ocIva; }
    public Double getOcTotal() { return ocTotal; }

    public LocalDate getOcFechaAprobacion() { return ocFechaAprobacion; }
    public LocalDate getOcFechaEliminacion() { return ocFechaEliminacion; }

    public String getOcEstado() { return ocEstado; }
    public void setOcEstado(String ocEstado) { this.ocEstado = trim(ocEstado); }

    public String getPrvRazonSocial() { return prvRazonSocial; }

    public List<DetalleOC> getDetalles() { return detalles; }
    public void setDetalles(List<DetalleOC> detalles) { this.detalles = detalles; }

    public String validate() {
        if (prvCodigo == null || prvCodigo.isBlank()) return "Debe seleccionar un proveedor";
        if (detalles == null || detalles.isEmpty()) return "Debe agregar al menos un detalle";
        for (DetalleOC d : detalles) {
            if (d.getMpCodigo() == null || d.getMpCodigo().isBlank()) return "Detalle: debe seleccionar una materia prima";
            if (d.getPxocCantidad() == null || d.getPxocCantidad().doubleValue() <= 0) return "Detalle: la cantidad debe ser > 0";
        }
        return null;
    }

    private String trim(String v) { return v == null ? null : v.trim(); }
}
