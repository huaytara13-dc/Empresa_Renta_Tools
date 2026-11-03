package com.rentatools.service;

/** Datos que el empleado ingresa para cada equipo al recibir la devolucion. */
public class ItemDevolucion {

    private final int equipoId;
    private final String estadoEquipo;
    private final String tipoIncidencia;   // vacio = sin incidencia
    private final String descripcion;
    private final String montoTexto;

    public ItemDevolucion(int equipoId, String estadoEquipo, String tipoIncidencia,
                          String descripcion, String montoTexto) {
        this.equipoId = equipoId;
        this.estadoEquipo = estadoEquipo;
        this.tipoIncidencia = tipoIncidencia;
        this.descripcion = descripcion;
        this.montoTexto = montoTexto;
    }

    public int getEquipoId() { return equipoId; }
    public String getEstadoEquipo() { return estadoEquipo; }
    public String getTipoIncidencia() { return tipoIncidencia; }
    public String getDescripcion() { return descripcion; }
    public String getMontoTexto() { return montoTexto; }
}
