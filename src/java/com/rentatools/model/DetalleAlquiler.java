package com.rentatools.model;

import java.math.BigDecimal;

public class DetalleAlquiler {

    private int id;
    private int alquilerId;
    private int equipoId;
    private int dias;
    private BigDecimal precioDia;
    private BigDecimal subtotal;

    // Datos del equipo, para mostrar en pantalla
    private String equipoCodigo;
    private String equipoNombre;

    public DetalleAlquiler() { }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getAlquilerId() { return alquilerId; }
    public void setAlquilerId(int alquilerId) { this.alquilerId = alquilerId; }

    public int getEquipoId() { return equipoId; }
    public void setEquipoId(int equipoId) { this.equipoId = equipoId; }

    public int getDias() { return dias; }
    public void setDias(int dias) { this.dias = dias; }

    public BigDecimal getPrecioDia() { return precioDia; }
    public void setPrecioDia(BigDecimal precioDia) { this.precioDia = precioDia; }

    public BigDecimal getSubtotal() { return subtotal; }
    public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }

    public String getEquipoCodigo() { return equipoCodigo; }
    public void setEquipoCodigo(String equipoCodigo) { this.equipoCodigo = equipoCodigo; }

    public String getEquipoNombre() { return equipoNombre; }
    public void setEquipoNombre(String equipoNombre) { this.equipoNombre = equipoNombre; }
}
