package com.rentatools.model;

import java.time.LocalDate;
import java.time.OffsetDateTime;

/** Guia de entrega de los equipos de un alquiler, con la conformidad del cliente. */
public class GuiaEntrega {

    private int id;
    private int alquilerId;
    private String serie;
    private int numero;
    private LocalDate fechaEmision;
    private String direccionEntrega;
    private String estado;               // EMITIDA | ENTREGADA | ANULADA
    private String recibidoPor;
    private String documentoReceptor;
    private OffsetDateTime fechaEntrega;
    private OffsetDateTime fechaCreacion;

    // Datos de otras tablas, para mostrar en pantalla
    private String clienteNombre;
    private String clienteDocumento;

    public GuiaEntrega() { }

    /** Por ejemplo T001-00000007. */
    public String getNumeroCompleto() {
        return serie + "-" + String.format("%08d", numero);
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getAlquilerId() { return alquilerId; }
    public void setAlquilerId(int alquilerId) { this.alquilerId = alquilerId; }

    public String getSerie() { return serie; }
    public void setSerie(String serie) { this.serie = serie; }

    public int getNumero() { return numero; }
    public void setNumero(int numero) { this.numero = numero; }

    public LocalDate getFechaEmision() { return fechaEmision; }
    public void setFechaEmision(LocalDate fechaEmision) { this.fechaEmision = fechaEmision; }

    public String getDireccionEntrega() { return direccionEntrega; }
    public void setDireccionEntrega(String direccionEntrega) { this.direccionEntrega = direccionEntrega; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public String getRecibidoPor() { return recibidoPor; }
    public void setRecibidoPor(String recibidoPor) { this.recibidoPor = recibidoPor; }

    public String getDocumentoReceptor() { return documentoReceptor; }
    public void setDocumentoReceptor(String documentoReceptor) { this.documentoReceptor = documentoReceptor; }

    public OffsetDateTime getFechaEntrega() { return fechaEntrega; }
    public void setFechaEntrega(OffsetDateTime fechaEntrega) { this.fechaEntrega = fechaEntrega; }

    public OffsetDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(OffsetDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }

    public String getClienteNombre() { return clienteNombre; }
    public void setClienteNombre(String clienteNombre) { this.clienteNombre = clienteNombre; }

    public String getClienteDocumento() { return clienteDocumento; }
    public void setClienteDocumento(String clienteDocumento) { this.clienteDocumento = clienteDocumento; }
}
