package com.rentatools.model;

public class DetalleDevolucion {

    private int equipoId;
    private String estadoEquipo;
    private String equipoCodigo;
    private String equipoNombre;

    public DetalleDevolucion() { }

    public int getEquipoId() { return equipoId; }
    public void setEquipoId(int equipoId) { this.equipoId = equipoId; }

    public String getEstadoEquipo() { return estadoEquipo; }
    public void setEstadoEquipo(String estadoEquipo) { this.estadoEquipo = estadoEquipo; }

    public String getEquipoCodigo() { return equipoCodigo; }
    public void setEquipoCodigo(String equipoCodigo) { this.equipoCodigo = equipoCodigo; }

    public String getEquipoNombre() { return equipoNombre; }
    public void setEquipoNombre(String equipoNombre) { this.equipoNombre = equipoNombre; }
}
