<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="com.rentatools.model.Alquiler"%>
<%@page import="com.rentatools.model.DetalleAlquiler"%>
<%@page import="com.rentatools.model.GuiaEntrega"%>
<%@page import="com.rentatools.service.GuiaService"%>
<% String tituloPagina = "Guía de entrega"; String menuActivo = "guias"; %>
<%@ include file="fragmentos/cabecera.jspf" %>
<%
    GuiaEntrega g = (GuiaEntrega) request.getAttribute("guia");
    Alquiler al = (Alquiler) request.getAttribute("alquiler");
    String csrf = (String) request.getAttribute("csrf");
    String flashTipo = (String) request.getAttribute("flashTipo");
    String flashMensaje = (String) request.getAttribute("flashMensaje");
    boolean emitida = "EMITIDA".equals(g.getEstado());
    boolean entregada = "ENTREGADA".equals(g.getEstado());
%>

<style>
    @media print {
        .sidebar, .topbar, .no-imprimir { display: none !important; }
        .app { display: block; }
        main { padding: 0 !important; }
        .card { box-shadow: none !important; border: 1px solid #ccc !important; }
    }
</style>

<div class="d-flex flex-wrap justify-content-between align-items-center gap-3 mb-4 no-imprimir">
    <h1 class="h3 fw-bold mb-0">Guía de entrega <%= g.getNumeroCompleto() %>
        <span class="badge fs-6 align-middle <%= GuiaService.claseEstado(g.getEstado()) %>"><%= Html.esc(GuiaService.etiquetaEstado(g.getEstado())) %></span></h1>
    <div class="d-flex gap-2">
        <a class="btn btn-outline-primary" href="<%= ctx %>/guias">Volver a la lista</a>
        <a class="btn btn-outline-primary" href="<%= ctx %>/alquileres?accion=ver&amp;id=<%= g.getAlquilerId() %>">Ver alquiler N.º <%= g.getAlquilerId() %></a>
        <button type="button" class="btn btn-naranja" onclick="window.print()">Imprimir / guardar PDF</button>
    </div>
</div>

<% if (flashMensaje != null) { %>
<div class="alert alert-<%= Html.esc(flashTipo) %> alert-dismissible fade show no-imprimir" role="alert">
    <%= Html.esc(flashMensaje) %>
    <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Cerrar"></button>
</div>
<% } %>

<% if ("ANULADA".equals(g.getEstado())) { %>
<div class="alert alert-danger" role="alert"><strong>GUÍA ANULADA.</strong> El alquiler asociado fue anulado.</div>
<% } %>

<section class="card p-4 p-md-5 mb-3" style="max-width: 900px">
    <div class="d-flex flex-wrap justify-content-between gap-3 mb-4">
        <div>
            <div style="font-size: 28px; font-weight: 800; font-style: italic; letter-spacing: -1px">Renta<span style="color: #fa5a14">Tools</span></div>
            <div class="text-secondary small">Alquiler de equipos y herramientas · Lima, Perú</div>
        </div>
        <div class="border rounded p-3 text-center" style="min-width: 240px">
            <div class="fw-bold text-uppercase">Guía de entrega</div>
            <div class="fs-5 fw-bold"><%= g.getNumeroCompleto() %></div>
            <div class="small text-secondary">Fecha de emisión: <%= g.getFechaEmision() %></div>
        </div>
    </div>

    <dl class="row mb-4">
        <dt class="col-sm-3 fw-normal text-secondary">Cliente</dt><dd class="col-sm-9"><%= Html.esc(g.getClienteNombre()) %></dd>
        <dt class="col-sm-3 fw-normal text-secondary">Documento</dt><dd class="col-sm-9"><%= Html.esc(g.getClienteDocumento()) %></dd>
        <dt class="col-sm-3 fw-normal text-secondary">Lugar de entrega</dt><dd class="col-sm-9"><%= g.getDireccionEntrega().isEmpty() ? "—" : Html.esc(g.getDireccionEntrega()) %></dd>
        <% if (al != null) { %>
        <dt class="col-sm-3 fw-normal text-secondary">Alquiler</dt><dd class="col-sm-9">N.º <%= al.getId() %></dd>
        <dt class="col-sm-3 fw-normal text-secondary">Devolver el</dt><dd class="col-sm-9 mb-0"><strong><%= al.getFechaDevolucionPrevista() %></strong></dd>
        <% } %>
    </dl>

    <div class="table-responsive">
        <table class="table tabla align-middle">
            <thead><tr><th>Código</th><th>Equipo</th><th class="text-end">Días de alquiler</th></tr></thead>
            <tbody>
            <% if (al != null) { for (DetalleAlquiler d : al.getDetalles()) { %>
                <tr>
                    <td class="fw-semibold"><%= Html.esc(d.getEquipoCodigo()) %></td>
                    <td><%= Html.esc(d.getEquipoNombre()) %></td>
                    <td class="text-end"><%= d.getDias() %></td>
                </tr>
            <% } } %>
            </tbody>
        </table>
    </div>

    <div class="row mt-4 pt-3">
        <div class="col-6">
            <% if (entregada) { %>
            <div class="small text-secondary">Recibido conforme por</div>
            <div class="fw-semibold"><%= Html.esc(g.getRecibidoPor()) %></div>
            <div class="small">Documento: <%= Html.esc(g.getDocumentoReceptor()) %></div>
            <div class="small text-secondary">Fecha: <%= g.getFechaEntrega() != null ? g.getFechaEntrega().toLocalDate() : "" %></div>
            <% } else { %>
            <div style="border-top: 1px solid #444; margin-top: 56px; padding-top: 6px" class="small text-secondary">Firma y DNI del cliente (conformidad)</div>
            <% } %>
        </div>
        <div class="col-6 text-end">
            <div style="border-top: 1px solid #444; margin-top: 56px; padding-top: 6px" class="small text-secondary">RentaTools</div>
        </div>
    </div>
</section>

<% if (emitida) { %>
<section class="card p-4 no-imprimir" style="max-width: 900px">
    <h2 class="h5 fw-bold mb-3">Registrar conformidad de entrega</h2>
    <form method="post" action="<%= ctx %>/guias" class="row g-3" novalidate>
        <input type="hidden" name="accion" value="conformidad">
        <input type="hidden" name="id" value="<%= g.getId() %>">
        <input type="hidden" name="csrf" value="<%= Html.esc(csrf) %>">
        <div class="col-md-7">
            <label for="recibidoPor" class="form-label fw-semibold">Recibido por *</label>
            <input type="text" id="recibidoPor" name="recibidoPor" class="form-control" maxlength="100" required>
        </div>
        <div class="col-md-5">
            <label for="documentoReceptor" class="form-label fw-semibold">DNI o documento *</label>
            <input type="text" id="documentoReceptor" name="documentoReceptor" class="form-control" maxlength="12" required>
        </div>
        <div class="col-12">
            <button type="submit" class="btn btn-naranja">Registrar conformidad</button>
        </div>
    </form>
</section>
<% } %>

<%@ include file="fragmentos/pie.jspf" %>
