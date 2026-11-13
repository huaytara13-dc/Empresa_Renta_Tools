package com.rentatools.model;

/**
 * Definicion de un reporte: que consulta SQL ejecuta y como se dibuja el grafico.
 * La PRIMERA columna del resultado es la etiqueta del eje X y la SEGUNDA es el valor
 * que se grafica; las demas columnas solo aparecen en la tabla y en el Excel.
 */
public class ReporteDef {

    private final String id;
    private final String area;
    private final String titulo;
    private final String grafico;     // bar | line | pie
    private final boolean usaFechas;  // si admite filtro Desde / Hasta
    private final String sql;

    public ReporteDef(String id, String area, String titulo, String grafico,
                      boolean usaFechas, String sql) {
        this.id = id;
        this.area = area;
        this.titulo = titulo;
        this.grafico = grafico;
        this.usaFechas = usaFechas;
        this.sql = sql;
    }

    public String getId() { return id; }
    public String getArea() { return area; }
    public String getTitulo() { return titulo; }
    public String getGrafico() { return grafico; }
    public boolean isUsaFechas() { return usaFechas; }
    public String getSql() { return sql; }
}
