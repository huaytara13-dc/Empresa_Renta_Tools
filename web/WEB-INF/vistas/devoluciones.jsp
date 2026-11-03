<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="java.math.BigDecimal"%>
<%@page import="java.util.List"%>
<%@page import="com.rentatools.model.Devolucion"%>
<% String tituloPagina = "Devoluciones"; String menuActivo = "devoluciones"; %>
<%@ include file="fragmentos/cabecera.jspf" %>
<%
    @SuppressWarnings("unchecked")
    List<Devolucion> devoluciones = (List<Devolucion>) request.getAttribute("devoluciones");
    String flashTipo = (String) request.getAttribute("flashTipo");
    String flashMensaje = (String) request.getAttribute("flashMensaje");
    int conRetraso = 0, conPenalidad = 0;
    BigDecimal sumaPenalidades = BigDecimal.ZERO;
    for (Devolucion q : devoluciones) {
        if (q.getDiasRetraso() > 0) conRetraso++;
        if (q.getPenalidadTotal().signum() > 0) conPenalidad++;
        sumaPenalidades = sumaPenalidades.add(q.getPenalidadTotal());
    }
%>

<div class="d-flex flex-wrap justify-content-between align-items-center gap-3 mb-4">
    <div>
        <h1 class="h3 fw-bold mb-1">Devoluciones</h1>
        <p class="text-secondary mb-0">Recepción de equipos, incidencias y penalidades.</p>
    </div>
    <a class="btn btn-naranja px-4 py-2" href="<%= ctx %>/devoluciones?accion=nueva">+ Registrar devolución</a>
</div>

<% if (flashMensaje != null) { %>
<div class="alert alert-<%= Html.esc(flashTipo) %> alert-dismissible fade show" role="alert">
    <%= Html.esc(flashMensaje) %>
    <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Cerrar"></button>
</div>
<% } %>

<div class="row g-3 mb-4">
    <div class="col-12 col-md-6 col-xl-3"><div class="card kpi p-3"><div class="titulo">Devoluciones registradas</div><div class="valor"><%= devoluciones.size() %></div></div></div>
    <div class="col-12 col-md-6 col-xl-3"><div class="card kpi naranja p-3"><div class="titulo">Con retraso</div><div class="valor"><%= conRetraso %></div></div></div>
    <div class="col-12 col-md-6 col-xl-3"><div class="card kpi p-3" style="border-top-color: #a32a12"><div class="titulo">Con penalidad</div><div class="valor"><%= conPenalidad %></div></div></div>
    <div class="col-12 col-md-6 col-xl-3"><div class="card kpi verde p-3"><div class="titulo">Penalidades acumuladas</div><div class="valor fs-2">S/ <%= sumaPenalidades.toPlainString() %></div></div></div>
</div>

<section class="card p-4">
    <div class="d-flex flex-wrap justify-content-between align-items-center gap-3 mb-3">
        <h2 class="h5 fw-bold mb-0">Lista de devoluciones</h2>
        <input type="text" id="buscar" class="form-control" style="max-width: 280px"
               placeholder="Buscar devolución…" aria-label="Buscar devolución">
    </div>

    <div class="table-responsive">
        <table class="table tabla align-middle" id="tablaDevoluciones">
            <thead>
                <tr>
                    <th>N.º</th><th>Alquiler</th><th>Cliente</th><th>Fecha</th><th>Retraso</th>
                    <th>Incidencias</th><th>Penalidad</th><th>Recibió</th><th class="text-end">Acciones</th>
                </tr>
            </thead>
            <tbody>
            <% for (Devolucion dv : devoluciones) { %>
                <tr>
                    <td class="fw-semibold"><%= dv.getId() %></td>
                    <td><a class="text-decoration-none" href="<%= ctx %>/alquileres?accion=ver&amp;id=<%= dv.getAlquilerId() %>">N.º <%= dv.getAlquilerId() %></a></td>
                    <td><%= Html.esc(dv.getClienteNombre()) %></td>
                    <td class="text-nowrap"><%= dv.getFechaDevolucion() %></td>
                    <td><% if (dv.getDiasRetraso() > 0) { %><span class="badge text-bg-warning"><%= dv.getDiasRetraso() %> día<%= dv.getDiasRetraso() == 1 ? "" : "s" %></span><% } else { %><span class="text-secondary">A tiempo</span><% } %></td>
                    <td><%= dv.getCantidadIncidencias() %></td>
                    <td class="text-nowrap"><%= dv.getPenalidadTotal().signum() > 0 ? "S/ " + dv.getPenalidadTotal().toPlainString() : "—" %></td>
                    <td><%= Html.esc(dv.getUsuarioNombre()) %></td>
                    <td class="text-end text-nowrap">
                        <a class="btn btn-sm btn-outline-primary" href="<%= ctx %>/devoluciones?accion=ver&amp;id=<%= dv.getId() %>">Ver</a>
                    </td>
                </tr>
            <% } %>
            </tbody>
        </table>
    </div>
    <% if (devoluciones.isEmpty()) { %><p class="text-secondary mb-0">Aún no hay devoluciones registradas.</p><% } %>
</section>

<script>
    document.getElementById('buscar').addEventListener('input', function () {
        var q = this.value.toLowerCase();
        document.querySelectorAll('#tablaDevoluciones tbody tr').forEach(function (tr) {
            tr.style.display = tr.textContent.toLowerCase().indexOf(q) > -1 ? '' : 'none';
        });
    });
</script>

<%@ include file="fragmentos/pie.jspf" %>
