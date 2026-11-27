<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="java.util.List"%>
<%@page import="com.rentatools.model.Equipo"%>
<%@page import="com.rentatools.service.EquipoService"%>
<% String tituloPagina = "Equipos"; String menuActivo = "equipos"; %>
<%@ include file="fragmentos/cabecera.jspf" %>
<%
    @SuppressWarnings("unchecked")
    List<Equipo> equipos = (List<Equipo>) request.getAttribute("equipos");
    String csrf = (String) request.getAttribute("csrf");
    String flashTipo = (String) request.getAttribute("flashTipo");
    String flashMensaje = (String) request.getAttribute("flashMensaje");
    @SuppressWarnings("unchecked")
    java.util.Map<String, Integer> conteos = (java.util.Map<String, Integer>) request.getAttribute("conteos");
    int total = 0;
    for (int n : conteos.values()) total += n;
    String busqueda = ((com.rentatools.model.Pagina<?>) request.getAttribute("pag")).getQ();
    int disponibles = conteos.getOrDefault("DISPONIBLE", 0);
    int alquilados = conteos.getOrDefault("ALQUILADO", 0);
    int enRevision = conteos.getOrDefault("MANTENIMIENTO", 0) + conteos.getOrDefault("INCIDENCIA", 0);
%>

<div class="d-flex flex-wrap justify-content-between align-items-center gap-3 mb-4">
    <div>
        <h1 class="h3 fw-bold mb-1">Equipos</h1>
        <p class="text-secondary mb-0">Inventario de equipos y herramientas disponibles para alquiler.</p>
    </div>
    <% if (esAdmin) { %>
    <a class="btn btn-naranja px-4 py-2" href="<%= ctx %>/equipos?accion=nuevo">+ Nuevo equipo</a>
    <% } %>
</div>

<% if (flashMensaje != null) { %>
<div class="alert alert-<%= Html.esc(flashTipo) %> alert-dismissible fade show" role="alert">
    <%= Html.esc(flashMensaje) %>
    <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Cerrar"></button>
</div>
<% } %>

<div class="row g-3 mb-4">
    <div class="col-12 col-md-6 col-xl-3"><div class="card kpi p-3"><div class="titulo">Total de equipos</div><div class="valor"><%= total %></div></div></div>
    <div class="col-12 col-md-6 col-xl-3"><div class="card kpi verde p-3"><div class="titulo">Disponibles</div><div class="valor"><%= disponibles %></div></div></div>
    <div class="col-12 col-md-6 col-xl-3"><div class="card kpi p-3"><div class="titulo">Alquilados</div><div class="valor"><%= alquilados %></div></div></div>
    <div class="col-12 col-md-6 col-xl-3"><div class="card kpi naranja p-3"><div class="titulo">En mantenimiento o con incidencia</div><div class="valor"><%= enRevision %></div></div></div>
</div>

<section class="card p-4">
    <div class="d-flex flex-wrap justify-content-between align-items-center gap-3 mb-3">
        <h2 class="h5 fw-bold mb-0">Lista de equipos</h2>
        <form method="get" action="<%= ctx %>/equipos" class="d-flex gap-2">
            <input type="text" name="q" value="<%= Html.esc(busqueda) %>" class="form-control" style="max-width: 280px"
                   placeholder="Buscar equipo…" aria-label="Buscar equipo">
            <button class="btn btn-outline-primary" type="submit">Buscar</button>
            <% if (!busqueda.isEmpty()) { %><a class="btn btn-link" href="<%= ctx %>/equipos">Limpiar</a><% } %>
        </form>
    </div>

    <div class="table-responsive">
        <table class="table tabla align-middle" id="tablaEquipos">
            <thead>
                <tr>
                    <th>Código</th><th>Equipo</th><th>Categoría</th><th>Precio por día</th><th>Estado</th>
                    <% if (esAdmin) { %><th class="text-end">Acciones</th><% } %>
                </tr>
            </thead>
            <tbody>
            <% for (Equipo eq : equipos) {
                   String st = eq.getEstado();
                   String claseEstado = "DISPONIBLE".equals(st) ? "text-bg-success"
                           : "ALQUILADO".equals(st) ? "text-bg-primary"
                           : "MANTENIMIENTO".equals(st) ? "text-bg-warning"
                           : "INCIDENCIA".equals(st) ? "text-bg-danger" : "text-bg-secondary";
            %>
                <tr>
                    <td class="fw-semibold text-nowrap"><%= Html.esc(eq.getCodigo()) %></td>
                    <td><%= Html.esc(eq.getNombre()) %>
                        <% if (!eq.getMarca().isEmpty()) { %><div class="text-secondary small"><%= Html.esc(eq.getMarca()) %></div><% } %></td>
                    <td><%= Html.esc(eq.getCategoria()) %></td>
                    <td class="text-nowrap">S/ <%= eq.getPrecioDia().toPlainString() %></td>
                    <td><span class="badge <%= claseEstado %>"><%= Html.esc(EquipoService.etiquetaEstado(st)) %></span></td>
                    <% if (esAdmin) { %>
                    <td class="text-end text-nowrap">
                        <a class="btn btn-sm btn-outline-primary"
                           href="<%= ctx %>/equipos?accion=editar&amp;id=<%= eq.getId() %>">Editar</a>
                        <button type="button" class="btn btn-sm btn-outline-danger"
                                data-bs-toggle="modal" data-bs-target="#modalEliminar"
                                data-id="<%= eq.getId() %>"
                                data-nombre="<%= Html.esc(eq.getCodigo() + " - " + eq.getNombre()) %>">Eliminar</button>
                    </td>
                    <% } %>
                </tr>
            <% } %>
            </tbody>
        </table>
    </div>
    <% if (equipos.isEmpty()) { %><p class="text-secondary mb-0"><%= busqueda.isEmpty() ? "Aún no hay equipos registrados." : "No se encontraron resultados para la búsqueda." %></p><% } %>
    <%@ include file="fragmentos/paginacion.jspf" %>
</section>

<% if (esAdmin) { %>
<div class="modal fade" id="modalEliminar" tabindex="-1" aria-labelledby="tituloEliminar" aria-hidden="true">
    <div class="modal-dialog modal-dialog-centered">
        <form method="post" action="<%= ctx %>/equipos" class="modal-content">
            <input type="hidden" name="accion" value="eliminar">
            <input type="hidden" name="id" id="idEliminar">
            <input type="hidden" name="csrf" value="<%= Html.esc(csrf) %>">
            <div class="modal-header">
                <h2 class="modal-title h5" id="tituloEliminar">Eliminar equipo</h2>
                <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Cerrar"></button>
            </div>
            <div class="modal-body">
                ¿Seguro que desea eliminar el equipo <strong id="nombreEliminar"></strong>?
                Esta acción no se puede deshacer.
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-outline-primary" data-bs-dismiss="modal">Cancelar</button>
                <button type="submit" class="btn btn-danger">Eliminar</button>
            </div>
        </form>
    </div>
</div>
<% } %>

<script>
    var modal = document.getElementById('modalEliminar');
    if (modal) {
        modal.addEventListener('show.bs.modal', function (ev) {
            var b = ev.relatedTarget;
            document.getElementById('idEliminar').value = b.getAttribute('data-id');
            document.getElementById('nombreEliminar').textContent = b.getAttribute('data-nombre');
        });
    }
</script>

<%@ include file="fragmentos/pie.jspf" %>
