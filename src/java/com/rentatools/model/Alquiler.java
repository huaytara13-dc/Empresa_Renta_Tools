package com.rentatools.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

public class Alquiler {

    private int id;
    private int clienteId;
    private int usuarioId;
    private int solicitudId;      // 0 si es un alquiler directo
    private LocalDate fechaInicio;
    private LocalDate fechaDevolucionPrevista;
    private LocalDate fechaDevolucionReal;
    private BigDecimal subtotal;
    private BigDecimal igv;
    private BigDecimal garantia;
    private BigDecimal total;
    private String estado;
    private String observaciones;
    private OffsetDateTime fechaCreacion;

    // Datos de otras tablas, para mostrar en pantalla
    private String clienteNombre;
    private String clienteDocumento;
    private String usuarioNombre;
    private int cantidadEquipos;
    private List<DetalleAlquiler> detalles = new ArrayList<>();

    public Alquiler() { }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getClienteId() { return clienteId; }
    public void setClienteId(int clienteId) { this.clienteId = clienteId; }

    public int getSolicitudId() { return solicitudId; }
    public void setSolicitudId(int solicitudId) { this.solicitudId = solicitudId; }

    public int getUsuarioId() { return usuarioId; }
    public void setUsuarioId(int usuarioId) { this.usuarioId = usuarioId; }

    public LocalDate getFechaInicio() { return fechaInicio; }
    public void setFechaInicio(LocalDate fechaInicio) { this.fechaInicio = fechaInicio; }

    public LocalDate getFechaDevolucionPrevista() { return fechaDevolucionPrevista; }
    public void setFechaDevolucionPrevista(LocalDate f) { this.fechaDevolucionPrevista = f; }

    public LocalDate getFechaDevolucionReal() { return fechaDevolucionReal; }
    public void setFechaDevolucionReal(LocalDate f) { this.fechaDevolucionReal = f; }

    public BigDecimal getSubtotal() { return subtotal; }
    public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }

    public BigDecimal getIgv() { return igv; }
    public void setIgv(BigDecimal igv) { this.igv = igv; }

    public BigDecimal getGarantia() { return garantia; }
    public void setGarantia(BigDecimal garantia) { this.garantia = garantia; }

    public BigDecimal getTotal() { return total; }
    public void setTotal(BigDecimal total) { this.total = total; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }

    public OffsetDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(OffsetDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }

    public String getClienteNombre() { return clienteNombre; }
    public void setClienteNombre(String clienteNombre) { this.clienteNombre = clienteNombre; }

    public String getClienteDocumento() { return clienteDocumento; }
    public void setClienteDocumento(String clienteDocumento) { this.clienteDocumento = clienteDocumento; }

    public String getUsuarioNombre() { return usuarioNombre; }
    public void setUsuarioNombre(String usuarioNombre) { this.usuarioNombre = usuarioNombre; }

    public int getCantidadEquipos() { return cantidadEquipos; }
    public void setCantidadEquipos(int cantidadEquipos) { this.cantidadEquipos = cantidadEquipos; }

    public List<DetalleAlquiler> getDetalles() { return detalles; }
    public void setDetalles(List<DetalleAlquiler> detalles) { this.detalles = detalles; }
}
