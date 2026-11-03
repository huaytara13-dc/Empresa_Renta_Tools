<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="java.time.LocalDate"%>
<%@page import="com.rentatools.model.Alquiler"%>
<%@page import="com.rentatools.model.DetalleAlquiler"%>
<%@page import="com.rentatools.model.Devolucion"%>
<%@page import="com.rentatools.service.AlquilerService"%>
<% String tituloPagina = "Alquiler"; String menuActivo = "alquileres"; %>
<%@ include file="fragmentos/cabecera.jspf" %>
<%
    Alquiler al = (Alquiler) request.getAttribute("alquiler");
    Devolucion devolucion = (Devolucion) request.getAttribute("devolucion");
    LocalDate hoy = (LocalDate) request.getAttribute("hoy");
    String csrf = (String) request.getAttribute("csrf");
    String flashTipo = (String) request.getAttribute("flashTipo");
    String flashMensaje = (String) request.getAttribute("flashMensaje");
    String st = al.getEstado();
    boolean activo = "ACTIVO".equals(st);
    String claseEstado = activo ? "text-bg-primary" : "FINALIZADO".equals(st) ? "text-bg-success" : "text-bg-secondary";
    String etiquetaEstado = activo ? "Activo" : "FINALIZADO".equals(st) ? "Finalizado" : "Anulado";
%>

<div class="d-flex flex-wrap justify-content-between align-items-center gap-3 mb-4">
    <div>
        <h1 class="h3 fw-bold mb-1">Alquiler N.º <%= al.getId() %>
            <span class="badge fs-6 align-middle <%= claseEstado %>"><%= etiquetaEstado %></span></h1>
        <p class="text-secondary mb-0">Registrado por <%= Html.esc(al.getUsuarioNombre()) %></p>
    </div>
    <div class="d-flex gap-2">
        <a class="btn btn-outline-primary" href="<%= ctx %>/alquileres">Volver a la lista</a>
        <% if (activo) { %>
        <a class="btn btn-naranja" href="<%= ctx %>/devoluciones?accion=nueva&amp;alquilerId=<%= al.getId() %>">Registrar devolución</a>
        <% } %>
        <% if (devolucion != null) { %>
        <a class="btn btn-outline-primary" href="<%= ctx %>/devoluciones?accion=ver&amp;id=<%= devolucion.getId() %>">Ver devolución</a>
        <% } %>
        <% if (esAdmin && activo) { %>
        <button type="button" class="btn btn-outline-danger" data-bs-toggle="modal" data-bs-target="#modalAnular">Anular alquiler</button>
        <% } %>
    </div>
</div>

<% if (flashMensaje != null) { %>
<div class="alert alert-<%= Html.esc(flashTipo) %> alert-dismissible fade show" role="alert">
    <%= Html.esc(flashMensaje) %>
    <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Cerrar"></button>
</div>
<% } %>

<div class="row g-3">
    <div class="col-12 col-xl-8">
        <section class="card p-4 mb-3">
            <h2 class="h5 fw-bold mb-3">Datos del alquiler</h2>
            <dl class="row mb-0">
                <dt class="col-sm-4 fw-normal text-secondary">Cliente</dt>
                <dd class="col-sm-8"><%= Html.esc(al.getClienteNombre()) %> · <%= Html.esc(al.getClienteDocumento()) %></dd>
                <dt class="col-sm-4 fw-normal text-secondary">Fecha de inicio</dt>
                <dd class="col-sm-8"><%= al.getFechaInicio() %></dd>
                <dt class="col-sm-4 fw-normal text-secondary">Devolución prevista</dt>
                <dd class="col-sm-8"><%= al.getFechaDevolucionPrevista() %>
                    <% if (activo) { %><span class="badge ms-2 <%= AlquilerService.clasePlazo(al, hoy) %>"><%= Html.esc(AlquilerService.etiquetaPlazo(al, hoy)) %></span><% } %></dd>
                <% if (al.getFechaDevolucionReal() != null) { %>
                <dt class="col-sm-4 fw-normal text-secondary">Devolución real</dt>
                <dd class="col-sm-8"><%= al.getFechaDevolucionReal() %></dd>
                <% } %>
                <dt class="col-sm-4 fw-normal text-secondary">Observaciones</dt>
                <dd class="col-sm-8 mb-0"><%= al.getObservaciones().isEmpty() ? "—" : Html.esc(al.getObservaciones()) %></dd>
            </dl>
        </section>

        <section class="card p-4">
            <h2 class="h5 fw-bold mb-3">Equipos</h2>
            <div class="table-responsive">
                <table class="table tabla align-middle mb-0">
                    <thead><tr><th>Código</th><th>Equipo</th><th>Días</th><th>Precio por día</th><th class="text-end">Subtotal</th></tr></thead>
                    <tbody>
                    <% for (DetalleAlquiler d : al.getDetalles()) { %>
                        <tr>
                            <td class="fw-semibold"><%= Html.esc(d.getEquipoCodigo()) %></td>
                            <td><%= Html.esc(d.getEquipoNombre()) %></td>
                            <td><%= d.getDias() %></td>
                            <td class="text-nowrap">S/ <%= d.getPrecioDia().toPlainString() %></td>
                            <td class="text-end text-nowrap">S/ <%= d.getSubtotal().toPlainString() %></td>
                        </tr>
                    <% } %>
                    </tbody>
                </table>
            </div>
        </section>
    </div>

    <div class="col-12 col-xl-4">
        <section class="card p-4">
            <h2 class="h5 fw-bold mb-3">Montos</h2>
            <dl class="row mb-0">
                <dt class="col-6 fw-normal text-secondary">Subtotal</dt><dd class="col-6 text-end">S/ <%= al.getSubtotal().toPlainString() %></dd>
                <dt class="col-6 fw-normal text-secondary">IGV</dt><dd class="col-6 text-end">S/ <%= al.getIgv().toPlainString() %></dd>
                <dt class="col-6">Total</dt><dd class="col-6 text-end fw-bold">S/ <%= al.getTotal().toPlainString() %></dd>
                <dt class="col-6 fw-normal text-secondary">Garantía (reembolsable)</dt><dd class="col-6 text-end">S/ <%= al.getGarantia().toPlainString() %></dd>
            </dl>
        </section>
    </div>
</div>

<% if (esAdmin && activo) { %>
<div class="modal fade" id="modalAnular" tabindex="-1" aria-labelledby="tituloAnular" aria-hidden="true">
    <div class="modal-dialog modal-dialog-centered">
        <form method="post" action="<%= ctx %>/alquileres" class="modal-content">
            <input type="hidden" name="accion" value="anular">
            <input type="hidden" name="id" value="<%= al.getId() %>">
            <input type="hidden" name="csrf" value="<%= Html.esc(csrf) %>">
            <div class="modal-header">
                <h2 class="modal-title h5" id="tituloAnular">Anular alquiler</h2>
                <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Cerrar"></button>
            </div>
            <div class="modal-body">
                ¿Seguro que desea anular el alquiler N.º <strong><%= al.getId() %></strong>?
                Los equipos volverán a estar disponibles. Esta acción no se puede deshacer.
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-outline-primary" data-bs-dismiss="modal">Cancelar</button>
                <button type="submit" class="btn btn-danger">Anular</button>
            </div>
        </form>
    </div>
</div>
<% } %>

<%@ include file="fragmentos/pie.jspf" %>
