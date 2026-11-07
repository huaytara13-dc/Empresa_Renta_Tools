<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="java.util.List"%>
<%@page import="com.rentatools.model.Proveedor"%>
<%@page import="com.rentatools.service.ProveedorService"%>
<% String tituloPagina = "Proveedor"; String menuActivo = "proveedores"; %>
<%@ include file="fragmentos/cabecera.jspf" %>
<%
    Proveedor f = (Proveedor) request.getAttribute("proveedorForm");
    boolean editar = "editar".equals(request.getAttribute("modo"));
    @SuppressWarnings("unchecked")
    List<String> errores = (List<String>) request.getAttribute("errores");
    String csrf = (String) request.getAttribute("csrf");
%>

<div class="mb-4">
    <h1 class="h3 fw-bold mb-1"><%= editar ? "Editar proveedor" : "Nuevo proveedor" %></h1>
    <p class="text-secondary mb-0">Los campos con * son obligatorios.</p>
</div>

<% if (errores != null && !errores.isEmpty()) { %>
<div class="alert alert-danger" role="alert">
    <ul class="mb-0">
        <% for (String err : errores) { %><li><%= Html.esc(err) %></li><% } %>
    </ul>
</div>
<% } %>

<form method="post" action="<%= ctx %>/proveedores" class="card p-4" novalidate>
    <input type="hidden" name="accion" value="<%= editar ? "actualizar" : "crear" %>">
    <input type="hidden" name="csrf" value="<%= Html.esc(csrf) %>">
    <% if (editar) { %><input type="hidden" name="id" value="<%= f.getId() %>"><% } %>

    <div class="row g-3">
        <div class="col-md-4">
            <label for="ruc" class="form-label fw-semibold">RUC *</label>
            <input type="text" id="ruc" name="ruc" class="form-control" maxlength="11" inputmode="numeric"
                   autocomplete="off" required value="<%= Html.esc(f.getRuc()) %>">
            <div class="form-text">11 dígitos.</div>
        </div>
        <div class="col-md-8">
            <label for="razonSocial" class="form-label fw-semibold">Razón social *</label>
            <input type="text" id="razonSocial" name="razonSocial" class="form-control" maxlength="150" required
                   value="<%= Html.esc(f.getRazonSocial()) %>">
        </div>
        <div class="col-md-6">
            <label for="rubro" class="form-label fw-semibold">Rubro *</label>
            <select id="rubro" name="rubro" class="form-select">
                <option value="">Seleccione…</option>
                <% for (String rb : ProveedorService.RUBROS) { %>
                <option value="<%= Html.esc(rb) %>" <%= rb.equals(f.getRubro()) ? "selected" : "" %>><%= Html.esc(rb) %></option>
                <% } %>
            </select>
        </div>
        <div class="col-md-6">
            <label for="contacto" class="form-label fw-semibold">Persona de contacto</label>
            <input type="text" id="contacto" name="contacto" class="form-control" maxlength="100"
                   value="<%= Html.esc(f.getContacto()) %>">
        </div>
        <div class="col-md-6">
            <label for="telefono" class="form-label fw-semibold">Teléfono</label>
            <input type="text" id="telefono" name="telefono" class="form-control" maxlength="20"
                   value="<%= Html.esc(f.getTelefono()) %>">
        </div>
        <div class="col-md-6">
            <label for="correo" class="form-label fw-semibold">Correo electrónico</label>
            <input type="email" id="correo" name="correo" class="form-control" maxlength="120"
                   value="<%= Html.esc(f.getCorreo()) %>">
        </div>
        <div class="col-md-8">
            <label for="direccion" class="form-label fw-semibold">Dirección</label>
            <input type="text" id="direccion" name="direccion" class="form-control" maxlength="200"
                   value="<%= Html.esc(f.getDireccion()) %>">
        </div>
        <div class="col-md-4">
            <label for="estado" class="form-label fw-semibold">Estado *</label>
            <select id="estado" name="estado" class="form-select">
                <option value="ACTIVO" <%= "ACTIVO".equals(f.getEstado()) ? "selected" : "" %>>Activo</option>
                <option value="INACTIVO" <%= "INACTIVO".equals(f.getEstado()) ? "selected" : "" %>>Inactivo</option>
            </select>
        </div>
    </div>

    <div class="mt-4 d-flex gap-2">
        <button type="submit" class="btn btn-naranja px-4"><%= editar ? "Guardar cambios" : "Registrar proveedor" %></button>
        <a class="btn btn-outline-primary" href="<%= ctx %>/proveedores">Cancelar</a>
    </div>
</form>

<%@ include file="fragmentos/pie.jspf" %>
