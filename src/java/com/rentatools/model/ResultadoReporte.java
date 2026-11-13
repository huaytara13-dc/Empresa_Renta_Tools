package com.rentatools.model;

import java.util.List;

/** Resultado de ejecutar un reporte: nombres de columnas y filas. */
public class ResultadoReporte {

    private final List<String> columnas;
    private final List<List<Object>> filas;

    public ResultadoReporte(List<String> columnas, List<List<Object>> filas) {
        this.columnas = columnas;
        this.filas = filas;
    }

    public List<String> getColumnas() { return columnas; }
    public List<List<Object>> getFilas() { return filas; }
}
