package com.rentatools.model;

import java.math.BigDecimal;

public class Incidencia {

    private int id;
    private int equipoId;
    private String tipo;
    private String descripcion;
    private BigDecimal monto;
    private String equipoCodigo;

    public Incidencia() { }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getEquipoId() { return equipoId; }
    public void setEquipoId(int equipoId) { this.equipoId = equipoId; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public BigDecimal getMonto() { return monto; }
    public void setMonto(BigDecimal monto) { this.monto = monto; }

    public String getEquipoCodigo() { return equipoCodigo; }
    public void setEquipoCodigo(String equipoCodigo) { this.equipoCodigo = equipoCodigo; }
}
