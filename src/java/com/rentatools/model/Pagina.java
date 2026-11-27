package com.rentatools.model;

import java.util.List;

/** Una pagina de resultados de un listado (items + datos para dibujar la paginacion). */
public class Pagina<T> {

    private final List<T> items;
    private final int pagina;
    private final int tamano;
    private final int total;
    private final String q;

    public Pagina(List<T> items, int pagina, int tamano, int total, String q) {
        this.items = items;
        this.pagina = pagina;
        this.tamano = tamano;
        this.total = total;
        this.q = q == null ? "" : q;
    }

    public List<T> getItems() { return items; }
    public int getPagina() { return pagina; }
    public int getTamano() { return tamano; }
    /** Total de registros que cumplen la busqueda (todas las paginas). */
    public int getTotal() { return total; }
    public String getQ() { return q; }

    public int getTotalPaginas() {
        return Math.max(1, (total + tamano - 1) / tamano);
    }

    /** Numero del primer registro mostrado (1, 11, 21...). */
    public int getDesde() { return total == 0 ? 0 : (pagina - 1) * tamano + 1; }

    /** Numero del ultimo registro mostrado. */
    public int getHasta() { return Math.min(total, pagina * tamano); }
}
