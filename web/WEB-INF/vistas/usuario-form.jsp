<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="java.util.List"%>
<% String tituloPagina = "Usuario"; String menuActivo = "usuarios"; %>
<%@ include file="fragmentos/cabecera.jspf" %>
<%
    Usuario f = (Usuario) request.getAttribute("usuarioForm");
    boolean editar = "editar".equals(request.getAttribute("modo"));
    @SuppressWarnings("unchecked")
    List<String> errores = (List<String>) request.getAttribute("errores");
    String csrf = (String) request.getAttribute("csrf");
    boolean esPropia = editar && f.getId() == actual.getId();
%>

<div class="mb-4">
    <h1 class="h3 fw-bold mb-1"><%= editar ? "Editar usuario" : "Nuevo usuario" %></h1>
    <p class="text-secondary mb-0">Los campos con * son obligatorios.</p>
</div>

<% if (errores != null && !errores.isEmpty()) { %>
<div class="alert alert-danger" role="alert">
    <ul class="mb-0">
        <% for (String err : errores) { %><li><%= Html.esc(err) %></li><% } %>
    </ul>
</div>
<% } %>

<% if (esPropia) { %>
<div class="alert alert-info">Está editando su propia cuenta: no puede cambiar su rol ni desactivarla.</div>
<% } %>

<form method="post" action="<%= ctx %>/usuarios" class="card p-4" novalidate>
    <input type="hidden" name="accion" value="<%= editar ? "actualizar" : "crear" %>">
    <input type="hidden" name="csrf" value="<%= Html.esc(csrf) %>">
    <% if (editar) { %><input type="hidden" name="id" value="<%= f.getId() %>"><% } %>

    <div class="row g-3">
        <div class="col-md-6">
            <label for="nombres" class="form-label fw-semibold">Nombres *</label>
            <input type="text" id="nombres" name="nombres" class="form-control" maxlength="80" required
                   value="<%= Html.esc(f.getNombres()) %>">
        </div>
        <div class="col-md-6">
            <label for="apellidos" class="form-label fw-semibold">Apellidos *</label>
            <input type="text" id="apellidos" name="apellidos" class="form-control" maxlength="80" required
                   value="<%= Html.esc(f.getApellidos()) %>">
        </div>
        <div class="col-md-6">
            <label for="correo" class="form-label fw-semibold">Correo electrónico *</label>
            <input type="email" id="correo" name="correo" class="form-control" maxlength="120" required
                   value="<%= Html.esc(f.getCorreo()) %>">
        </div>
        <div class="col-md-6">
            <label for="usuario" class="form-label fw-semibold">Usuario *</label>
            <input type="text" id="usuario" name="usuario" class="form-control" maxlength="50"
                   pattern="[A-Za-z0-9._-]{3,50}" autocomplete="off" required
                   value="<%= Html.esc(f.getUsuario()) %>">
            <div class="form-text">De 3 a 50 caracteres: letras, números, punto, guion o guion bajo.</div>
        </div>
        <div class="col-md-6">
            <label for="rol" class="form-label fw-semibold">Rol *</label>
            <select id="rol" name="rol" class="form-select">
                <option value="EMPLEADO" <%= "EMPLEADO".equals(f.getRol()) ? "selected" : "" %>>Empleado</option>
                <option value="ADMIN" <%= "ADMIN".equals(f.getRol()) ? "selected" : "" %>>Administrador</option>
            </select>
        </div>
        <div class="col-md-6">
            <label for="estado" class="form-label fw-semibold">Estado *</label>
            <select id="estado" name="estado" class="form-select">
                <option value="ACTIVO" <%= "ACTIVO".equals(f.getEstado()) ? "selected" : "" %>>Activo</option>
                <option value="INACTIVO" <%= "INACTIVO".equals(f.getEstado()) ? "selected" : "" %>>Inactivo</option>
                <% if ("BLOQUEADO".equals(f.getEstado())) { %><option value="BLOQUEADO" selected>Bloqueado</option><% } %>
            </select>
        </div>
        <div class="col-md-6">
            <label for="password" class="form-label fw-semibold"><%= editar ? "Nueva contraseña" : "Contraseña *" %></label>
            <input type="password" id="password" name="password" class="form-control" autocomplete="new-password"
                   minlength="8" maxlength="72" <%= editar ? "" : "required" %>>
            <div class="form-text"><%= editar ? "Déjela vacía para mantener la actual. " : "" %>Mínimo 8 caracteres.</div>
        </div>
        <div class="col-md-6">
            <label for="password2" class="form-label fw-semibold">Confirmar contraseña</label>
            <input type="password" id="password2" name="password2" class="form-control"
                   autocomplete="new-password" maxlength="72">
        </div>
    </div>

    <div class="mt-4 d-flex gap-2">
        <button type="submit" class="btn btn-naranja px-4"><%= editar ? "Guardar cambios" : "Crear usuario" %></button>
        <a class="btn btn-outline-primary" href="<%= ctx %>/usuarios">Cancelar</a>
    </div>
</form>

<%@ include file="fragmentos/pie.jspf" %>
