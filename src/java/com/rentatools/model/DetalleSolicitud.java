package com.rentatools.model;

import java.math.BigDecimal;

public class DetalleSolicitud {

    private int id;
    private int solicitudId;
    private int equipoId;
    private int dias;
    private BigDecimal precioDia;
    private BigDecimal subtotal;

    // Datos del equipo, para mostrar en pantalla
    private String equipoCodigo;
    private String equipoNombre;
    private String equipoEstado;

    public DetalleSolicitud() { }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getSolicitudId() { return solicitudId; }
    public void setSolicitudId(int solicitudId) { this.solicitudId = solicitudId; }

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

    public String getEquipoEstado() { return equipoEstado; }
    public void setEquipoEstado(String equipoEstado) { this.equipoEstado = equipoEstado; }
}
