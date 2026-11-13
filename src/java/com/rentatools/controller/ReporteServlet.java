package com.rentatools.controller;

import com.rentatools.model.ReporteDef;
import com.rentatools.model.ResultadoReporte;
import com.rentatools.model.Usuario;
import com.rentatools.repository.ReporteCatalogo;
import com.rentatools.repository.ReporteRepository;
import com.rentatools.util.XlsxWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Etapa 7 - Reporteria. Solo el administrador.
 *   /reportes                      -> indice de reportes por area
 *   /reportes?id=V1&desde=&hasta=  -> grafico (Apache ECharts) + tabla
 *   /reportes?id=V1&formato=excel  -> descarga de los datos en Excel (.xlsx)
 */
@WebServlet(name = "ReporteServlet", urlPatterns = {"/reportes"})
public class ReporteServlet extends HttpServlet {

    private static final String VISTAS = "/WEB-INF/vistas/";
    private final ReporteRepository repositorio = new ReporteRepository();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        Usuario actual = (Usuario) request.getAttribute("actual");
        if (actual == null || !"ADMIN".equals(actual.getRol())) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        request.setAttribute("catalogo", ReporteCatalogo.porArea());
        String id = request.getParameter("id");
        ReporteDef def = ReporteCatalogo.buscar(id);
        if (def == null) {
            request.getRequestDispatcher(VISTAS + "reportes.jsp").forward(request, response);
            return;
        }

        LocalDate desde = fecha(request.getParameter("desde"));
        LocalDate hasta = fecha(request.getParameter("hasta"));
        if (desde != null && hasta != null && desde.isAfter(hasta)) {
            LocalDate t = desde;
            desde = hasta;
            hasta = t;
        }

        try {
            ResultadoReporte datos = repositorio.ejecutar(def, desde, hasta);

            if ("excel".equals(request.getParameter("formato"))) {
                enviarExcel(response, def, datos, desde, hasta);
                return;
            }

            request.setAttribute("def", def);
            request.setAttribute("datos", datos);
            request.setAttribute("desde", desde == null ? "" : desde.toString());
            request.setAttribute("hasta", hasta == null ? "" : hasta.toString());
            request.setAttribute("json", aJson(datos));
            request.getRequestDispatcher(VISTAS + "reportes.jsp").forward(request, response);

        } catch (SQLException e) {
            e.printStackTrace();
            throw new ServletException("No se pudo generar el reporte", e);
        }
    }

    private static LocalDate fecha(String s) {
        if (s == null || s.trim().isEmpty()) {
            return null;
        }
        try {
            return LocalDate.parse(s.trim());
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    private void enviarExcel(HttpServletResponse response, ReporteDef def, ResultadoReporte datos,
                             LocalDate desde, LocalDate hasta) throws IOException {
        String titulo = def.getTitulo();
        if (def.isUsaFechas() && (desde != null || hasta != null)) {
            titulo += " (" + (desde == null ? "inicio" : desde) + " a " + (hasta == null ? "hoy" : hasta) + ")";
        }
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition",
                "attachment; filename=\"reporte_" + def.getId() + "_" + LocalDate.now() + ".xlsx\"");
        XlsxWriter.escribir(response.getOutputStream(), def.getId() + " " + def.getTitulo(),
                titulo, datos.getColumnas(), datos.getFilas());
    }

    /** JSON simple para ECharts: {"columnas":[..],"etiquetas":[..],"valores":[..]}. */
    private static String aJson(ResultadoReporte d) {
        StringBuilder et = new StringBuilder("[");
        StringBuilder va = new StringBuilder("[");
        List<List<Object>> filas = d.getFilas();
        for (int i = 0; i < filas.size(); i++) {
            List<Object> f = filas.get(i);
            if (i > 0) {
                et.append(',');
                va.append(',');
            }
            et.append(texto(f.get(0) == null ? "" : f.get(0).toString()));
            Object v = f.size() > 1 ? f.get(1) : null;
            va.append(v instanceof Number ? new java.math.BigDecimal(v.toString()).toPlainString() : "0");
        }
        et.append(']');
        va.append(']');
        StringBuilder col = new StringBuilder("[");
        for (int i = 0; i < d.getColumnas().size(); i++) {
            if (i > 0) {
                col.append(',');
            }
            col.append(texto(d.getColumnas().get(i)));
        }
        col.append(']');
        return "{\"columnas\":" + col + ",\"etiquetas\":" + et + ",\"valores\":" + va + "}";
    }

    private static String texto(String s) {
        StringBuilder b = new StringBuilder("\"");
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '"': b.append("\\\""); break;
                case '\\': b.append("\\\\"); break;
                case '\n': b.append("\\n"); break;
                case '\r': break;
                case '<': b.append("\\u003c"); break;
                case '>': b.append("\\u003e"); break;
                case '&': b.append("\\u0026"); break;
                default:
                    if (c < 0x20) b.append(' '); else b.append(c);
            }
        }
        return b.append('"').toString();
    }
}
