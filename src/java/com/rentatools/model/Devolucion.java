package com.rentatools.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

public class Devolucion {

    private int id;
    private int alquilerId;
    private int usuarioId;
    private LocalDate fechaDevolucion;
    private int diasRetraso;
    private BigDecimal penalidadRetraso = BigDecimal.ZERO;
    private BigDecimal penalidadIncidencias = BigDecimal.ZERO;
    private BigDecimal penalidadTotal = BigDecimal.ZERO;
    private BigDecimal garantiaDevuelta = BigDecimal.ZERO;
    private BigDecimal montoAdicional = BigDecimal.ZERO;
    private String observaciones;
    private OffsetDateTime fechaCreacion;

    // Datos de otras tablas, para mostrar en pantalla
    private String clienteNombre;
    private String usuarioNombre;
    private int cantidadIncidencias;
    private List<DetalleDevolucion> detalles = new ArrayList<>();
    private List<Incidencia> incidencias = new ArrayList<>();

    public Devolucion() { }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getAlquilerId() { return alquilerId; }
    public void setAlquilerId(int alquilerId) { this.alquilerId = alquilerId; }

    public int getUsuarioId() { return usuarioId; }
    public void setUsuarioId(int usuarioId) { this.usuarioId = usuarioId; }

    public LocalDate getFechaDevolucion() { return fechaDevolucion; }
    public void setFechaDevolucion(LocalDate fechaDevolucion) { this.fechaDevolucion = fechaDevolucion; }

    public int getDiasRetraso() { return diasRetraso; }
    public void setDiasRetraso(int diasRetraso) { this.diasRetraso = diasRetraso; }

    public BigDecimal getPenalidadRetraso() { return penalidadRetraso; }
    public void setPenalidadRetraso(BigDecimal v) { this.penalidadRetraso = v; }

    public BigDecimal getPenalidadIncidencias() { return penalidadIncidencias; }
    public void setPenalidadIncidencias(BigDecimal v) { this.penalidadIncidencias = v; }

    public BigDecimal getPenalidadTotal() { return penalidadTotal; }
    public void setPenalidadTotal(BigDecimal v) { this.penalidadTotal = v; }

    public BigDecimal getGarantiaDevuelta() { return garantiaDevuelta; }
    public void setGarantiaDevuelta(BigDecimal v) { this.garantiaDevuelta = v; }

    public BigDecimal getMontoAdicional() { return montoAdicional; }
    public void setMontoAdicional(BigDecimal v) { this.montoAdicional = v; }

    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }

    public OffsetDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(OffsetDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }

    public String getClienteNombre() { return clienteNombre; }
    public void setClienteNombre(String clienteNombre) { this.clienteNombre = clienteNombre; }

    public String getUsuarioNombre() { return usuarioNombre; }
    public void setUsuarioNombre(String usuarioNombre) { this.usuarioNombre = usuarioNombre; }

    public int getCantidadIncidencias() { return cantidadIncidencias; }
    public void setCantidadIncidencias(int c) { this.cantidadIncidencias = c; }

    public List<DetalleDevolucion> getDetalles() { return detalles; }
    public void setDetalles(List<DetalleDevolucion> detalles) { this.detalles = detalles; }

    public List<Incidencia> getIncidencias() { return incidencias; }
    public void setIncidencias(List<Incidencia> incidencias) { this.incidencias = incidencias; }
}
