<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="com.rentatools.model.DetalleDevolucion"%>
<%@page import="com.rentatools.model.Devolucion"%>
<%@page import="com.rentatools.model.Incidencia"%>
<%@page import="com.rentatools.service.DevolucionService"%>
<%@page import="com.rentatools.service.EquipoService"%>
<% String tituloPagina = "Devolución"; String menuActivo = "devoluciones"; %>
<%@ include file="fragmentos/cabecera.jspf" %>
<%
    Devolucion dv = (Devolucion) request.getAttribute("devolucion");
    String flashTipo = (String) request.getAttribute("flashTipo");
    String flashMensaje = (String) request.getAttribute("flashMensaje");
%>

<div class="d-flex flex-wrap justify-content-between align-items-center gap-3 mb-4">
    <div>
        <h1 class="h3 fw-bold mb-1">Devolución N.º <%= dv.getId() %></h1>
        <p class="text-secondary mb-0">Recibida por <%= Html.esc(dv.getUsuarioNombre()) %></p>
    </div>
    <div class="d-flex gap-2">
        <a class="btn btn-outline-primary" href="<%= ctx %>/devoluciones">Volver a la lista</a>
        <a class="btn btn-outline-primary" href="<%= ctx %>/alquileres?accion=ver&amp;id=<%= dv.getAlquilerId() %>">Ver alquiler N.º <%= dv.getAlquilerId() %></a>
    </div>
</div>

<% if (flashMensaje != null) { %>
<div class="alert alert-<%= Html.esc(flashTipo) %> alert-dismissible fade show" role="alert">
    <%= Html.esc(flashMensaje) %>
    <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Cerrar"></button>
</div>
<% } %>

<% if (dv.getMontoAdicional().signum() > 0) { %>
<div class="alert alert-danger" role="alert">
    Las penalidades superan la garantía: se debe cobrar al cliente <strong>S/ <%= dv.getMontoAdicional().toPlainString() %></strong> adicionales.
</div>
<% } %>

<div class="row g-3">
    <div class="col-12 col-xl-8">
        <section class="card p-4 mb-3">
            <h2 class="h5 fw-bold mb-3">Datos de la devolución</h2>
            <dl class="row mb-0">
                <dt class="col-sm-4 fw-normal text-secondary">Cliente</dt>
                <dd class="col-sm-8"><%= Html.esc(dv.getClienteNombre()) %></dd>
                <dt class="col-sm-4 fw-normal text-secondary">Fecha de devolución</dt>
                <dd class="col-sm-8"><%= dv.getFechaDevolucion() %></dd>
                <dt class="col-sm-4 fw-normal text-secondary">Retraso</dt>
                <dd class="col-sm-8"><%= dv.getDiasRetraso() > 0 ? dv.getDiasRetraso() + (dv.getDiasRetraso() == 1 ? " día" : " días") : "Devuelto a tiempo" %></dd>
                <dt class="col-sm-4 fw-normal text-secondary">Observaciones</dt>
                <dd class="col-sm-8 mb-0"><%= dv.getObservaciones().isEmpty() ? "—" : Html.esc(dv.getObservaciones()) %></dd>
            </dl>
        </section>

        <section class="card p-4 mb-3">
            <h2 class="h5 fw-bold mb-3">Equipos recibidos</h2>
            <div class="table-responsive">
                <table class="table tabla align-middle mb-0">
                    <thead><tr><th>Código</th><th>Equipo</th><th>Quedó</th></tr></thead>
                    <tbody>
                    <% for (DetalleDevolucion d : dv.getDetalles()) {
                           String est = d.getEstadoEquipo();
                           String claseEst = "DISPONIBLE".equals(est) ? "text-bg-success"
                                   : "MANTENIMIENTO".equals(est) ? "text-bg-warning"
                                   : "INCIDENCIA".equals(est) ? "text-bg-danger" : "text-bg-secondary";
                    %>
                        <tr>
                            <td class="fw-semibold"><%= Html.esc(d.getEquipoCodigo()) %></td>
                            <td><%= Html.esc(d.getEquipoNombre()) %></td>
                            <td><span class="badge <%= claseEst %>"><%= Html.esc(EquipoService.etiquetaEstado(est)) %></span></td>
                        </tr>
                    <% } %>
                    </tbody>
                </table>
            </div>
        </section>

        <section class="card p-4">
            <h2 class="h5 fw-bold mb-3">Incidencias</h2>
            <% if (dv.getIncidencias().isEmpty()) { %>
            <p class="text-secondary mb-0">No se registraron incidencias.</p>
            <% } else { %>
            <div class="table-responsive">
                <table class="table tabla align-middle mb-0">
                    <thead><tr><th>Equipo</th><th>Tipo</th><th>Descripción</th><th class="text-end">Monto</th></tr></thead>
                    <tbody>
                    <% for (Incidencia inc : dv.getIncidencias()) { %>
                        <tr>
                            <td class="fw-semibold"><%= Html.esc(inc.getEquipoCodigo()) %></td>
                            <td><span class="badge text-bg-light border"><%= Html.esc(DevolucionService.etiquetaTipo(inc.getTipo())) %></span></td>
                            <td><%= Html.esc(inc.getDescripcion()) %></td>
                            <td class="text-end text-nowrap">S/ <%= inc.getMonto().toPlainString() %></td>
                        </tr>
                    <% } %>
                    </tbody>
                </table>
            </div>
            <% } %>
        </section>
    </div>

    <div class="col-12 col-xl-4">
        <section class="card p-4">
            <h2 class="h5 fw-bold mb-3">Liquidación</h2>
            <dl class="row mb-0">
                <dt class="col-7 fw-normal text-secondary">Penalidad por retraso</dt><dd class="col-5 text-end">S/ <%= dv.getPenalidadRetraso().toPlainString() %></dd>
                <dt class="col-7 fw-normal text-secondary">Penalidad por incidencias</dt><dd class="col-5 text-end">S/ <%= dv.getPenalidadIncidencias().toPlainString() %></dd>
                <dt class="col-7">Penalidad total</dt><dd class="col-5 text-end fw-bold">S/ <%= dv.getPenalidadTotal().toPlainString() %></dd>
                <dt class="col-7">Garantía devuelta</dt><dd class="col-5 text-end fw-bold text-success">S/ <%= dv.getGarantiaDevuelta().toPlainString() %></dd>
                <dt class="col-7">Cobro adicional</dt><dd class="col-5 text-end fw-bold <%= dv.getMontoAdicional().signum() > 0 ? "text-danger" : "" %>">S/ <%= dv.getMontoAdicional().toPlainString() %></dd>
            </dl>
        </section>
    </div>
</div>

<%@ include file="fragmentos/pie.jspf" %>
