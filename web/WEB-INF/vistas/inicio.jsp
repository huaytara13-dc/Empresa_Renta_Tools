<%@page contentType="text/html" pageEncoding="UTF-8"%>
<% String tituloPagina = "Inicio"; String menuActivo = "inicio"; %>
<%@ include file="fragmentos/cabecera.jspf" %>

<div class="banner mb-4">
    <h1 class="h3 fw-bold mb-1">Hola, <%= Html.esc(actual.getNombres()) %></h1>
    <p class="mb-0 opacity-75">Este es el resumen del sistema de gestión de alquileres de RentaTools.</p>
</div>

<% if (esAdmin) { %>
<div class="row g-3 mb-4">
    <div class="col-12 col-md-4">
        <div class="card kpi p-3"><div class="titulo">Usuarios registrados</div>
            <div class="valor"><%= request.getAttribute("totalUsuarios") %></div></div>
    </div>
    <div class="col-12 col-md-4">
        <div class="card kpi verde p-3"><div class="titulo">Usuarios activos</div>
            <div class="valor"><%= request.getAttribute("usuariosActivos") %></div></div>
    </div>
    <div class="col-12 col-md-4">
        <div class="card kpi naranja p-3"><div class="titulo">Usuarios bloqueados</div>
            <div class="valor"><%= request.getAttribute("usuariosBloqueados") %></div></div>
    </div>
</div>

<div class="card p-4 mb-4">
    <h2 class="h5 fw-bold mb-3">Accesos rápidos</h2>
    <div>
        <a class="btn btn-naranja me-2 mb-2" href="<%= ctx %>/usuarios?accion=nuevo">+ Nuevo usuario</a>
        <a class="btn btn-outline-primary mb-2" href="<%= ctx %>/usuarios">Gestionar usuarios</a>
    </div>
</div>
<% } %>

<div class="card p-4">
    <h2 class="h5 fw-bold mb-2">Próximos módulos</h2>
    <p class="text-secondary mb-0">
        Clientes, equipos, alquileres, devoluciones<%= esAdmin ? " y reportes" : "" %> estarán disponibles
        en las siguientes etapas del proyecto. Aquí se mostrarán las alertas de devoluciones por vencer.
    </p>
</div>

<%@ include file="fragmentos/pie.jspf" %>
