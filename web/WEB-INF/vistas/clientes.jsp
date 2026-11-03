<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="java.util.List"%>
<%@page import="com.rentatools.model.Cliente"%>
<% String tituloPagina = "Clientes"; String menuActivo = "clientes"; %>
<%@ include file="fragmentos/cabecera.jspf" %>
<%
    @SuppressWarnings("unchecked")
    List<Cliente> clientes = (List<Cliente>) request.getAttribute("clientes");
    String csrf = (String) request.getAttribute("csrf");
    String flashTipo = (String) request.getAttribute("flashTipo");
    String flashMensaje = (String) request.getAttribute("flashMensaje");
    int activos = 0;
    for (Cliente x : clientes) {
        if ("ACTIVO".equals(x.getEstado())) activos++;
    }
%>

<div class="d-flex flex-wrap justify-content-between align-items-center gap-3 mb-4">
    <div>
        <h1 class="h3 fw-bold mb-1">Clientes</h1>
        <p class="text-secondary mb-0">Registra y mantiene los datos de los clientes de RentaTools.</p>
    </div>
    <a class="btn btn-naranja px-4 py-2" href="<%= ctx %>/clientes?accion=nuevo">+ Nuevo cliente</a>
</div>

<% if (flashMensaje != null) { %>
<div class="alert alert-<%= Html.esc(flashTipo) %> alert-dismissible fade show" role="alert">
    <%= Html.esc(flashMensaje) %>
    <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Cerrar"></button>
</div>
<% } %>

<div class="row g-3 mb-4">
    <div class="col-12 col-md-4"><div class="card kpi p-3"><div class="titulo">Clientes registrados</div><div class="valor"><%= clientes.size() %></div></div></div>
    <div class="col-12 col-md-4"><div class="card kpi verde p-3"><div class="titulo">Activos</div><div class="valor"><%= activos %></div></div></div>
    <div class="col-12 col-md-4"><div class="card kpi naranja p-3"><div class="titulo">Inactivos</div><div class="valor"><%= clientes.size() - activos %></div></div></div>
</div>

<section class="card p-4">
    <div class="d-flex flex-wrap justify-content-between align-items-center gap-3 mb-3">
        <h2 class="h5 fw-bold mb-0">Lista de clientes</h2>
        <input type="text" id="buscar" class="form-control" style="max-width: 280px"
               placeholder="Buscar cliente…" aria-label="Buscar cliente">
    </div>

    <div class="table-responsive">
        <table class="table tabla align-middle" id="tablaClientes">
            <thead>
                <tr>
                    <th>Documento</th><th>Cliente</th><th>Teléfono</th><th>Correo</th><th>Estado</th>
                    <th class="text-end">Acciones</th>
                </tr>
            </thead>
            <tbody>
            <% for (Cliente c : clientes) {
                   String claseEstado = "ACTIVO".equals(c.getEstado()) ? "text-bg-success" : "text-bg-secondary";
            %>
                <tr>
                    <td class="text-nowrap"><span class="badge text-bg-light border"><%= Html.esc(c.getTipoDocumento()) %></span>
                        <%= Html.esc(c.getNumeroDocumento()) %></td>
                    <td class="fw-semibold"><%= Html.esc((c.getNombres() + " " + c.getApellidos()).trim()) %></td>
                    <td><%= Html.esc(c.getTelefono()) %></td>
                    <td><%= Html.esc(c.getCorreo()) %></td>
                    <td><span class="badge <%= claseEstado %>"><%= Html.esc(c.getEstado()) %></span></td>
                    <td class="text-end text-nowrap">
                        <a class="btn btn-sm btn-outline-primary"
                           href="<%= ctx %>/clientes?accion=editar&amp;id=<%= c.getId() %>">Editar</a>
                        <% if (esAdmin) { %>
                        <button type="button" class="btn btn-sm btn-outline-danger"
                                data-bs-toggle="modal" data-bs-target="#modalEliminar"
                                data-id="<%= c.getId() %>"
                                data-nombre="<%= Html.esc((c.getNombres() + " " + c.getApellidos()).trim()) %>">Eliminar</button>
                        <% } %>
                    </td>
                </tr>
            <% } %>
            </tbody>
        </table>
    </div>
    <% if (clientes.isEmpty()) { %><p class="text-secondary mb-0">Aún no hay clientes registrados.</p><% } %>
</section>

<% if (esAdmin) { %>
<!-- Confirmacion de eliminacion (solo administrador) -->
<div class="modal fade" id="modalEliminar" tabindex="-1" aria-labelledby="tituloEliminar" aria-hidden="true">
    <div class="modal-dialog modal-dialog-centered">
        <form method="post" action="<%= ctx %>/clientes" class="modal-content">
            <input type="hidden" name="accion" value="eliminar">
            <input type="hidden" name="id" id="idEliminar">
            <input type="hidden" name="csrf" value="<%= Html.esc(csrf) %>">
            <div class="modal-header">
                <h2 class="modal-title h5" id="tituloEliminar">Eliminar cliente</h2>
                <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Cerrar"></button>
            </div>
            <div class="modal-body">
                ¿Seguro que desea eliminar al cliente <strong id="nombreEliminar"></strong>?
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
    document.getElementById('buscar').addEventListener('input', function () {
        var q = this.value.toLowerCase();
        document.querySelectorAll('#tablaClientes tbody tr').forEach(function (tr) {
            tr.style.display = tr.textContent.toLowerCase().indexOf(q) > -1 ? '' : 'none';
        });
    });
    var modal = document.getElementById('modalEliminar');
    if (modal) {
        modal.addEventListener('show.bs.modal', function (e) {
            var b = e.relatedTarget;
            document.getElementById('idEliminar').value = b.getAttribute('data-id');
            document.getElementById('nombreEliminar').textContent = b.getAttribute('data-nombre');
        });
    }
</script>

<%@ include file="fragmentos/pie.jspf" %>
