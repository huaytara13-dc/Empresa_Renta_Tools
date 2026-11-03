<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="java.util.List"%>
<%@page import="com.rentatools.model.Cliente"%>
<% String tituloPagina = "Cliente"; String menuActivo = "clientes"; %>
<%@ include file="fragmentos/cabecera.jspf" %>
<%
    Cliente f = (Cliente) request.getAttribute("clienteForm");
    boolean editar = "editar".equals(request.getAttribute("modo"));
    @SuppressWarnings("unchecked")
    List<String> errores = (List<String>) request.getAttribute("errores");
    String csrf = (String) request.getAttribute("csrf");
%>

<div class="mb-4">
    <h1 class="h3 fw-bold mb-1"><%= editar ? "Editar cliente" : "Nuevo cliente" %></h1>
    <p class="text-secondary mb-0">Los campos con * son obligatorios.</p>
</div>

<% if (errores != null && !errores.isEmpty()) { %>
<div class="alert alert-danger" role="alert">
    <ul class="mb-0">
        <% for (String err : errores) { %><li><%= Html.esc(err) %></li><% } %>
    </ul>
</div>
<% } %>

<form method="post" action="<%= ctx %>/clientes" class="card p-4" novalidate>
    <input type="hidden" name="accion" value="<%= editar ? "actualizar" : "crear" %>">
    <input type="hidden" name="csrf" value="<%= Html.esc(csrf) %>">
    <% if (editar) { %><input type="hidden" name="id" value="<%= f.getId() %>"><% } %>

    <div class="row g-3">
        <div class="col-md-4">
            <label for="tipoDocumento" class="form-label fw-semibold">Tipo de documento *</label>
            <select id="tipoDocumento" name="tipoDocumento" class="form-select">
                <option value="DNI" <%= "DNI".equals(f.getTipoDocumento()) ? "selected" : "" %>>DNI</option>
                <option value="RUC" <%= "RUC".equals(f.getTipoDocumento()) ? "selected" : "" %>>RUC</option>
                <option value="CE" <%= "CE".equals(f.getTipoDocumento()) ? "selected" : "" %>>Carnet de extranjería</option>
            </select>
        </div>
        <div class="col-md-8">
            <label for="numeroDocumento" class="form-label fw-semibold">Número de documento *</label>
            <input type="text" id="numeroDocumento" name="numeroDocumento" class="form-control"
                   maxlength="15" autocomplete="off" required
                   value="<%= Html.esc(f.getNumeroDocumento()) %>">
            <div class="form-text">DNI: 8 dígitos · RUC: 11 dígitos · Carnet: 9 a 12 letras o números.</div>
        </div>
        <div class="col-md-6">
            <label for="nombres" class="form-label fw-semibold">Nombres o razón social *</label>
            <input type="text" id="nombres" name="nombres" class="form-control" maxlength="100" required
                   value="<%= Html.esc(f.getNombres()) %>">
        </div>
        <div class="col-md-6">
            <label for="apellidos" class="form-label fw-semibold">Apellidos</label>
            <input type="text" id="apellidos" name="apellidos" class="form-control" maxlength="100"
                   value="<%= Html.esc(f.getApellidos()) %>">
            <div class="form-text">Obligatorio para DNI y carnet. Déjelo vacío si es una empresa (RUC).</div>
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
        <button type="submit" class="btn btn-naranja px-4"><%= editar ? "Guardar cambios" : "Registrar cliente" %></button>
        <a class="btn btn-outline-primary" href="<%= ctx %>/clientes">Cancelar</a>
    </div>
</form>

<%@ include file="fragmentos/pie.jspf" %>
