<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="com.rentatools.model.Alquiler"%>
<%@page import="com.rentatools.model.Comprobante"%>
<%@page import="com.rentatools.model.DetalleAlquiler"%>
<%@page import="com.rentatools.service.ComprobanteService"%>
<% String tituloPagina = "Comprobante"; String menuActivo = "comprobantes"; %>
<%@ include file="fragmentos/cabecera.jspf" %>
<%
    Comprobante co = (Comprobante) request.getAttribute("comprobante");
    Alquiler al = (Alquiler) request.getAttribute("alquiler");
    String flashTipo = (String) request.getAttribute("flashTipo");
    String flashMensaje = (String) request.getAttribute("flashMensaje");
    boolean anulado = "ANULADO".equals(co.getEstado());
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
    <h1 class="h3 fw-bold mb-0"><%= Html.esc(ComprobanteService.etiquetaTipo(co.getTipo())) %> <%= co.getNumeroCompleto() %></h1>
    <div class="d-flex gap-2">
        <a class="btn btn-outline-primary" href="<%= ctx %>/comprobantes">Volver a la lista</a>
        <a class="btn btn-outline-primary" href="<%= ctx %>/alquileres?accion=ver&amp;id=<%= co.getAlquilerId() %>">Ver alquiler N.º <%= co.getAlquilerId() %></a>
        <button type="button" class="btn btn-naranja" onclick="window.print()">Imprimir / guardar PDF</button>
    </div>
</div>

<% if (flashMensaje != null) { %>
<div class="alert alert-<%= Html.esc(flashTipo) %> alert-dismissible fade show no-imprimir" role="alert">
    <%= Html.esc(flashMensaje) %>
    <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Cerrar"></button>
</div>
<% } %>

<% if (anulado) { %>
<div class="alert alert-danger" role="alert"><strong>COMPROBANTE ANULADO.</strong> El alquiler asociado fue anulado.</div>
<% } %>

<section class="card p-4 p-md-5" style="max-width: 900px">
    <div class="d-flex flex-wrap justify-content-between gap-3 mb-4">
        <div>
            <div style="font-size: 28px; font-weight: 800; font-style: italic; letter-spacing: -1px">Renta<span style="color: #fa5a14">Tools</span></div>
            <div class="text-secondary small">Alquiler de equipos y herramientas · Lima, Perú</div>
        </div>
        <div class="border rounded p-3 text-center" style="min-width: 240px">
            <div class="fw-bold text-uppercase"><%= Html.esc(ComprobanteService.etiquetaTipo(co.getTipo())) %></div>
            <div class="fs-5 fw-bold"><%= co.getNumeroCompleto() %></div>
            <div class="small text-secondary">Fecha de emisión: <%= co.getFechaEmision() %></div>
        </div>
    </div>

    <dl class="row mb-4">
        <dt class="col-sm-3 fw-normal text-secondary">Cliente</dt><dd class="col-sm-9"><%= Html.esc(co.getClienteNombre()) %></dd>
        <dt class="col-sm-3 fw-normal text-secondary">Documento</dt><dd class="col-sm-9"><%= Html.esc(co.getClienteDocumento()) %></dd>
        <dt class="col-sm-3 fw-normal text-secondary">Dirección</dt><dd class="col-sm-9"><%= co.getClienteDireccion().isEmpty() ? "—" : Html.esc(co.getClienteDireccion()) %></dd>
        <% if (al != null) { %>
        <dt class="col-sm-3 fw-normal text-secondary">Alquiler</dt>
        <dd class="col-sm-9 mb-0">N.º <%= al.getId() %> · del <%= al.getFechaInicio() %> al <%= al.getFechaDevolucionPrevista() %></dd>
        <% } %>
    </dl>

    <div class="table-responsive">
        <table class="table tabla align-middle">
            <thead><tr><th>Código</th><th>Descripción</th><th class="text-end">Días</th><th class="text-end">Precio por día</th><th class="text-end">Importe</th></tr></thead>
            <tbody>
            <% if (al != null) { for (DetalleAlquiler d : al.getDetalles()) { %>
                <tr>
                    <td class="fw-semibold"><%= Html.esc(d.getEquipoCodigo()) %></td>
                    <td>Alquiler de <%= Html.esc(d.getEquipoNombre()) %></td>
                    <td class="text-end"><%= d.getDias() %></td>
                    <td class="text-end text-nowrap">S/ <%= d.getPrecioDia().toPlainString() %></td>
                    <td class="text-end text-nowrap">S/ <%= d.getSubtotal().toPlainString() %></td>
                </tr>
            <% } } %>
            </tbody>
        </table>
    </div>

    <div class="ms-auto" style="max-width: 320px">
        <dl class="row mb-0">
            <dt class="col-6 fw-normal text-secondary">Subtotal</dt><dd class="col-6 text-end">S/ <%= co.getSubtotal().toPlainString() %></dd>
            <dt class="col-6 fw-normal text-secondary">IGV (18 %)</dt><dd class="col-6 text-end">S/ <%= co.getIgv().toPlainString() %></dd>
            <dt class="col-6 fs-5">Total</dt><dd class="col-6 text-end fs-5 fw-bold">S/ <%= co.getTotal().toPlainString() %></dd>
        </dl>
    </div>

    <% if (al != null) { %>
    <p class="text-secondary small mt-4 mb-0">La garantía de S/ <%= al.getGarantia().toPlainString() %> es reembolsable y no forma parte de este comprobante.</p>
    <% } %>
    <p class="text-secondary small mb-0">Documento de práctica académica: sin validez tributaria.</p>
</section>

<%@ include file="fragmentos/pie.jspf" %>
