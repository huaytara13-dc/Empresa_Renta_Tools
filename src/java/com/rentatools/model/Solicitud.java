package com.rentatools.model;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

/** Solicitud de alquiler (SOLPED): cabecera con su detalle de equipos. */
public class Solicitud {

    private int id;
    private int clienteId;
    private int usuarioId;
    private int diasSolicitados;
    private BigDecimal subtotal;
    private BigDecimal igv;
    private BigDecimal garantia;
    private BigDecimal total;
    private String estado;
    private String observaciones;
    private int aprobadoPor;                 // 0 si aun no se resuelve
    private OffsetDateTime fechaResolucion;
    private OffsetDateTime fechaCreacion;

    // Datos de otras tablas, para mostrar en pantalla
    private String clienteNombre;
    private String clienteDocumento;
    private String usuarioNombre;
    private String aprobadorNombre;
    private int cantidadEquipos;
    private int alquilerId;                  // 0 si aun no se convirtio en alquiler
    private List<DetalleSolicitud> detalles = new ArrayList<>();

    public Solicitud() { }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getClienteId() { return clienteId; }
    public void setClienteId(int clienteId) { this.clienteId = clienteId; }

    public int getUsuarioId() { return usuarioId; }
    public void setUsuarioId(int usuarioId) { this.usuarioId = usuarioId; }

    public int getDiasSolicitados() { return diasSolicitados; }
    public void setDiasSolicitados(int diasSolicitados) { this.diasSolicitados = diasSolicitados; }

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

    public int getAprobadoPor() { return aprobadoPor; }
    public void setAprobadoPor(int aprobadoPor) { this.aprobadoPor = aprobadoPor; }

    public OffsetDateTime getFechaResolucion() { return fechaResolucion; }
    public void setFechaResolucion(OffsetDateTime fechaResolucion) { this.fechaResolucion = fechaResolucion; }

    public OffsetDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(OffsetDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }

    public String getClienteNombre() { return clienteNombre; }
    public void setClienteNombre(String clienteNombre) { this.clienteNombre = clienteNombre; }

    public String getClienteDocumento() { return clienteDocumento; }
    public void setClienteDocumento(String clienteDocumento) { this.clienteDocumento = clienteDocumento; }

    public String getUsuarioNombre() { return usuarioNombre; }
    public void setUsuarioNombre(String usuarioNombre) { this.usuarioNombre = usuarioNombre; }

    public String getAprobadorNombre() { return aprobadorNombre; }
    public void setAprobadorNombre(String aprobadorNombre) { this.aprobadorNombre = aprobadorNombre; }

    public int getCantidadEquipos() { return cantidadEquipos; }
    public void setCantidadEquipos(int cantidadEquipos) { this.cantidadEquipos = cantidadEquipos; }

    public int getAlquilerId() { return alquilerId; }
    public void setAlquilerId(int alquilerId) { this.alquilerId = alquilerId; }

    public List<DetalleSolicitud> getDetalles() { return detalles; }
    public void setDetalles(List<DetalleSolicitud> detalles) { this.detalles = detalles; }
}
