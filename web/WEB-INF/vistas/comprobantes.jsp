<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="java.math.BigDecimal"%>
<%@page import="java.util.List"%>
<%@page import="com.rentatools.model.Comprobante"%>
<%@page import="com.rentatools.service.ComprobanteService"%>
<% String tituloPagina = "Comprobantes"; String menuActivo = "comprobantes"; %>
<%@ include file="fragmentos/cabecera.jspf" %>
<%
    @SuppressWarnings("unchecked")
    List<Comprobante> comprobantes = (List<Comprobante>) request.getAttribute("comprobantes");
    String flashTipo = (String) request.getAttribute("flashTipo");
    String flashMensaje = (String) request.getAttribute("flashMensaje");
    @SuppressWarnings("unchecked")
    java.util.Map<String, java.math.BigDecimal> ind = (java.util.Map<String, java.math.BigDecimal>) request.getAttribute("ind");
    String busqueda = ((com.rentatools.model.Pagina<?>) request.getAttribute("pag")).getQ();
    int emitidos = ind.get("emitidos").intValue();
    int boletas = ind.get("boletas").intValue();
    int facturas = ind.get("facturas").intValue();
    BigDecimal facturado = ind.get("facturado");
%>

<div class="mb-4">
    <h1 class="h3 fw-bold mb-1">Comprobantes de pago</h1>
    <p class="text-secondary mb-0">Boletas y facturas de los alquileres. Se emiten automáticamente al registrar cada alquiler.</p>
</div>

<% if (flashMensaje != null) { %>
<div class="alert alert-<%= Html.esc(flashTipo) %> alert-dismissible fade show" role="alert">
    <%= Html.esc(flashMensaje) %>
    <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Cerrar"></button>
</div>
<% } %>

<div class="row g-3 mb-4">
    <div class="col-12 col-md-6 col-xl-3"><div class="card kpi p-3"><div class="titulo">Comprobantes emitidos</div><div class="valor"><%= emitidos %></div></div></div>
    <div class="col-12 col-md-6 col-xl-3"><div class="card kpi naranja p-3"><div class="titulo">Boletas</div><div class="valor"><%= boletas %></div></div></div>
    <div class="col-12 col-md-6 col-xl-3"><div class="card kpi p-3"><div class="titulo">Facturas</div><div class="valor"><%= facturas %></div></div></div>
    <div class="col-12 col-md-6 col-xl-3"><div class="card kpi verde p-3"><div class="titulo">Total facturado</div><div class="valor fs-2">S/ <%= facturado.toPlainString() %></div></div></div>
</div>

<section class="card p-4">
    <div class="d-flex flex-wrap justify-content-between align-items-center gap-3 mb-3">
        <h2 class="h5 fw-bold mb-0">Lista de comprobantes</h2>
        <form method="get" action="<%= ctx %>/comprobantes" class="d-flex gap-2">
            <input type="text" name="q" value="<%= Html.esc(busqueda) %>" class="form-control" style="max-width: 280px"
                   placeholder="Buscar comprobante…" aria-label="Buscar comprobante">
            <button class="btn btn-outline-primary" type="submit">Buscar</button>
            <% if (!busqueda.isEmpty()) { %><a class="btn btn-link" href="<%= ctx %>/comprobantes">Limpiar</a><% } %>
        </form>
    </div>

    <div class="table-responsive">
        <table class="table tabla align-middle" id="tablaComprobantes">
            <thead>
                <tr><th>Número</th><th>Tipo</th><th>Fecha</th><th>Cliente</th><th>Alquiler</th><th>Total</th><th>Estado</th><th class="text-end">Acciones</th></tr>
            </thead>
            <tbody>
            <% for (Comprobante co : comprobantes) { %>
                <tr>
                    <td class="fw-semibold text-nowrap"><%= co.getNumeroCompleto() %></td>
                    <td><%= Html.esc(ComprobanteService.etiquetaTipo(co.getTipo())) %></td>
                    <td class="text-nowrap"><%= co.getFechaEmision() %></td>
                    <td><%= Html.esc(co.getClienteNombre()) %>
                        <div class="text-secondary small"><%= Html.esc(co.getClienteDocumento()) %></div></td>
                    <td><a class="text-decoration-none" href="<%= ctx %>/alquileres?accion=ver&amp;id=<%= co.getAlquilerId() %>">N.º <%= co.getAlquilerId() %></a></td>
                    <td class="text-nowrap">S/ <%= co.getTotal().toPlainString() %></td>
                    <td><span class="badge <%= "EMITIDO".equals(co.getEstado()) ? "text-bg-success" : "text-bg-secondary" %>"><%= "EMITIDO".equals(co.getEstado()) ? "Emitido" : "Anulado" %></span></td>
                    <td class="text-end"><a class="btn btn-sm btn-outline-primary" href="<%= ctx %>/comprobantes?accion=ver&amp;id=<%= co.getId() %>">Ver</a></td>
                </tr>
            <% } %>
            </tbody>
        </table>
    </div>
    <% if (comprobantes.isEmpty()) { %><p class="text-secondary mb-0"><%= busqueda.isEmpty() ? "Aún no hay comprobantes emitidos." : "No se encontraron resultados para la búsqueda." %></p><% } %>
    <%@ include file="fragmentos/paginacion.jspf" %>
</section>

<script>
</script>

<%@ include file="fragmentos/pie.jspf" %>
