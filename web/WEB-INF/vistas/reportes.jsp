<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="java.util.List"%>
<%@page import="java.util.Map"%>
<%@page import="com.rentatools.model.ReporteDef"%>
<%@page import="com.rentatools.model.ResultadoReporte"%>
<% String tituloPagina = "Reportes"; String menuActivo = "reportes"; %>
<%@ include file="fragmentos/cabecera.jspf" %>
<%
    @SuppressWarnings("unchecked")
    Map<String, List<ReporteDef>> catalogo = (Map<String, List<ReporteDef>>) request.getAttribute("catalogo");
    ReporteDef def = (ReporteDef) request.getAttribute("def");
    ResultadoReporte datos = (ResultadoReporte) request.getAttribute("datos");
    String desde = (String) request.getAttribute("desde");
    String hasta = (String) request.getAttribute("hasta");
    String json = (String) request.getAttribute("json");
%>

<div class="d-flex flex-wrap justify-content-between align-items-center gap-3 mb-4">
    <div>
        <h1 class="h3 fw-bold mb-1">Reportes</h1>
        <p class="text-secondary mb-0">Gr&aacute;ficos con Apache ECharts y descarga de los datos en Excel.</p>
    </div>
    <% if (def != null) { %>
    <a class="btn btn-outline-primary" href="<%= ctx %>/reportes">&larr; Todos los reportes</a>
    <% } %>
</div>

<% if (def == null) { %>

    <% for (Map.Entry<String, List<ReporteDef>> area : catalogo.entrySet()) { %>
    <div class="card shadow-sm mb-4">
        <div class="card-body">
            <h2 class="h5 fw-bold mb-3"><%= Html.esc(area.getKey()) %></h2>
            <div class="row g-2">
                <% for (ReporteDef r : area.getValue()) { %>
                <div class="col-md-6 col-xl-4">
                    <a class="d-block border rounded-3 p-3 text-decoration-none h-100"
                       href="<%= ctx %>/reportes?id=<%= Html.esc(r.getId()) %>">
                        <span class="badge text-bg-primary me-2"><%= Html.esc(r.getId()) %></span>
                        <span class="text-dark"><%= Html.esc(r.getTitulo()) %></span>
                    </a>
                </div>
                <% } %>
            </div>
        </div>
    </div>
    <% } %>

<% } else { %>

    <div class="card shadow-sm mb-4">
        <div class="card-body">
            <div class="d-flex flex-wrap justify-content-between align-items-start gap-3">
                <div>
                    <span class="badge text-bg-primary mb-2"><%= Html.esc(def.getArea()) %> &middot; <%= Html.esc(def.getId()) %></span>
                    <h2 class="h4 fw-bold mb-0"><%= Html.esc(def.getTitulo()) %></h2>
                </div>
                <a class="btn btn-naranja px-4"
                   href="<%= ctx %>/reportes?id=<%= Html.esc(def.getId()) %>&formato=excel&desde=<%= Html.esc(desde) %>&hasta=<%= Html.esc(hasta) %>">
                    Descargar Excel</a>
            </div>

            <% if (def.isUsaFechas()) { %>
            <form class="row g-2 align-items-end mt-3" method="get" action="<%= ctx %>/reportes">
                <input type="hidden" name="id" value="<%= Html.esc(def.getId()) %>">
                <div class="col-auto">
                    <label class="form-label small mb-1" for="desde">Desde</label>
                    <input class="form-control" type="date" id="desde" name="desde" value="<%= Html.esc(desde) %>">
                </div>
                <div class="col-auto">
                    <label class="form-label small mb-1" for="hasta">Hasta</label>
                    <input class="form-control" type="date" id="hasta" name="hasta" value="<%= Html.esc(hasta) %>">
                </div>
                <div class="col-auto">
                    <button class="btn btn-outline-primary" type="submit">Filtrar</button>
                    <a class="btn btn-link" href="<%= ctx %>/reportes?id=<%= Html.esc(def.getId()) %>">Quitar filtro</a>
                </div>
            </form>
            <% } %>
        </div>
    </div>

    <% if (datos.getFilas().isEmpty()) { %>
    <div class="alert alert-info">No hay datos para este reporte con los filtros elegidos.</div>
    <% } else { %>

    <div class="card shadow-sm mb-4">
        <div class="card-body">
            <div id="grafico" style="width:100%;height:380px"></div>
        </div>
    </div>

    <div class="card shadow-sm mb-4">
        <div class="card-body table-responsive">
            <table class="table table-hover align-middle tabla mb-0">
                <thead>
                    <tr>
                        <% for (String c : datos.getColumnas()) { %><th><%= Html.esc(c) %></th><% } %>
                    </tr>
                </thead>
                <tbody>
                    <% for (List<Object> fila : datos.getFilas()) { %>
                    <tr>
                        <% for (Object v : fila) { %><td><%= v == null ? "" : Html.esc(v.toString()) %></td><% } %>
                    </tr>
                    <% } %>
                </tbody>
            </table>
        </div>
    </div>

    <script type="application/json" id="datosReporte"><%= json %></script>
    <script src="https://cdn.jsdelivr.net/npm/echarts@5.5.1/dist/echarts.min.js"></script>
    <script>
        (function () {
            var d = JSON.parse(document.getElementById('datosReporte').textContent);
            var tipo = '<%= Html.esc(def.getGrafico()) %>';
            var colores = ['#1F5FBF', '#F28C28', '#146C3A', '#8E44AD', '#C0392B', '#16A085', '#7F8C8D', '#D4AC0D'];
            var graf = echarts.init(document.getElementById('grafico'));
            var nombre = d.columnas.length > 1 ? d.columnas[1] : 'Valor';
            var opcion;
            if (tipo === 'pie') {
                opcion = {
                    color: colores,
                    tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
                    legend: { bottom: 0 },
                    series: [{ name: nombre, type: 'pie', radius: ['35%', '65%'],
                        data: d.etiquetas.map(function (e, i) { return { name: e, value: d.valores[i] }; }) }]
                };
            } else {
                opcion = {
                    color: colores,
                    tooltip: { trigger: 'axis' },
                    grid: { left: 50, right: 20, top: 40, bottom: 70 },
                    xAxis: { type: 'category', data: d.etiquetas, axisLabel: { rotate: d.etiquetas.length > 8 ? 35 : 0 } },
                    yAxis: { type: 'value', name: nombre },
                    series: [{ name: nombre, type: tipo, data: d.valores, smooth: tipo === 'line',
                        label: { show: d.etiquetas.length <= 12, position: 'top' } }]
                };
            }
            graf.setOption(opcion);
            window.addEventListener('resize', function () { graf.resize(); });
        })();
    </script>

    <% } %>
<% } %>

<%@ include file="fragmentos/pie.jspf" %>
