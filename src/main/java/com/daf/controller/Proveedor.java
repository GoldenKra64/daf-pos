package com.daf.controller;

import java.sql.Connection;
import java.util.List;

import com.daf.model.ProveedorModel;

public class Proveedor {

    /* ===== CAMPOS BD ===== */
    private String prvCodigo;
    private String ctCodigo;
    private String prvRazonSocial;
    private String prvRuc;
    private String prvTelefono;   // puede ser null
    private String prvCelular;
    private String prvMail;
    private String prvDireccion;
    private String prvEstado;

    private final ProveedorModel model;

    /* ===== CONSTANTES ===== */
    private static final String ESTADO_ACTIVO = "ACT";
    private static final String ESTADO_INACTIVO = "INA";

    /* ===== CONSTRUCTOR NUEVO ===== */
    public Proveedor(Connection conn) {
        this.model = new ProveedorModel(conn);
        this.prvEstado = ESTADO_ACTIVO;
    }

    /* ===== CONSTRUCTOR DESDE BD (MISMA FIRMA QUE USA ProveedorModel) ===== */
    public Proveedor(
            String prvCodigo,
            String ctCodigo,
            String prvRazonSocial,
            String prvRuc,
            String prvTelefono,
            String prvCelular,
            String prvMail,
            String prvDireccion,
            String prvEstado,
            Connection conn
    ) {
        this.prvCodigo = trim(prvCodigo);
        this.ctCodigo = trim(ctCodigo);
        this.prvRazonSocial = trim(prvRazonSocial);
        this.prvRuc = trim(prvRuc);
        this.prvTelefono = trim(prvTelefono);
        this.prvCelular = trim(prvCelular);
        this.prvMail = trim(prvMail);
        this.prvDireccion = trim(prvDireccion);
        this.prvEstado = trim(prvEstado);
        this.model = new ProveedorModel(conn);
    }

    /* ===== VALIDACIÓN ===== */
    public String validate() {

        if (ctCodigo == null || ctCodigo.isBlank()) {
            return "Debe seleccionar una ciudad";
        }

        if (prvRazonSocial == null || prvRazonSocial.isBlank()) {
            return "La razón social es obligatoria";
        }

        if (prvRuc == null || !(prvRuc.length() == 10 || prvRuc.length() == 13) || !prvRuc.matches("\\d+")) {
            return "El RUC debe tener 10 o 13 dígitos numéricos";
        }

        // Teléfono: puede ser null o vacío, pero si viene debe ser numérico
        if (prvTelefono != null && !prvTelefono.isBlank() && !prvTelefono.matches("\\d+")) {
            return "Teléfono inválido";
        }

        // Celular: obligatorio y numérico (ajusta si en tu negocio puede ser null)
        if (prvCelular == null || prvCelular.isBlank() || !prvCelular.matches("\\d+")) {
            return "Celular inválido";
        }

        if (prvMail == null || prvMail.isBlank() || !prvMail.contains("@")) {
            return "Correo electrónico inválido";
        }

        if (prvDireccion == null || prvDireccion.isBlank()) {
            return "La dirección es obligatoria";
        }

        // Duplicado RUC (si tu model ya lo implementa)
        if (model.existsByRuc(prvRuc, prvCodigo)) {
            return "Proveedor ya se encuentra registrado";
        }

        return null;
    }

    /* ===== DATA PROVIDER (DP) ===== */

    public boolean saveDP() {
        if (prvCodigo == null || prvCodigo.isBlank()) {
            prvCodigo = model.generateNextCode();
            prvEstado = ESTADO_ACTIVO;
            return model.add(this);
        }
        return model.update(prvCodigo, this);
    }

   /** Baja lógica */
    public boolean deleteDP() {

        if (prvCodigo == null || prvCodigo.isBlank()) {
            return false;
        }

        prvEstado = ESTADO_INACTIVO;
        return model.update(prvCodigo, this);
    }


    /** Solo activos */
    public List<Proveedor> getAllDP() {
        return model.getAllActivos();
    }

    /** Búsqueda por filtro (razón social, ruc, mail, teléfono, etc) */
    public List<Proveedor> getByFiltroDP(String texto) {
        return model.getByFiltroActivos(texto);
    }

    /* ===== GETTERS ===== */
    public String getPrvCodigo() { return prvCodigo; }
    public String getCtCodigo() { return ctCodigo; }
    public String getPrvRazonSocial() { return prvRazonSocial; }
    public String getPrvRuc() { return prvRuc; }
    public String getPrvTelefono() { return prvTelefono; }
    public String getPrvCelular() { return prvCelular; }
    public String getPrvMail() { return prvMail; }
    public String getPrvDireccion() { return prvDireccion; }
    public String getPrvEstado() { return prvEstado; }

    /* ===== SETTERS ===== */
    public void setCtCodigo(String v) { this.ctCodigo = trim(v); }
    public void setPrvRazonSocial(String v) { this.prvRazonSocial = trim(v); }
    public void setPrvRuc(String v) { this.prvRuc = trim(v); }
    public void setPrvTelefono(String v) { this.prvTelefono = trim(v); }
    public void setPrvCelular(String v) { this.prvCelular = trim(v); }
    public void setPrvMail(String v) { this.prvMail = trim(v); }
    public void setPrvDireccion(String v) { this.prvDireccion = trim(v); }
    public void setPrvEstado(String v) { this.prvEstado = trim(v); }

    private String trim(String v) {
        return v == null ? null : v.trim();
    }
}
