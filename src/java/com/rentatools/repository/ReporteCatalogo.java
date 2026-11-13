package com.rentatools.repository;

import com.rentatools.model.ReporteDef;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Catalogo de reportes (Etapa 7). Cada reporte es una consulta SQL de solo lectura.
 * Para agregar un reporte nuevo basta con una linea mas en este archivo.
 */
public final class ReporteCatalogo {

    public static final String USUARIOS = "Usuarios";
    public static final String CLIENTES = "Clientes";
    public static final String PROVEEDORES = "Proveedores";
    public static final String EQUIPOS = "Equipos";
    public static final String ALQUILERES = "Alquileres (ventas)";
    public static final String DOCUMENTOS = "Documentos y devoluciones";

    /** Filtro Desde/Hasta sobre una columna de fecha (4 parametros). */
    private static String rango(String col) {
        return "(?::date IS NULL OR " + col + " >= ?::date) AND "
             + "(?::date IS NULL OR " + col + " < ?::date + 1)";
    }

    /** Monto de ingreso de un alquiler: subtotal + IGV (la garantia se devuelve, no es ingreso). */
    private static final String INGRESO = "(a.subtotal + a.igv)";

    private static final List<ReporteDef> LISTA = new ArrayList<>();

    private static void add(String id, String area, String titulo, String graf, boolean f, String sql) {
        LISTA.add(new ReporteDef(id, area, titulo, graf, f, sql));
    }

    static {
        // ---------------- USUARIOS ----------------
        add("U1", USUARIOS, "Usuarios registrados por mes", "bar", false,
            "SELECT to_char(fecha_creacion,'YYYY-MM') AS \"Mes\", COUNT(*) AS \"Usuarios registrados\" "
          + "FROM usuarios GROUP BY 1 ORDER BY 1");
        add("U2", USUARIOS, "Usuarios activos / inactivos / bloqueados", "pie", false,
            "SELECT estado AS \"Estado\", COUNT(*) AS \"Usuarios\" FROM usuarios GROUP BY estado ORDER BY estado");
        add("U3", USUARIOS, "Usuarios por rol", "pie", false,
            "SELECT rol AS \"Rol\", COUNT(*) AS \"Usuarios\" FROM usuarios GROUP BY rol ORDER BY rol");
        add("U4", USUARIOS, "Accesos al sistema por día", "line", true,
            "SELECT to_char(fecha,'YYYY-MM-DD') AS \"Día\", COUNT(*) AS \"Accesos exitosos\" "
          + "FROM registro_accesos WHERE evento = 'LOGIN_OK' AND " + rango("fecha") + " GROUP BY 1 ORDER BY 1");
        add("U5", USUARIOS, "Intentos de acceso fallidos y bloqueos", "bar", true,
            "SELECT usuario AS \"Usuario\", "
          + "COUNT(*) FILTER (WHERE evento='LOGIN_FALLIDO') AS \"Intentos fallidos\", "
          + "COUNT(*) FILTER (WHERE evento='BLOQUEO') AS \"Bloqueos\" "
          + "FROM registro_accesos WHERE evento IN ('LOGIN_FALLIDO','BLOQUEO') AND " + rango("fecha")
          + " GROUP BY usuario ORDER BY 2 DESC");
        add("U6", USUARIOS, "Actividades por usuario", "bar", true,
            "SELECT usuario AS \"Usuario\", COUNT(*) AS \"Actividades\" FROM auditoria WHERE "
          + rango("fecha") + " GROUP BY usuario ORDER BY 2 DESC");
        add("U7", USUARIOS, "Cambios realizados por módulo", "pie", true,
            "SELECT modulo AS \"Módulo\", COUNT(*) AS \"Cambios\" FROM auditoria WHERE "
          + rango("fecha") + " GROUP BY modulo ORDER BY 2 DESC");

        // ---------------- CLIENTES ----------------
        add("C1", CLIENTES, "Clientes nuevos por mes", "bar", false,
            "SELECT to_char(fecha_creacion,'YYYY-MM') AS \"Mes\", COUNT(*) AS \"Clientes nuevos\" "
          + "FROM clientes GROUP BY 1 ORDER BY 1");
        add("C2", CLIENTES, "Clientes activos / inactivos", "pie", false,
            "SELECT estado AS \"Estado\", COUNT(*) AS \"Clientes\" FROM clientes GROUP BY estado ORDER BY estado");
        add("C3", CLIENTES, "Clientes por tipo de documento", "pie", false,
            "SELECT tipo_documento AS \"Tipo de documento\", COUNT(*) AS \"Clientes\" "
          + "FROM clientes GROUP BY tipo_documento ORDER BY 2 DESC");
        add("C4", CLIENTES, "Clientes frecuentes (más alquileres)", "bar", true,
            "SELECT c.nombres || ' ' || c.apellidos AS \"Cliente\", COUNT(a.id) AS \"Alquileres\", "
          + "COALESCE(SUM(" + INGRESO + "),0) AS \"Monto S/\" "
          + "FROM alquileres a JOIN clientes c ON c.id = a.cliente_id "
          + "WHERE a.estado <> 'ANULADO' AND " + rango("a.fecha_creacion")
          + " GROUP BY c.id, c.nombres, c.apellidos ORDER BY 2 DESC, 3 DESC LIMIT 10");
        add("C5", CLIENTES, "Clientes con mayor volumen de compras (S/)", "bar", true,
            "SELECT c.nombres || ' ' || c.apellidos AS \"Cliente\", COALESCE(SUM(" + INGRESO + "),0) AS \"Monto S/\", "
          + "COUNT(a.id) AS \"Alquileres\" "
          + "FROM alquileres a JOIN clientes c ON c.id = a.cliente_id "
          + "WHERE a.estado <> 'ANULADO' AND " + rango("a.fecha_creacion")
          + " GROUP BY c.id, c.nombres, c.apellidos ORDER BY 2 DESC LIMIT 10");
        add("C6", CLIENTES, "Historial de alquileres por cliente", "bar", true,
            "SELECT c.nombres || ' ' || c.apellidos AS \"Cliente\", COUNT(a.id) AS \"Alquileres\", "
          + "COALESCE(SUM(" + INGRESO + "),0) AS \"Total S/\", "
          + "ROUND(COALESCE(AVG(" + INGRESO + "),0),2) AS \"Promedio S/\", "
          + "MAX(a.fecha_inicio) AS \"Último alquiler\" "
          + "FROM clientes c LEFT JOIN alquileres a ON a.cliente_id = c.id AND a.estado <> 'ANULADO' AND "
          + rango("a.fecha_creacion") + " GROUP BY c.id, c.nombres, c.apellidos ORDER BY 2 DESC, 1");
        add("C7", CLIENTES, "Alquileres pendientes de devolución por cliente", "bar", false,
            "SELECT c.nombres || ' ' || c.apellidos AS \"Cliente\", COUNT(a.id) AS \"Alquileres activos\", "
          + "SUM(" + INGRESO + ") AS \"Monto S/\", MIN(a.fecha_devolucion_prevista) AS \"Próxima devolución\" "
          + "FROM alquileres a JOIN clientes c ON c.id = a.cliente_id WHERE a.estado = 'ACTIVO' "
          + "GROUP BY c.id, c.nombres, c.apellidos ORDER BY 2 DESC");
        add("C8", CLIENTES, "Evolución acumulada de clientes", "line", false,
            "SELECT mes AS \"Mes\", SUM(n) OVER (ORDER BY mes) AS \"Clientes acumulados\", n AS \"Nuevos en el mes\" "
          + "FROM (SELECT to_char(fecha_creacion,'YYYY-MM') AS mes, COUNT(*) AS n FROM clientes GROUP BY 1) t ORDER BY 1");

        // ---------------- PROVEEDORES ----------------
        add("P1", PROVEEDORES, "Proveedores registrados por mes", "bar", false,
            "SELECT to_char(fecha_creacion,'YYYY-MM') AS \"Mes\", COUNT(*) AS \"Proveedores\" "
          + "FROM proveedores GROUP BY 1 ORDER BY 1");
        add("P2", PROVEEDORES, "Proveedores activos / inactivos", "pie", false,
            "SELECT estado AS \"Estado\", COUNT(*) AS \"Proveedores\" FROM proveedores GROUP BY estado ORDER BY estado");
        add("P3", PROVEEDORES, "Proveedores por rubro", "pie", false,
            "SELECT COALESCE(NULLIF(rubro,''),'Sin rubro') AS \"Rubro\", COUNT(*) AS \"Proveedores\" "
          + "FROM proveedores GROUP BY 1 ORDER BY 2 DESC");

        // ---------------- EQUIPOS ----------------
        add("E1", EQUIPOS, "Equipos registrados por categoría", "bar", false,
            "SELECT COALESCE(NULLIF(categoria,''),'Sin categoría') AS \"Categoría\", COUNT(*) AS \"Equipos\" "
          + "FROM equipos GROUP BY 1 ORDER BY 2 DESC");
        add("E2", EQUIPOS, "Disponibilidad de equipos por estado", "pie", false,
            "SELECT estado AS \"Estado\", COUNT(*) AS \"Equipos\" FROM equipos GROUP BY estado ORDER BY 2 DESC");
        add("E3", EQUIPOS, "Equipos más alquilados", "bar", true,
            "SELECT e.codigo || ' - ' || e.nombre AS \"Equipo\", COUNT(d.id) AS \"Veces alquilado\", "
          + "COALESCE(SUM(d.dias),0) AS \"Días alquilado\" "
          + "FROM equipos e JOIN detalle_alquiler d ON d.equipo_id = e.id "
          + "JOIN alquileres a ON a.id = d.alquiler_id WHERE a.estado <> 'ANULADO' AND " + rango("a.fecha_creacion")
          + " GROUP BY e.id, e.codigo, e.nombre ORDER BY 2 DESC, 3 DESC LIMIT 10");
        add("E4", EQUIPOS, "Equipos menos alquilados", "bar", true,
            "SELECT e.codigo || ' - ' || e.nombre AS \"Equipo\", COUNT(a.id) AS \"Veces alquilado\" "
          + "FROM equipos e LEFT JOIN detalle_alquiler d ON d.equipo_id = e.id "
          + "LEFT JOIN alquileres a ON a.id = d.alquiler_id AND a.estado <> 'ANULADO' AND " + rango("a.fecha_creacion")
          + " WHERE e.estado <> 'BAJA' GROUP BY e.id, e.codigo, e.nombre ORDER BY 2 ASC, 1 LIMIT 10");
        add("E5", EQUIPOS, "Precios de alquiler por día", "bar", false,
            "SELECT codigo || ' - ' || nombre AS \"Equipo\", precio_dia AS \"Precio por día S/\", "
          + "categoria AS \"Categoría\", marca AS \"Marca\" "
          + "FROM equipos WHERE estado <> 'BAJA' ORDER BY precio_dia DESC LIMIT 20");
        add("E6", EQUIPOS, "Equipos dados de baja", "bar", false,
            "SELECT COALESCE(NULLIF(categoria,''),'Sin categoría') AS \"Categoría\", COUNT(*) AS \"Equipos de baja\" "
          + "FROM equipos WHERE estado = 'BAJA' GROUP BY 1 ORDER BY 2 DESC");
        add("E7", EQUIPOS, "Ingresos generados por equipo (valorización)", "bar", true,
            "SELECT e.codigo || ' - ' || e.nombre AS \"Equipo\", COALESCE(SUM(d.subtotal),0) AS \"Ingresos S/\", "
          + "COUNT(d.id) AS \"Veces alquilado\" "
          + "FROM equipos e JOIN detalle_alquiler d ON d.equipo_id = e.id "
          + "JOIN alquileres a ON a.id = d.alquiler_id WHERE a.estado <> 'ANULADO' AND " + rango("a.fecha_creacion")
          + " GROUP BY e.id, e.codigo, e.nombre ORDER BY 2 DESC LIMIT 15");

        // ---------------- ALQUILERES (VENTAS) ----------------
        add("V1", ALQUILERES, "Alquileres por mes", "bar", true,
            "SELECT to_char(a.fecha_creacion,'YYYY-MM') AS \"Mes\", COUNT(*) AS \"Alquileres\", "
          + "SUM(" + INGRESO + ") AS \"Monto S/\" FROM alquileres a "
          + "WHERE a.estado <> 'ANULADO' AND " + rango("a.fecha_creacion") + " GROUP BY 1 ORDER BY 1");
        add("V2", ALQUILERES, "Alquileres por día", "line", true,
            "SELECT to_char(a.fecha_creacion,'YYYY-MM-DD') AS \"Día\", COUNT(*) AS \"Alquileres\", "
          + "SUM(" + INGRESO + ") AS \"Monto S/\" FROM alquileres a "
          + "WHERE a.estado <> 'ANULADO' AND " + rango("a.fecha_creacion") + " GROUP BY 1 ORDER BY 1");
        add("V3", ALQUILERES, "Alquileres por semana", "line", true,
            "SELECT to_char(date_trunc('week', a.fecha_creacion),'YYYY-MM-DD') AS \"Semana (inicio)\", COUNT(*) AS \"Alquileres\", "
          + "SUM(" + INGRESO + ") AS \"Monto S/\" FROM alquileres a "
          + "WHERE a.estado <> 'ANULADO' AND " + rango("a.fecha_creacion") + " GROUP BY 1 ORDER BY 1");
        add("V4", ALQUILERES, "Alquileres por año", "bar", true,
            "SELECT to_char(a.fecha_creacion,'YYYY') AS \"Año\", COUNT(*) AS \"Alquileres\", "
          + "SUM(" + INGRESO + ") AS \"Monto S/\" FROM alquileres a "
          + "WHERE a.estado <> 'ANULADO' AND " + rango("a.fecha_creacion") + " GROUP BY 1 ORDER BY 1");
        add("V5", ALQUILERES, "Alquileres por usuario (vendedor)", "bar", true,
            "SELECT u.usuario AS \"Usuario\", COUNT(a.id) AS \"Alquileres\", SUM(" + INGRESO + ") AS \"Monto S/\" "
          + "FROM alquileres a JOIN usuarios u ON u.id = a.usuario_id "
          + "WHERE a.estado <> 'ANULADO' AND " + rango("a.fecha_creacion") + " GROUP BY u.usuario ORDER BY 2 DESC");
        add("V6", ALQUILERES, "Alquileres por categoría de equipo", "pie", true,
            "SELECT COALESCE(NULLIF(e.categoria,''),'Sin categoría') AS \"Categoría\", SUM(d.subtotal) AS \"Ingresos S/\", "
          + "COUNT(d.id) AS \"Veces alquilado\" "
          + "FROM detalle_alquiler d JOIN equipos e ON e.id = d.equipo_id JOIN alquileres a ON a.id = d.alquiler_id "
          + "WHERE a.estado <> 'ANULADO' AND " + rango("a.fecha_creacion") + " GROUP BY 1 ORDER BY 2 DESC");
        add("V7", ALQUILERES, "Alquileres anulados, activos y finalizados", "pie", true,
            "SELECT a.estado AS \"Estado\", COUNT(*) AS \"Alquileres\", SUM(a.total) AS \"Monto total S/\" "
          + "FROM alquileres a WHERE " + rango("a.fecha_creacion") + " GROUP BY a.estado ORDER BY 2 DESC");
        add("V8", ALQUILERES, "Ingresos por mes (alquiler + penalidades)", "bar", true,
            "SELECT m.mes AS \"Mes\", ROUND(m.alq + COALESCE(p.pen,0),2) AS \"Ingreso total S/\", "
          + "m.alq AS \"Por alquileres S/\", COALESCE(p.pen,0) AS \"Por penalidades S/\" FROM "
          + "(SELECT to_char(a.fecha_creacion,'YYYY-MM') AS mes, SUM(" + INGRESO + ") AS alq FROM alquileres a "
          + "WHERE a.estado <> 'ANULADO' AND " + rango("a.fecha_creacion") + " GROUP BY 1) m "
          + "LEFT JOIN (SELECT to_char(fecha_devolucion,'YYYY-MM') AS mes, SUM(penalidad_total) AS pen FROM devoluciones GROUP BY 1) p "
          + "ON p.mes = m.mes ORDER BY 1");
        add("V9", ALQUILERES, "Ticket promedio por mes", "line", true,
            "SELECT to_char(a.fecha_creacion,'YYYY-MM') AS \"Mes\", ROUND(AVG(" + INGRESO + "),2) AS \"Ticket promedio S/\", "
          + "COUNT(*) AS \"Alquileres\" FROM alquileres a WHERE a.estado <> 'ANULADO' AND " + rango("a.fecha_creacion")
          + " GROUP BY 1 ORDER BY 1");
        add("V10", ALQUILERES, "Evolución acumulada de ingresos", "line", true,
            "SELECT mes AS \"Mes\", SUM(monto) OVER (ORDER BY mes) AS \"Ingreso acumulado S/\", monto AS \"Ingreso del mes S/\" FROM "
          + "(SELECT to_char(a.fecha_creacion,'YYYY-MM') AS mes, SUM(" + INGRESO + ") AS monto FROM alquileres a "
          + "WHERE a.estado <> 'ANULADO' AND " + rango("a.fecha_creacion") + " GROUP BY 1) t ORDER BY 1");

        // ---------------- DOCUMENTOS Y DEVOLUCIONES ----------------
        add("D1", DOCUMENTOS, "Comprobantes emitidos por tipo", "pie", true,
            "SELECT tipo AS \"Tipo\", COUNT(*) AS \"Comprobantes\", SUM(total) AS \"Monto S/\" "
          + "FROM comprobantes WHERE estado = 'EMITIDO' AND " + rango("fecha_emision") + " GROUP BY tipo ORDER BY 2 DESC");
        add("D2", DOCUMENTOS, "Incidencias por tipo", "bar", true,
            "SELECT tipo AS \"Tipo de incidencia\", COUNT(*) AS \"Incidencias\", COALESCE(SUM(monto),0) AS \"Monto S/\" "
          + "FROM incidencias WHERE " + rango("fecha_creacion") + " GROUP BY tipo ORDER BY 2 DESC");
        add("D3", DOCUMENTOS, "Devoluciones con retraso por mes", "bar", true,
            "SELECT to_char(fecha_devolucion,'YYYY-MM') AS \"Mes\", COUNT(*) FILTER (WHERE dias_retraso > 0) AS \"Con retraso\", "
          + "COUNT(*) AS \"Devoluciones\", COALESCE(SUM(penalidad_total),0) AS \"Penalidades S/\" "
          + "FROM devoluciones WHERE " + rango("fecha_devolucion") + " GROUP BY 1 ORDER BY 1");
    }

    private ReporteCatalogo() { }

    public static List<ReporteDef> todos() { return LISTA; }

    public static ReporteDef buscar(String id) {
        if (id == null) return null;
        for (ReporteDef r : LISTA) {
            if (r.getId().equals(id)) return r;
        }
        return null;
    }

    /** Reportes agrupados por area, en el orden en que se declararon. */
    public static Map<String, List<ReporteDef>> porArea() {
        Map<String, List<ReporteDef>> m = new LinkedHashMap<>();
        for (ReporteDef r : LISTA) {
            m.computeIfAbsent(r.getArea(), k -> new ArrayList<>()).add(r);
        }
        return m;
    }
}
