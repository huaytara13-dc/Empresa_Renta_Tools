<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="java.util.List"%>
<%@page import="com.rentatools.model.Equipo"%>
<%@page import="com.rentatools.service.EquipoService"%>
<% String tituloPagina = "Equipo"; String menuActivo = "equipos"; %>
<%@ include file="fragmentos/cabecera.jspf" %>
<%
    Equipo f = (Equipo) request.getAttribute("equipoForm");
    boolean editar = "editar".equals(request.getAttribute("modo"));
    @SuppressWarnings("unchecked")
    List<String> errores = (List<String>) request.getAttribute("errores");
    String csrf = (String) request.getAttribute("csrf");
    String precioTexto = (String) request.getAttribute("precioTexto");
    if (precioTexto == null) {
        precioTexto = f.getPrecioDia() == null ? "" : f.getPrecioDia().toPlainString();
    }
    boolean alquilado = "ALQUILADO".equals(f.getEstado());
%>

<div class="mb-4">
    <h1 class="h3 fw-bold mb-1"><%= editar ? "Editar equipo" : "Nuevo equipo" %></h1>
    <p class="text-secondary mb-0">Los campos con * son obligatorios.</p>
</div>

<% if (errores != null && !errores.isEmpty()) { %>
<div class="alert alert-danger" role="alert">
    <ul class="mb-0">
        <% for (String err : errores) { %><li><%= Html.esc(err) %></li><% } %>
    </ul>
</div>
<% } %>

<% if (alquilado) { %>
<div class="alert alert-info">Este equipo está alquilado: su estado cambia al registrar la devolución.</div>
<% } %>

<form method="post" action="<%= ctx %>/equipos" class="card p-4" novalidate>
    <input type="hidden" name="accion" value="<%= editar ? "actualizar" : "crear" %>">
    <input type="hidden" name="csrf" value="<%= Html.esc(csrf) %>">
    <% if (editar) { %><input type="hidden" name="id" value="<%= f.getId() %>"><% } %>

    <div class="row g-3">
        <div class="col-md-4">
            <label for="codigo" class="form-label fw-semibold">Código *</label>
            <input type="text" id="codigo" name="codigo" class="form-control text-uppercase" maxlength="20"
                   autocomplete="off" required value="<%= Html.esc(f.getCodigo()) %>">
            <div class="form-text">Por ejemplo TAL-001.</div>
        </div>
        <div class="col-md-8">
            <label for="nombre" class="form-label fw-semibold">Nombre *</label>
            <input type="text" id="nombre" name="nombre" class="form-control" maxlength="100" required
                   value="<%= Html.esc(f.getNombre()) %>">
        </div>
        <div class="col-md-4">
            <label for="categoria" class="form-label fw-semibold">Categoría *</label>
            <select id="categoria" name="categoria" class="form-select">
                <option value="">Seleccione…</option>
                <% for (String cat : EquipoService.CATEGORIAS) { %>
                <option value="<%= Html.esc(cat) %>" <%= cat.equals(f.getCategoria()) ? "selected" : "" %>><%= Html.esc(cat) %></option>
                <% } %>
            </select>
        </div>
        <div class="col-md-4">
            <label for="marca" class="form-label fw-semibold">Marca</label>
            <input type="text" id="marca" name="marca" class="form-control" maxlength="50"
                   value="<%= Html.esc(f.getMarca()) %>">
        </div>
        <div class="col-md-4">
            <label for="precioDia" class="form-label fw-semibold">Precio por día (S/) *</label>
            <input type="text" id="precioDia" name="precioDia" class="form-control" inputmode="decimal"
                   autocomplete="off" required value="<%= Html.esc(precioTexto) %>">
            <div class="form-text">Mayor que cero, con máximo 2 decimales.</div>
        </div>
        <div class="col-12">
            <label for="descripcion" class="form-label fw-semibold">Descripción</label>
            <textarea id="descripcion" name="descripcion" class="form-control" rows="2" maxlength="255"><%= Html.esc(f.getDescripcion()) %></textarea>
        </div>
        <div class="col-md-4">
            <label for="estado" class="form-label fw-semibold">Estado *</label>
            <select id="estado" name="estado" class="form-select">
                <option value="DISPONIBLE" <%= "DISPONIBLE".equals(f.getEstado()) ? "selected" : "" %>>Disponible</option>
                <option value="MANTENIMIENTO" <%= "MANTENIMIENTO".equals(f.getEstado()) ? "selected" : "" %>>En mantenimiento</option>
                <option value="INCIDENCIA" <%= "INCIDENCIA".equals(f.getEstado()) ? "selected" : "" %>>Con incidencia</option>
                <option value="BAJA" <%= "BAJA".equals(f.getEstado()) ? "selected" : "" %>>De baja</option>
                <% if (alquilado) { %><option value="ALQUILADO" selected>Alquilado</option><% } %>
            </select>
        </div>
    </div>

    <div class="mt-4 d-flex gap-2">
        <button type="submit" class="btn btn-naranja px-4"><%= editar ? "Guardar cambios" : "Registrar equipo" %></button>
        <a class="btn btn-outline-primary" href="<%= ctx %>/equipos">Cancelar</a>
    </div>
</form>

<%@ include file="fragmentos/pie.jspf" %>
