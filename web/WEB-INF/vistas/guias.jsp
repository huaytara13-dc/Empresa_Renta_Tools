<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="java.util.List"%>
<%@page import="com.rentatools.model.GuiaEntrega"%>
<%@page import="com.rentatools.service.GuiaService"%>
<% String tituloPagina = "Guías de entrega"; String menuActivo = "guias"; %>
<%@ include file="fragmentos/cabecera.jspf" %>
<%
    @SuppressWarnings("unchecked")
    List<GuiaEntrega> guias = (List<GuiaEntrega>) request.getAttribute("guias");
    String flashTipo = (String) request.getAttribute("flashTipo");
    String flashMensaje = (String) request.getAttribute("flashMensaje");
    @SuppressWarnings("unchecked")
    java.util.Map<String, Integer> conteos = (java.util.Map<String, Integer>) request.getAttribute("conteos");
    String busqueda = ((com.rentatools.model.Pagina<?>) request.getAttribute("pag")).getQ();
    int pendientes = conteos.getOrDefault("EMITIDA", 0);
    int entregadas = conteos.getOrDefault("ENTREGADA", 0);
    int anuladas = conteos.getOrDefault("ANULADA", 0);
%>

<div class="mb-4">
    <h1 class="h3 fw-bold mb-1">Guías de entrega</h1>
    <p class="text-secondary mb-0">Constancia de entrega de los equipos, con la conformidad del cliente.</p>
</div>

<% if (flashMensaje != null) { %>
<div class="alert alert-<%= Html.esc(flashTipo) %> alert-dismissible fade show" role="alert">
    <%= Html.esc(flashMensaje) %>
    <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Cerrar"></button>
</div>
<% } %>

<div class="row g-3 mb-4">
    <div class="col-12 col-md-4"><div class="card kpi naranja p-3"><div class="titulo">Pendientes de conformidad</div><div class="valor"><%= pendientes %></div></div></div>
    <div class="col-12 col-md-4"><div class="card kpi verde p-3"><div class="titulo">Entregadas</div><div class="valor"><%= entregadas %></div></div></div>
    <div class="col-12 col-md-4"><div class="card kpi p-3" style="border-top-color: #6c757d"><div class="titulo">Anuladas</div><div class="valor"><%= anuladas %></div></div></div>
</div>

<section class="card p-4">
    <div class="d-flex flex-wrap justify-content-between align-items-center gap-3 mb-3">
        <h2 class="h5 fw-bold mb-0">Lista de guías</h2>
        <form method="get" action="<%= ctx %>/guias" class="d-flex gap-2">
            <input type="text" name="q" value="<%= Html.esc(busqueda) %>" class="form-control" style="max-width: 280px"
                   placeholder="Buscar guía…" aria-label="Buscar guía">
            <button class="btn btn-outline-primary" type="submit">Buscar</button>
            <% if (!busqueda.isEmpty()) { %><a class="btn btn-link" href="<%= ctx %>/guias">Limpiar</a><% } %>
        </form>
    </div>

    <div class="table-responsive">
        <table class="table tabla align-middle" id="tablaGuias">
            <thead>
                <tr><th>Número</th><th>Fecha</th><th>Cliente</th><th>Alquiler</th><th>Dirección de entrega</th><th>Estado</th><th class="text-end">Acciones</th></tr>
            </thead>
            <tbody>
            <% for (GuiaEntrega g : guias) { %>
                <tr>
                    <td class="fw-semibold text-nowrap"><%= g.getNumeroCompleto() %></td>
                    <td class="text-nowrap"><%= g.getFechaEmision() %></td>
                    <td><%= Html.esc(g.getClienteNombre()) %>
                        <div class="text-secondary small"><%= Html.esc(g.getClienteDocumento()) %></div></td>
                    <td><a class="text-decoration-none" href="<%= ctx %>/alquileres?accion=ver&amp;id=<%= g.getAlquilerId() %>">N.º <%= g.getAlquilerId() %></a></td>
                    <td><%= g.getDireccionEntrega().isEmpty() ? "—" : Html.esc(g.getDireccionEntrega()) %></td>
                    <td><span class="badge <%= GuiaService.claseEstado(g.getEstado()) %>"><%= Html.esc(GuiaService.etiquetaEstado(g.getEstado())) %></span></td>
                    <td class="text-end"><a class="btn btn-sm btn-outline-primary" href="<%= ctx %>/guias?accion=ver&amp;id=<%= g.getId() %>">Ver</a></td>
                </tr>
            <% } %>
            </tbody>
        </table>
    </div>
    <% if (guias.isEmpty()) { %><p class="text-secondary mb-0"><%= busqueda.isEmpty() ? "Aún no hay guías emitidas." : "No se encontraron resultados para la búsqueda." %></p><% } %>
    <%@ include file="fragmentos/paginacion.jspf" %>
</section>

<script>
</script>

<%@ include file="fragmentos/pie.jspf" %>
