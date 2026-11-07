<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="java.util.List"%>
<%@page import="com.rentatools.model.Proveedor"%>
<% String tituloPagina = "Proveedores"; String menuActivo = "proveedores"; %>
<%@ include file="fragmentos/cabecera.jspf" %>
<%
    @SuppressWarnings("unchecked")
    List<Proveedor> proveedores = (List<Proveedor>) request.getAttribute("proveedores");
    String csrf = (String) request.getAttribute("csrf");
    String flashTipo = (String) request.getAttribute("flashTipo");
    String flashMensaje = (String) request.getAttribute("flashMensaje");
    int activos = 0;
    for (Proveedor x : proveedores) {
        if ("ACTIVO".equals(x.getEstado())) activos++;
    }
%>

<div class="d-flex flex-wrap justify-content-between align-items-center gap-3 mb-4">
    <div>
        <h1 class="h3 fw-bold mb-1">Proveedores</h1>
        <p class="text-secondary mb-0">Proveedores de equipos, repuestos, accesorios y servicios.</p>
    </div>
    <a class="btn btn-naranja px-4 py-2" href="<%= ctx %>/proveedores?accion=nuevo">+ Nuevo proveedor</a>
</div>

<% if (flashMensaje != null) { %>
<div class="alert alert-<%= Html.esc(flashTipo) %> alert-dismissible fade show" role="alert">
    <%= Html.esc(flashMensaje) %>
    <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Cerrar"></button>
</div>
<% } %>

<div class="row g-3 mb-4">
    <div class="col-12 col-md-4"><div class="card kpi p-3"><div class="titulo">Proveedores registrados</div><div class="valor"><%= proveedores.size() %></div></div></div>
    <div class="col-12 col-md-4"><div class="card kpi verde p-3"><div class="titulo">Activos</div><div class="valor"><%= activos %></div></div></div>
    <div class="col-12 col-md-4"><div class="card kpi naranja p-3"><div class="titulo">Inactivos</div><div class="valor"><%= proveedores.size() - activos %></div></div></div>
</div>

<section class="card p-4">
    <div class="d-flex flex-wrap justify-content-between align-items-center gap-3 mb-3">
        <h2 class="h5 fw-bold mb-0">Lista de proveedores</h2>
        <input type="text" id="buscar" class="form-control" style="max-width: 280px"
               placeholder="Buscar proveedor…" aria-label="Buscar proveedor">
    </div>

    <div class="table-responsive">
        <table class="table tabla align-middle" id="tablaProveedores">
            <thead>
                <tr>
                    <th>RUC</th><th>Razón social</th><th>Rubro</th><th>Teléfono</th><th>Correo</th><th>Estado</th>
                    <th class="text-end">Acciones</th>
                </tr>
            </thead>
            <tbody>
            <% for (Proveedor pv : proveedores) {
                   String claseEstado = "ACTIVO".equals(pv.getEstado()) ? "text-bg-success" : "text-bg-secondary";
            %>
                <tr>
                    <td class="fw-semibold text-nowrap"><%= Html.esc(pv.getRuc()) %></td>
                    <td><%= Html.esc(pv.getRazonSocial()) %>
                        <% if (!pv.getContacto().isEmpty()) { %><div class="text-secondary small">Contacto: <%= Html.esc(pv.getContacto()) %></div><% } %></td>
                    <td><%= Html.esc(pv.getRubro()) %></td>
                    <td class="text-nowrap"><%= Html.esc(pv.getTelefono()) %></td>
                    <td><%= Html.esc(pv.getCorreo()) %></td>
                    <td><span class="badge <%= claseEstado %>"><%= Html.esc(pv.getEstado()) %></span></td>
                    <td class="text-end text-nowrap">
                        <a class="btn btn-sm btn-outline-primary"
                           href="<%= ctx %>/proveedores?accion=editar&amp;id=<%= pv.getId() %>">Editar</a>
                        <button type="button" class="btn btn-sm btn-outline-danger"
                                data-bs-toggle="modal" data-bs-target="#modalEliminar"
                                data-id="<%= pv.getId() %>"
                                data-nombre="<%= Html.esc(pv.getRazonSocial()) %>">Eliminar</button>
                    </td>
                </tr>
            <% } %>
            </tbody>
        </table>
    </div>
    <% if (proveedores.isEmpty()) { %><p class="text-secondary mb-0">Aún no hay proveedores registrados.</p><% } %>
</section>

<div class="modal fade" id="modalEliminar" tabindex="-1" aria-labelledby="tituloEliminar" aria-hidden="true">
    <div class="modal-dialog modal-dialog-centered">
        <form method="post" action="<%= ctx %>/proveedores" class="modal-content">
            <input type="hidden" name="accion" value="eliminar">
            <input type="hidden" name="id" id="idEliminar">
            <input type="hidden" name="csrf" value="<%= Html.esc(csrf) %>">
            <div class="modal-header">
                <h2 class="modal-title h5" id="tituloEliminar">Eliminar proveedor</h2>
                <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Cerrar"></button>
            </div>
            <div class="modal-body">
                ¿Seguro que desea eliminar al proveedor <strong id="nombreEliminar"></strong>?
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
    document.getElementById('buscar').addEventListener('input', function () {
        var q = this.value.toLowerCase();
        document.querySelectorAll('#tablaProveedores tbody tr').forEach(function (tr) {
            tr.style.display = tr.textContent.toLowerCase().indexOf(q) > -1 ? '' : 'none';
        });
    });
    document.getElementById('modalEliminar').addEventListener('show.bs.modal', function (ev) {
        var b = ev.relatedTarget;
        document.getElementById('idEliminar').value = b.getAttribute('data-id');
        document.getElementById('nombreEliminar').textContent = b.getAttribute('data-nombre');
    });
</script>

<%@ include file="fragmentos/pie.jspf" %>
