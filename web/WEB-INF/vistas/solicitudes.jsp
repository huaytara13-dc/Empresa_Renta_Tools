<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="java.util.List"%>
<%@page import="com.rentatools.model.Solicitud"%>
<%@page import="com.rentatools.service.SolicitudService"%>
<% String tituloPagina = "Solicitudes de alquiler"; String menuActivo = "solicitudes"; %>
<%@ include file="fragmentos/cabecera.jspf" %>
<%
    @SuppressWarnings("unchecked")
    List<Solicitud> solicitudes = (List<Solicitud>) request.getAttribute("solicitudes");
    String flashTipo = (String) request.getAttribute("flashTipo");
    String flashMensaje = (String) request.getAttribute("flashMensaje");
    int pendientes = 0, aprobadas = 0, atendidas = 0, otras = 0;
    for (Solicitud q : solicitudes) {
        String e = q.getEstado();
        if ("PENDIENTE".equals(e)) pendientes++;
        else if ("APROBADA".equals(e)) aprobadas++;
        else if ("ATENDIDA".equals(e)) atendidas++;
        else otras++;
    }
%>

<div class="d-flex flex-wrap justify-content-between align-items-center gap-3 mb-4">
    <div>
        <h1 class="h3 fw-bold mb-1">Solicitudes de alquiler</h1>
        <p class="text-secondary mb-0">Pedido del cliente: se aprueba y luego se convierte en alquiler.</p>
    </div>
    <a class="btn btn-naranja px-4 py-2" href="<%= ctx %>/solicitudes?accion=nueva">+ Nueva solicitud</a>
</div>

<% if (flashMensaje != null) { %>
<div class="alert alert-<%= Html.esc(flashTipo) %> alert-dismissible fade show" role="alert">
    <%= Html.esc(flashMensaje) %>
    <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Cerrar"></button>
</div>
<% } %>

<div class="row g-3 mb-4">
    <div class="col-12 col-md-6 col-xl-3"><div class="card kpi naranja p-3"><div class="titulo">Pendientes de aprobación</div><div class="valor"><%= pendientes %></div></div></div>
    <div class="col-12 col-md-6 col-xl-3"><div class="card kpi p-3"><div class="titulo">Aprobadas, por convertir</div><div class="valor"><%= aprobadas %></div></div></div>
    <div class="col-12 col-md-6 col-xl-3"><div class="card kpi verde p-3"><div class="titulo">Atendidas (alquiler creado)</div><div class="valor"><%= atendidas %></div></div></div>
    <div class="col-12 col-md-6 col-xl-3"><div class="card kpi p-3" style="border-top-color: #6c757d"><div class="titulo">Rechazadas o anuladas</div><div class="valor"><%= otras %></div></div></div>
</div>

<section class="card p-4">
    <div class="d-flex flex-wrap justify-content-between align-items-center gap-3 mb-3">
        <h2 class="h5 fw-bold mb-0">Lista de solicitudes</h2>
        <input type="text" id="buscar" class="form-control" style="max-width: 280px"
               placeholder="Buscar solicitud…" aria-label="Buscar solicitud">
    </div>

    <div class="table-responsive">
        <table class="table tabla align-middle" id="tablaSolicitudes">
            <thead>
                <tr>
                    <th>N.º</th><th>Cliente</th><th>Equipos</th><th>Días</th><th>Total estimado</th>
                    <th>Estado</th><th>Registró</th><th class="text-end">Acciones</th>
                </tr>
            </thead>
            <tbody>
            <% for (Solicitud so : solicitudes) { %>
                <tr>
                    <td class="fw-semibold"><%= so.getId() %></td>
                    <td><%= Html.esc(so.getClienteNombre()) %>
                        <div class="text-secondary small"><%= Html.esc(so.getClienteDocumento()) %></div></td>
                    <td><%= so.getCantidadEquipos() %></td>
                    <td><%= so.getDiasSolicitados() %></td>
                    <td class="text-nowrap">S/ <%= so.getTotal().toPlainString() %></td>
                    <td><span class="badge <%= SolicitudService.claseEstado(so.getEstado()) %>"><%= Html.esc(SolicitudService.etiquetaEstado(so.getEstado())) %></span></td>
                    <td><%= Html.esc(so.getUsuarioNombre()) %></td>
                    <td class="text-end text-nowrap">
                        <a class="btn btn-sm btn-outline-primary" href="<%= ctx %>/solicitudes?accion=ver&amp;id=<%= so.getId() %>">Ver</a>
                    </td>
                </tr>
            <% } %>
            </tbody>
        </table>
    </div>
    <% if (solicitudes.isEmpty()) { %><p class="text-secondary mb-0">Aún no hay solicitudes registradas.</p><% } %>
</section>

<script>
    document.getElementById('buscar').addEventListener('input', function () {
        var q = this.value.toLowerCase();
        document.querySelectorAll('#tablaSolicitudes tbody tr').forEach(function (tr) {
            tr.style.display = tr.textContent.toLowerCase().indexOf(q) > -1 ? '' : 'none';
        });
    });
</script>

<%@ include file="fragmentos/pie.jspf" %>
