package com.rentatools.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

/** Comprobante de pago (boleta o factura) de un alquiler. */
public class Comprobante {

    private int id;
    private int alquilerId;
    private String tipo;                 // BOLETA | FACTURA
    private String serie;
    private int numero;
    private LocalDate fechaEmision;
    private String clienteDocumento;
    private String clienteNombre;
    private String clienteDireccion;
    private BigDecimal subtotal;
    private BigDecimal igv;
    private BigDecimal total;
    private String estado;               // EMITIDO | ANULADO
    private OffsetDateTime fechaCreacion;

    public Comprobante() { }

    /** Por ejemplo B001-00000012. */
    public String getNumeroCompleto() {
        return serie + "-" + String.format("%08d", numero);
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getAlquilerId() { return alquilerId; }
    public void setAlquilerId(int alquilerId) { this.alquilerId = alquilerId; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public String getSerie() { return serie; }
    public void setSerie(String serie) { this.serie = serie; }

    public int getNumero() { return numero; }
    public void setNumero(int numero) { this.numero = numero; }

    public LocalDate getFechaEmision() { return fechaEmision; }
    public void setFechaEmision(LocalDate fechaEmision) { this.fechaEmision = fechaEmision; }

    public String getClienteDocumento() { return clienteDocumento; }
    public void setClienteDocumento(String clienteDocumento) { this.clienteDocumento = clienteDocumento; }

    public String getClienteNombre() { return clienteNombre; }
    public void setClienteNombre(String clienteNombre) { this.clienteNombre = clienteNombre; }

    public String getClienteDireccion() { return clienteDireccion; }
    public void setClienteDireccion(String clienteDireccion) { this.clienteDireccion = clienteDireccion; }

    public BigDecimal getSubtotal() { return subtotal; }
    public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }

    public BigDecimal getIgv() { return igv; }
    public void setIgv(BigDecimal igv) { this.igv = igv; }

    public BigDecimal getTotal() { return total; }
    public void setTotal(BigDecimal total) { this.total = total; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public OffsetDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(OffsetDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }
}
