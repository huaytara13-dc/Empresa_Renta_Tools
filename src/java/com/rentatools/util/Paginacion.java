package com.rentatools.util;

import java.sql.PreparedStatement;
import java.sql.SQLException;

/** Utilidades comunes para paginar y buscar en los listados. */
public final class Paginacion {

    /** Registros por pagina. */
    public static final int TAMANO = 10;

    private Paginacion() { }

    /** Texto de busqueda limpio (nunca null, maximo 60 caracteres). */
    public static String texto(String s) {
        if (s == null) {
            return "";
        }
        s = s.trim();
        return s.length() > 60 ? s.substring(0, 60) : s;
    }

    /** Mapa con las claves indicadas en cero (indicadores vacios cuando la busqueda es en vivo). */
    public static java.util.Map<String, java.math.BigDecimal> ceros(String... claves) {
        java.util.Map<String, java.math.BigDecimal> m = new java.util.LinkedHashMap<>();
        for (String c : claves) {
            m.put(c, java.math.BigDecimal.ZERO);
        }
        return m;
    }

    /** Numero de pagina pedido; si es invalido devuelve 1. */
    public static int entero(String s, int porDefecto) {
        try {
            int n = Integer.parseInt(s.trim());
            return n < 1 ? porDefecto : n;
        } catch (RuntimeException e) {
            return porDefecto;
        }
    }

    /** Ajusta la pagina pedida al rango valido segun el total de registros. */
    public static int ajustar(int pagina, int total, int tamano) {
        int ultima = Math.max(1, (total + tamano - 1) / tamano);
        return Math.min(Math.max(1, pagina), ultima);
    }

    /** WHERE con una condicion ILIKE por cada columna (vacio si no hay busqueda). */
    public static String where(String q, String... columnas) {
        if (q == null || q.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder(" WHERE (");
        for (int i = 0; i < columnas.length; i++) {
            if (i > 0) {
                sb.append(" OR ");
            }
            sb.append(columnas[i]).append(" ILIKE ?");
        }
        return sb.append(")").toString();
    }

    /**
     * Asigna el patron de busqueda a los parametros del WHERE. La busqueda es "empieza con"
     * (de izquierda a derecha): el texto escrito debe coincidir con el INICIO de la columna. Devuelve el indice del
     * siguiente parametro libre. Los comodines % _ \ escritos por el usuario se escapan.
     */
    public static int asignar(PreparedStatement ps, String q, int cantidadColumnas) throws SQLException {
        int i = 1;
        if (q != null && !q.isEmpty()) {
            String patron = q.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_") + "%";
            for (; i <= cantidadColumnas; i++) {
                ps.setString(i, patron);
            }
        }
        return i;
    }
}
