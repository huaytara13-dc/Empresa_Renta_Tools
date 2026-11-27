<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="java.util.List"%>
<% String tituloPagina = "Gestión de usuarios"; String menuActivo = "usuarios"; %>
<%@ include file="fragmentos/cabecera.jspf" %>
<%
    @SuppressWarnings("unchecked")
    List<Usuario> usuarios = (List<Usuario>) request.getAttribute("usuarios");
    String csrf = (String) request.getAttribute("csrf");
    String flashTipo = (String) request.getAttribute("flashTipo");
    String flashMensaje = (String) request.getAttribute("flashMensaje");
    @SuppressWarnings("unchecked")
    java.util.Map<String, Integer> conteos = (java.util.Map<String, Integer>) request.getAttribute("conteos");
    int total = 0;
    for (int n : conteos.values()) total += n;
    String busqueda = ((com.rentatools.model.Pagina<?>) request.getAttribute("pag")).getQ();
    int activos = conteos.getOrDefault("ACTIVO", 0);
    int bloqueados = conteos.getOrDefault("BLOQUEADO", 0);
%>

<div class="d-flex flex-wrap justify-content-between align-items-center gap-3 mb-4">
    <div>
        <h1 class="h3 fw-bold mb-1">Gestión de usuarios</h1>
        <p class="text-secondary mb-0">Crea, edita y elimina las cuentas de acceso al sistema.</p>
    </div>
    <a class="btn btn-naranja px-4 py-2" href="<%= ctx %>/usuarios?accion=nuevo">+ Nuevo usuario</a>
</div>

<% if (flashMensaje != null) { %>
<div class="alert alert-<%= Html.esc(flashTipo) %> alert-dismissible fade show" role="alert">
    <%= Html.esc(flashMensaje) %>
    <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Cerrar"></button>
</div>
<% } %>

<div class="row g-3 mb-4">
    <div class="col-12 col-md-4"><div class="card kpi p-3"><div class="titulo">Usuarios registrados</div><div class="valor"><%= total %></div></div></div>
    <div class="col-12 col-md-4"><div class="card kpi verde p-3"><div class="titulo">Activos</div><div class="valor"><%= activos %></div></div></div>
    <div class="col-12 col-md-4"><div class="card kpi naranja p-3"><div class="titulo">Bloqueados</div><div class="valor"><%= bloqueados %></div></div></div>
</div>

<section class="card p-4">
    <div class="d-flex flex-wrap justify-content-between align-items-center gap-3 mb-3">
        <h2 class="h5 fw-bold mb-0">Lista de usuarios</h2>
        <form method="get" action="<%= ctx %>/usuarios" class="d-flex gap-2">
            <input type="text" name="q" value="<%= Html.esc(busqueda) %>" class="form-control" style="max-width: 280px"
                   placeholder="Buscar usuario…" aria-label="Buscar usuario">
            <button class="btn btn-outline-primary" type="submit">Buscar</button>
            <% if (!busqueda.isEmpty()) { %><a class="btn btn-link" href="<%= ctx %>/usuarios">Limpiar</a><% } %>
        </form>
    </div>

    <div class="table-responsive">
        <table class="table tabla align-middle" id="tablaUsuarios">
            <thead>
                <tr>
                    <th>Usuario</th><th>Nombres</th><th>Correo</th><th>Rol</th><th>Estado</th>
                    <th class="text-end">Acciones</th>
                </tr>
            </thead>
            <tbody>
            <% for (Usuario u : usuarios) {
                   boolean yo = u.getId() == actual.getId();
                   String estado = u.getEstado();
                   String claseEstado = "ACTIVO".equals(estado) ? "text-bg-success"
                           : "BLOQUEADO".equals(estado) ? "text-bg-danger" : "text-bg-secondary";
                   String claseRol = "ADMIN".equals(u.getRol()) ? "text-bg-primary" : "text-bg-light border";
            %>
                <tr>
                    <td class="fw-semibold"><%= Html.esc(u.getUsuario()) %>
                        <% if (yo) { %><span class="badge text-bg-light border">Tú</span><% } %></td>
                    <td><%= Html.esc(u.getNombres() + " " + u.getApellidos()) %></td>
                    <td><%= Html.esc(u.getCorreo()) %></td>
                    <td><span class="badge <%= claseRol %>"><%= Html.esc(u.getRol()) %></span></td>
                    <td><span class="badge <%= claseEstado %>"><%= Html.esc(estado) %></span></td>
                    <td class="text-end text-nowrap">
                        <a class="btn btn-sm btn-outline-primary"
                           href="<%= ctx %>/usuarios?accion=editar&amp;id=<%= u.getId() %>">Editar</a>
                        <% if ("BLOQUEADO".equals(estado)) { %>
                        <form method="post" action="<%= ctx %>/usuarios" class="d-inline">
                            <input type="hidden" name="accion" value="desbloquear">
                            <input type="hidden" name="id" value="<%= u.getId() %>">
                            <input type="hidden" name="csrf" value="<%= Html.esc(csrf) %>">
                            <button type="submit" class="btn btn-sm btn-outline-warning">Desbloquear</button>
                        </form>
                        <% } %>
                        <button type="button" class="btn btn-sm btn-outline-danger" <%= yo ? "disabled" : "" %>
                                data-bs-toggle="modal" data-bs-target="#modalEliminar"
                                data-id="<%= u.getId() %>" data-nombre="<%= Html.esc(u.getUsuario()) %>">Eliminar</button>
                    </td>
                </tr>
            <% } %>
            </tbody>
        </table>
    </div>
    <% if (usuarios.isEmpty()) { %><p class="text-secondary mb-0"><%= busqueda.isEmpty() ? "No hay usuarios registrados." : "No se encontraron resultados para la búsqueda." %></p><% } %>
    <%@ include file="fragmentos/paginacion.jspf" %>
</section>

<!-- Confirmacion de eliminacion -->
<div class="modal fade" id="modalEliminar" tabindex="-1" aria-labelledby="tituloEliminar" aria-hidden="true">
    <div class="modal-dialog modal-dialog-centered">
        <form method="post" action="<%= ctx %>/usuarios" class="modal-content">
            <input type="hidden" name="accion" value="eliminar">
            <input type="hidden" name="id" id="idEliminar">
            <input type="hidden" name="csrf" value="<%= Html.esc(csrf) %>">
            <div class="modal-header">
                <h2 class="modal-title h5" id="tituloEliminar">Eliminar usuario</h2>
                <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Cerrar"></button>
            </div>
            <div class="modal-body">
                ¿Seguro que desea eliminar al usuario <strong id="nombreEliminar"></strong>?
                Esta acción no se puede deshacer.
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-outline-primary" data-bs-dismiss="modal">Cancelar</button>
                <button type="submit" class="btn btn-danger">Eliminar</button>
            </div>
        </form>
    </div>
</div>

<script>
    document.getElementById('modalEliminar').addEventListener('show.bs.modal', function (e) {
        var b = e.relatedTarget;
        document.getElementById('idEliminar').value = b.getAttribute('data-id');
        document.getElementById('nombreEliminar').textContent = b.getAttribute('data-nombre');
    });
</script>

<%@ include file="fragmentos/pie.jspf" %>
