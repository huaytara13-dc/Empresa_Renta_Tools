<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="java.time.LocalDate"%>
<%@page import="java.util.List"%>
<%@page import="com.rentatools.model.Alquiler"%>
<%@page import="com.rentatools.service.AlquilerService"%>
<% String tituloPagina = "Inicio"; String menuActivo = "inicio"; %>
<%@ include file="fragmentos/cabecera.jspf" %>
<%
    @SuppressWarnings("unchecked")
    List<Alquiler> alertas = (List<Alquiler>) request.getAttribute("alertas");
    LocalDate hoy = (LocalDate) request.getAttribute("hoy");
    int totalAlertas = (Integer) request.getAttribute("totalAlertas");
%>

<div class="banner mb-4">
    <h1 class="h3 fw-bold mb-1">Hola, <%= Html.esc(actual.getNombres()) %></h1>
    <p class="mb-0 opacity-75">Este es el resumen del sistema de gestión de alquileres de RentaTools.</p>
</div>

<div class="row g-3 mb-3">
    <div class="col-12 col-md-6 col-xl-3">
        <div class="card kpi verde p-3"><div class="titulo">Equipos disponibles</div>
            <div class="valor"><%= request.getAttribute("equiposDisponibles") %>
                <small class="fs-6 fw-normal text-secondary">de <%= request.getAttribute("totalEquipos") %></small></div></div>
    </div>
    <div class="col-12 col-md-6 col-xl-3">
        <div class="card kpi p-3"><div class="titulo">Alquileres activos</div>
            <div class="valor"><%= request.getAttribute("alquileresActivos") %></div></div>
    </div>
    <div class="col-12 col-md-6 col-xl-3">
        <div class="card kpi naranja p-3"><div class="titulo">Por vencer (<%= AlquilerService.DIAS_AVISO %> días o menos)</div>
            <div class="valor"><%= request.getAttribute("alquileresPorVencer") %></div></div>
    </div>
    <div class="col-12 col-md-6 col-xl-3">
        <div class="card kpi p-3" style="border-top-color: #a32a12"><div class="titulo">Devoluciones vencidas</div>
            <div class="valor"><%= request.getAttribute("alquileresVencidos") %></div></div>
    </div>
</div>

<div class="row g-3 mb-4">
    <div class="col-12 col-md-6 col-xl-3">
        <div class="card kpi p-3"><div class="titulo">Clientes registrados</div>
            <div class="valor"><%= request.getAttribute("totalClientes") %></div></div>
    </div>
    <% if (esAdmin) { %>
    <div class="col-12 col-md-6 col-xl-3">
        <div class="card kpi p-3"><div class="titulo">Usuarios registrados</div>
            <div class="valor"><%= request.getAttribute("totalUsuarios") %></div></div>
    </div>
    <div class="col-12 col-md-6 col-xl-3">
        <div class="card kpi verde p-3"><div class="titulo">Usuarios activos</div>
            <div class="valor"><%= request.getAttribute("usuariosActivos") %></div></div>
    </div>
    <div class="col-12 col-md-6 col-xl-3">
        <div class="card kpi naranja p-3"><div class="titulo">Usuarios bloqueados</div>
            <div class="valor"><%= request.getAttribute("usuariosBloqueados") %></div></div>
    </div>
    <% } %>
</div>

<div class="row g-3">
    <div class="col-12 col-xl-7">
        <section class="card p-4 h-100">
            <h2 class="h5 fw-bold mb-3">Alertas de devolución</h2>
            <% if (alertas.isEmpty()) { %>
            <p class="text-secondary mb-0">No hay alquileres vencidos ni por vencer.</p>
            <% } else { %>
            <ul class="list-group list-group-flush">
                <% for (Alquiler al : alertas) { %>
                <li class="list-group-item d-flex flex-wrap justify-content-between align-items-center gap-2 px-0">
                    <span><a class="fw-semibold text-decoration-none"
                             href="<%= ctx %>/alquileres?accion=ver&amp;id=<%= al.getId() %>">N.º <%= al.getId() %></a>
                        · <%= Html.esc(al.getClienteNombre()) %>
                        <span class="text-secondary small">(<%= al.getCantidadEquipos() %> equipo<%= al.getCantidadEquipos() == 1 ? "" : "s" %>)</span></span>
                    <span class="badge <%= AlquilerService.clasePlazo(al, hoy) %>"><%= Html.esc(AlquilerService.etiquetaPlazo(al, hoy)) %></span>
                </li>
                <% } %>
            </ul>
            <% if (totalAlertas > alertas.size()) { %>
            <a class="btn btn-sm btn-outline-primary mt-3 align-self-start" href="<%= ctx %>/alquileres">Ver todos los alquileres</a>
            <% } %>
            <% } %>
        </section>
    </div>
    <div class="col-12 col-xl-5">
        <section class="card p-4 h-100">
            <h2 class="h5 fw-bold mb-3">Accesos rápidos</h2>
            <div class="d-flex flex-wrap gap-2">
                <a class="btn btn-naranja" href="<%= ctx %>/alquileres?accion=nuevo">+ Nuevo alquiler</a>
                <a class="btn btn-outline-primary" href="<%= ctx %>/alquileres">Ver alquileres</a>
                <a class="btn btn-outline-primary" href="<%= ctx %>/devoluciones?accion=nueva">Registrar devolución</a>
                <a class="btn btn-outline-primary" href="<%= ctx %>/clientes?accion=nuevo">+ Nuevo cliente</a>
                <a class="btn btn-outline-primary" href="<%= ctx %>/equipos">Consultar equipos</a>
                <% if (esAdmin) { %>
                <a class="btn btn-outline-primary" href="<%= ctx %>/equipos?accion=nuevo">+ Nuevo equipo</a>
                <a class="btn btn-outline-primary" href="<%= ctx %>/usuarios">Gestionar usuarios</a>
                <% } %>
            </div>
            <p class="text-secondary small mt-3 mb-0">
                Los reportes estarán disponibles en las siguientes etapas.
            </p>
        </section>
    </div>
</div>

<%@ include file="fragmentos/pie.jspf" %>
