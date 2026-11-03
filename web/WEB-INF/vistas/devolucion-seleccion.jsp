<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="java.time.LocalDate"%>
<%@page import="java.util.List"%>
<%@page import="com.rentatools.model.Alquiler"%>
<%@page import="com.rentatools.service.AlquilerService"%>
<% String tituloPagina = "Registrar devolución"; String menuActivo = "devoluciones"; %>
<%@ include file="fragmentos/cabecera.jspf" %>
<%
    @SuppressWarnings("unchecked")
    List<Alquiler> activos = (List<Alquiler>) request.getAttribute("activos");
    LocalDate hoy = (LocalDate) request.getAttribute("hoy");
    String flashTipo = (String) request.getAttribute("flashTipo");
    String flashMensaje = (String) request.getAttribute("flashMensaje");
%>

<div class="mb-4">
    <h1 class="h3 fw-bold mb-1">Registrar devolución</h1>
    <p class="text-secondary mb-0">Elija el alquiler activo cuyos equipos está recibiendo.</p>
</div>

<% if (flashMensaje != null) { %>
<div class="alert alert-<%= Html.esc(flashTipo) %> alert-dismissible fade show" role="alert">
    <%= Html.esc(flashMensaje) %>
    <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Cerrar"></button>
</div>
<% } %>

<section class="card p-4">
    <div class="d-flex flex-wrap justify-content-between align-items-center gap-3 mb-3">
        <h2 class="h5 fw-bold mb-0">Alquileres activos</h2>
        <input type="text" id="buscar" class="form-control" style="max-width: 280px"
               placeholder="Buscar por cliente o número…" aria-label="Buscar alquiler">
    </div>

    <div class="table-responsive">
        <table class="table tabla align-middle" id="tablaActivos">
            <thead>
                <tr><th>N.º</th><th>Cliente</th><th>Equipos</th><th>Devolución prevista</th><th class="text-end">Acciones</th></tr>
            </thead>
            <tbody>
            <% for (Alquiler al : activos) { %>
                <tr>
                    <td class="fw-semibold"><%= al.getId() %></td>
                    <td><%= Html.esc(al.getClienteNombre()) %>
                        <div class="text-secondary small"><%= Html.esc(al.getClienteDocumento()) %></div></td>
                    <td><%= al.getCantidadEquipos() %></td>
                    <td class="text-nowrap"><%= al.getFechaDevolucionPrevista() %>
                        <div><span class="badge <%= AlquilerService.clasePlazo(al, hoy) %>"><%= Html.esc(AlquilerService.etiquetaPlazo(al, hoy)) %></span></div></td>
                    <td class="text-end text-nowrap">
                        <a class="btn btn-sm btn-naranja" href="<%= ctx %>/devoluciones?accion=nueva&amp;alquilerId=<%= al.getId() %>">Registrar devolución</a>
                    </td>
                </tr>
            <% } %>
            </tbody>
        </table>
    </div>
    <% if (activos.isEmpty()) { %><p class="text-secondary mb-0">No hay alquileres activos pendientes de devolución.</p><% } %>
</section>

<script>
    document.getElementById('buscar').addEventListener('input', function () {
        var q = this.value.toLowerCase();
        document.querySelectorAll('#tablaActivos tbody tr').forEach(function (tr) {
            tr.style.display = tr.textContent.toLowerCase().indexOf(q) > -1 ? '' : 'none';
        });
    });
</script>

<%@ include file="fragmentos/pie.jspf" %>
