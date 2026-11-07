<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="com.rentatools.model.DetalleSolicitud"%>
<%@page import="com.rentatools.model.Solicitud"%>
<%@page import="com.rentatools.service.EquipoService"%>
<%@page import="com.rentatools.service.SolicitudService"%>
<% String tituloPagina = "Solicitud de alquiler"; String menuActivo = "solicitudes"; %>
<%@ include file="fragmentos/cabecera.jspf" %>
<%
    Solicitud so = (Solicitud) request.getAttribute("solicitud");
    String csrf = (String) request.getAttribute("csrf");
    String flashTipo = (String) request.getAttribute("flashTipo");
    String flashMensaje = (String) request.getAttribute("flashMensaje");
    String st = so.getEstado();
    boolean pendiente = "PENDIENTE".equals(st);
    boolean aprobada = "APROBADA".equals(st);
%>

<div class="d-flex flex-wrap justify-content-between align-items-center gap-3 mb-4">
    <div>
        <h1 class="h3 fw-bold mb-1">Solicitud N.º <%= so.getId() %>
            <span class="badge fs-6 align-middle <%= SolicitudService.claseEstado(st) %>"><%= Html.esc(SolicitudService.etiquetaEstado(st)) %></span></h1>
        <p class="text-secondary mb-0">Registrada por <%= Html.esc(so.getUsuarioNombre()) %></p>
    </div>
    <div class="d-flex flex-wrap gap-2">
        <a class="btn btn-outline-primary" href="<%= ctx %>/solicitudes">Volver a la lista</a>
        <% if (pendiente) { %>
        <a class="btn btn-outline-primary" href="<%= ctx %>/solicitudes?accion=editar&amp;id=<%= so.getId() %>">Editar</a>
        <% } %>
        <% if (aprobada) { %>
        <form method="post" action="<%= ctx %>/solicitudes" class="d-inline"
              onsubmit="return confirm('¿Convertir esta solicitud en alquiler? Los equipos quedarán como alquilados.');">
            <input type="hidden" name="accion" value="convertir">
            <input type="hidden" name="id" value="<%= so.getId() %>">
            <input type="hidden" name="csrf" value="<%= Html.esc(csrf) %>">
            <button type="submit" class="btn btn-naranja">Convertir en alquiler</button>
        </form>
        <% } %>
        <% if (so.getAlquilerId() > 0) { %>
        <a class="btn btn-naranja" href="<%= ctx %>/alquileres?accion=ver&amp;id=<%= so.getAlquilerId() %>">Ver alquiler N.º <%= so.getAlquilerId() %></a>
        <% } %>
    </div>
</div>

<% if (flashMensaje != null) { %>
<div class="alert alert-<%= Html.esc(flashTipo) %> alert-dismissible fade show" role="alert">
    <%= Html.esc(flashMensaje) %>
    <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Cerrar"></button>
</div>
<% } %>

<% if (esAdmin && pendiente) { %>
<div class="card p-3 mb-3 border-warning">
    <div class="d-flex flex-wrap align-items-center gap-2">
        <strong class="me-2">Decisión del administrador:</strong>
        <form method="post" action="<%= ctx %>/solicitudes" class="d-inline">
            <input type="hidden" name="accion" value="aprobar">
            <input type="hidden" name="id" value="<%= so.getId() %>">
            <input type="hidden" name="csrf" value="<%= Html.esc(csrf) %>">
            <button type="submit" class="btn btn-success btn-sm">Aprobar</button>
        </form>
        <form method="post" action="<%= ctx %>/solicitudes" class="d-inline"
              onsubmit="return confirm('¿Rechazar esta solicitud?');">
            <input type="hidden" name="accion" value="rechazar">
            <input type="hidden" name="id" value="<%= so.getId() %>">
            <input type="hidden" name="csrf" value="<%= Html.esc(csrf) %>">
            <button type="submit" class="btn btn-outline-danger btn-sm">Rechazar</button>
        </form>
    </div>
</div>
<% } %>

<div class="row g-3">
    <div class="col-12 col-xl-8">
        <section class="card p-4 mb-3">
            <h2 class="h5 fw-bold mb-3">Datos de la solicitud</h2>
            <dl class="row mb-0">
                <dt class="col-sm-4 fw-normal text-secondary">Cliente</dt>
                <dd class="col-sm-8"><%= Html.esc(so.getClienteNombre()) %> · <%= Html.esc(so.getClienteDocumento()) %></dd>
                <dt class="col-sm-4 fw-normal text-secondary">Fecha de registro</dt>
                <dd class="col-sm-8"><%= so.getFechaCreacion() != null ? so.getFechaCreacion().toLocalDate() : "" %></dd>
                <dt class="col-sm-4 fw-normal text-secondary">Días solicitados</dt>
                <dd class="col-sm-8"><%= so.getDiasSolicitados() %></dd>
                <% if (so.getFechaResolucion() != null) { %>
                <dt class="col-sm-4 fw-normal text-secondary">Resuelta por</dt>
                <dd class="col-sm-8"><%= Html.esc(so.getAprobadorNombre()) %> · <%= so.getFechaResolucion().toLocalDate() %></dd>
                <% } %>
                <dt class="col-sm-4 fw-normal text-secondary">Observaciones</dt>
                <dd class="col-sm-8 mb-0"><%= so.getObservaciones().isEmpty() ? "—" : Html.esc(so.getObservaciones()) %></dd>
            </dl>
        </section>

        <section class="card p-4">
            <h2 class="h5 fw-bold mb-3">Equipos solicitados</h2>
            <div class="table-responsive">
                <table class="table tabla align-middle mb-0">
                    <thead><tr><th>Código</th><th>Equipo</th><th>Estado actual</th><th>Días</th><th>Precio por día</th><th class="text-end">Subtotal</th></tr></thead>
                    <tbody>
                    <% for (DetalleSolicitud d : so.getDetalles()) {
                           boolean libre = "DISPONIBLE".equals(d.getEquipoEstado());
                    %>
                        <tr>
                            <td class="fw-semibold"><%= Html.esc(d.getEquipoCodigo()) %></td>
                            <td><%= Html.esc(d.getEquipoNombre()) %></td>
                            <td><span class="badge <%= libre ? "text-bg-success" : "text-bg-warning" %>"><%= Html.esc(EquipoService.etiquetaEstado(d.getEquipoEstado())) %></span></td>
                            <td><%= d.getDias() %></td>
                            <td class="text-nowrap">S/ <%= d.getPrecioDia().toPlainString() %></td>
                            <td class="text-end text-nowrap">S/ <%= d.getSubtotal().toPlainString() %></td>
                        </tr>
                    <% } %>
                    </tbody>
                </table>
            </div>
            <% if (aprobada) { %>
            <p class="text-secondary small mt-3 mb-0">Al convertirla, el alquiler se calcula con los precios vigentes ese día y los equipos deben seguir disponibles.</p>
            <% } %>
        </section>
    </div>

    <div class="col-12 col-xl-4">
        <section class="card p-4 mb-3">
            <h2 class="h5 fw-bold mb-3">Montos estimados</h2>
            <dl class="row mb-0">
                <dt class="col-6 fw-normal text-secondary">Subtotal</dt><dd class="col-6 text-end">S/ <%= so.getSubtotal().toPlainString() %></dd>
                <dt class="col-6 fw-normal text-secondary">IGV</dt><dd class="col-6 text-end">S/ <%= so.getIgv().toPlainString() %></dd>
                <dt class="col-6">Total</dt><dd class="col-6 text-end fw-bold">S/ <%= so.getTotal().toPlainString() %></dd>
                <dt class="col-6 fw-normal text-secondary">Garantía</dt><dd class="col-6 text-end">S/ <%= so.getGarantia().toPlainString() %></dd>
            </dl>
        </section>

        <% if (esAdmin && (pendiente || aprobada)) { %>
        <section class="card p-4">
            <h2 class="h6 fw-bold mb-2">Anular solicitud</h2>
            <p class="text-secondary small">La solicitud deja de ser válida y no se podrá convertir en alquiler.</p>
            <form method="post" action="<%= ctx %>/solicitudes"
                  onsubmit="return confirm('¿Anular la solicitud N.º <%= so.getId() %>? Esta acción no se puede deshacer.');">
                <input type="hidden" name="accion" value="anular">
                <input type="hidden" name="id" value="<%= so.getId() %>">
                <input type="hidden" name="csrf" value="<%= Html.esc(csrf) %>">
                <button type="submit" class="btn btn-outline-danger btn-sm">Anular solicitud</button>
            </form>
        </section>
        <% } %>
    </div>
</div>

<%@ include file="fragmentos/pie.jspf" %>
