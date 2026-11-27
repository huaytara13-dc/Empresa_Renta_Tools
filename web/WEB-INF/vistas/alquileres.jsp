<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="java.time.LocalDate"%>
<%@page import="java.util.List"%>
<%@page import="com.rentatools.model.Alquiler"%>
<%@page import="com.rentatools.service.AlquilerService"%>
<% String tituloPagina = "Alquileres"; String menuActivo = "alquileres"; %>
<%@ include file="fragmentos/cabecera.jspf" %>
<%
    @SuppressWarnings("unchecked")
    List<Alquiler> alquileres = (List<Alquiler>) request.getAttribute("alquileres");
    LocalDate hoy = (LocalDate) request.getAttribute("hoy");
    String flashTipo = (String) request.getAttribute("flashTipo");
    String flashMensaje = (String) request.getAttribute("flashMensaje");
    String busqueda = ((com.rentatools.model.Pagina<?>) request.getAttribute("pag")).getQ();
    int activos = (Integer) request.getAttribute("kActivos");
    int porVencer = (Integer) request.getAttribute("kPorVencer");
    int vencidos = (Integer) request.getAttribute("kVencidos");
    int finalizados = (Integer) request.getAttribute("kFinalizados");
%>

<div class="d-flex flex-wrap justify-content-between align-items-center gap-3 mb-4">
    <div>
        <h1 class="h3 fw-bold mb-1">Alquileres</h1>
        <p class="text-secondary mb-0">Registro y seguimiento de los alquileres de equipos.</p>
    </div>
    <a class="btn btn-naranja px-4 py-2" href="<%= ctx %>/alquileres?accion=nuevo">+ Nuevo alquiler</a>
</div>

<% if (flashMensaje != null) { %>
<div class="alert alert-<%= Html.esc(flashTipo) %> alert-dismissible fade show" role="alert">
    <%= Html.esc(flashMensaje) %>
    <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Cerrar"></button>
</div>
<% } %>

<div class="row g-3 mb-4">
    <div class="col-12 col-md-6 col-xl-3"><div class="card kpi p-3"><div class="titulo">Activos</div><div class="valor"><%= activos %></div></div></div>
    <div class="col-12 col-md-6 col-xl-3"><div class="card kpi naranja p-3"><div class="titulo">Por vencer</div><div class="valor"><%= porVencer %></div></div></div>
    <div class="col-12 col-md-6 col-xl-3"><div class="card kpi p-3" style="border-top-color: #a32a12"><div class="titulo">Vencidos</div><div class="valor"><%= vencidos %></div></div></div>
    <div class="col-12 col-md-6 col-xl-3"><div class="card kpi verde p-3"><div class="titulo">Finalizados</div><div class="valor"><%= finalizados %></div></div></div>
</div>

<section class="card p-4">
    <div class="d-flex flex-wrap justify-content-between align-items-center gap-3 mb-3">
        <h2 class="h5 fw-bold mb-0">Lista de alquileres</h2>
        <form method="get" action="<%= ctx %>/alquileres" class="d-flex gap-2">
            <input type="text" name="q" value="<%= Html.esc(busqueda) %>" class="form-control" style="max-width: 280px"
                   placeholder="Buscar alquiler…" aria-label="Buscar alquiler">
            <button class="btn btn-outline-primary" type="submit">Buscar</button>
            <% if (!busqueda.isEmpty()) { %><a class="btn btn-link" href="<%= ctx %>/alquileres">Limpiar</a><% } %>
        </form>
    </div>

    <div class="table-responsive">
        <table class="table tabla align-middle" id="tablaAlquileres">
            <thead>
                <tr>
                    <th>N.º</th><th>Cliente</th><th>Equipos</th><th>Inicio</th><th>Devolución prevista</th>
                    <th>Total</th><th>Estado</th><th class="text-end">Acciones</th>
                </tr>
            </thead>
            <tbody>
            <% for (Alquiler al : alquileres) {
                   String st = al.getEstado();
                   boolean activo = "ACTIVO".equals(st);
                   String claseEstado = activo ? "text-bg-primary" : "FINALIZADO".equals(st) ? "text-bg-success" : "text-bg-secondary";
                   String etiquetaEstado = activo ? "Activo" : "FINALIZADO".equals(st) ? "Finalizado" : "Anulado";
            %>
                <tr>
                    <td class="fw-semibold"><%= al.getId() %></td>
                    <td><%= Html.esc(al.getClienteNombre()) %>
                        <div class="text-secondary small"><%= Html.esc(al.getClienteDocumento()) %></div></td>
                    <td><%= al.getCantidadEquipos() %></td>
                    <td class="text-nowrap"><%= al.getFechaInicio() %></td>
                    <td class="text-nowrap"><%= al.getFechaDevolucionPrevista() %>
                        <% if (activo) { %><div><span class="badge <%= AlquilerService.clasePlazo(al, hoy) %>"><%= Html.esc(AlquilerService.etiquetaPlazo(al, hoy)) %></span></div><% } %></td>
                    <td class="text-nowrap">S/ <%= al.getTotal().toPlainString() %></td>
                    <td><span class="badge <%= claseEstado %>"><%= etiquetaEstado %></span></td>
                    <td class="text-end text-nowrap">
                        <a class="btn btn-sm btn-outline-primary"
                           href="<%= ctx %>/alquileres?accion=ver&amp;id=<%= al.getId() %>">Ver</a>
                    </td>
                </tr>
            <% } %>
            </tbody>
        </table>
    </div>
    <% if (alquileres.isEmpty()) { %><p class="text-secondary mb-0"><%= busqueda.isEmpty() ? "Aún no hay alquileres registrados." : "No se encontraron resultados para la búsqueda." %></p><% } %>
    <%@ include file="fragmentos/paginacion.jspf" %>
</section>

<script>
</script>

<%@ include file="fragmentos/pie.jspf" %>
