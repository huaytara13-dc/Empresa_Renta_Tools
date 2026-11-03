<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="java.time.LocalDate"%>
<%@page import="java.util.List"%>
<%@page import="java.util.Set"%>
<%@page import="com.rentatools.model.Cliente"%>
<%@page import="com.rentatools.model.Equipo"%>
<%@page import="com.rentatools.service.AlquilerService"%>
<% String tituloPagina = "Nuevo alquiler"; String menuActivo = "alquileres"; %>
<%@ include file="fragmentos/cabecera.jspf" %>
<%
    @SuppressWarnings("unchecked")
    List<Cliente> clientes = (List<Cliente>) request.getAttribute("clientes");
    @SuppressWarnings("unchecked")
    List<Equipo> equipos = (List<Equipo>) request.getAttribute("equipos");
    @SuppressWarnings("unchecked")
    List<String> errores = (List<String>) request.getAttribute("errores");
    @SuppressWarnings("unchecked")
    Set<Integer> marcados = (Set<Integer>) request.getAttribute("formEquipoIds");
    int formClienteId = (Integer) request.getAttribute("formClienteId");
    String formFecha = (String) request.getAttribute("formFecha");
    String formObs = (String) request.getAttribute("formObservaciones");
    LocalDate hoy = (LocalDate) request.getAttribute("hoy");
    String csrf = (String) request.getAttribute("csrf");
%>

<div class="mb-4">
    <h1 class="h3 fw-bold mb-1">Nuevo alquiler</h1>
    <p class="text-secondary mb-0">El alquiler inicia hoy (<%= hoy %>). Los equipos elegidos quedan como alquilados.</p>
</div>

<% if (errores != null && !errores.isEmpty()) { %>
<div class="alert alert-danger" role="alert">
    <ul class="mb-0">
        <% for (String err : errores) { %><li><%= Html.esc(err) %></li><% } %>
    </ul>
</div>
<% } %>

<form method="post" action="<%= ctx %>/alquileres" novalidate>
    <input type="hidden" name="accion" value="registrar">
    <input type="hidden" name="csrf" value="<%= Html.esc(csrf) %>">

    <div class="row g-3">
        <div class="col-12 col-xl-8">
            <section class="card p-4 mb-3">
                <h2 class="h5 fw-bold mb-3">Cliente y plazo</h2>
                <div class="row g-3">
                    <div class="col-md-7">
                        <label for="clienteId" class="form-label fw-semibold">Cliente *</label>
                        <select id="clienteId" name="clienteId" class="form-select">
                            <option value="">Seleccione…</option>
                            <% for (Cliente c : clientes) { %>
                            <option value="<%= c.getId() %>" <%= c.getId() == formClienteId ? "selected" : "" %>>
                                <%= Html.esc((c.getNombres() + " " + c.getApellidos()).trim()) %> · <%= Html.esc(c.getTipoDocumento()) %> <%= Html.esc(c.getNumeroDocumento()) %>
                            </option>
                            <% } %>
                        </select>
                        <% if (clientes.isEmpty()) { %>
                        <div class="form-text text-danger">No hay clientes activos. Registre uno primero.</div>
                        <% } %>
                    </div>
                    <div class="col-md-5">
                        <label for="fecha" class="form-label fw-semibold">Fecha de devolución *</label>
                        <input type="date" id="fecha" name="fechaDevolucion" class="form-control"
                               min="<%= hoy %>" value="<%= Html.esc(formFecha) %>">
                    </div>
                    <div class="col-12">
                        <label for="observaciones" class="form-label fw-semibold">Observaciones</label>
                        <textarea id="observaciones" name="observaciones" class="form-control" rows="2"
                                  maxlength="255"><%= Html.esc(formObs) %></textarea>
                    </div>
                </div>
            </section>

            <section class="card p-4">
                <h2 class="h5 fw-bold mb-3">Equipos disponibles</h2>
                <% if (equipos.isEmpty()) { %>
                <p class="text-secondary mb-0">No hay equipos disponibles en este momento.</p>
                <% } else { %>
                <div class="table-responsive">
                    <table class="table tabla align-middle mb-0">
                        <thead>
                            <tr><th style="width: 40px"></th><th>Código</th><th>Equipo</th><th>Categoría</th><th>Precio por día</th></tr>
                        </thead>
                        <tbody>
                        <% for (Equipo eq : equipos) { %>
                            <tr>
                                <td><input type="checkbox" class="form-check-input chk-equipo" name="equipoId"
                                           value="<%= eq.getId() %>" data-precio="<%= eq.getPrecioDia().toPlainString() %>"
                                           id="eq<%= eq.getId() %>" aria-label="Seleccionar <%= Html.esc(eq.getCodigo()) %>"
                                           <%= marcados != null && marcados.contains(eq.getId()) ? "checked" : "" %>></td>
                                <td class="fw-semibold"><label for="eq<%= eq.getId() %>"><%= Html.esc(eq.getCodigo()) %></label></td>
                                <td><%= Html.esc(eq.getNombre()) %>
                                    <% if (!eq.getMarca().isEmpty()) { %><span class="text-secondary small">· <%= Html.esc(eq.getMarca()) %></span><% } %></td>
                                <td><%= Html.esc(eq.getCategoria()) %></td>
                                <td class="text-nowrap">S/ <%= eq.getPrecioDia().toPlainString() %></td>
                            </tr>
                        <% } %>
                        </tbody>
                    </table>
                </div>
                <% } %>
            </section>
        </div>

        <div class="col-12 col-xl-4">
            <section class="card p-4" id="resumen"
                     data-hoy="<%= hoy %>" data-igv="<%= AlquilerService.IGV.toPlainString() %>"
                     data-garantia="<%= AlquilerService.GARANTIA.toPlainString() %>">
                <h2 class="h5 fw-bold mb-3">Resumen estimado</h2>
                <dl class="row mb-0">
                    <dt class="col-7 fw-normal text-secondary">Equipos</dt><dd class="col-5 text-end" id="rEquipos">0</dd>
                    <dt class="col-7 fw-normal text-secondary">Días</dt><dd class="col-5 text-end" id="rDias">0</dd>
                    <dt class="col-7 fw-normal text-secondary">Subtotal</dt><dd class="col-5 text-end" id="rSubtotal">S/ 0.00</dd>
                    <dt class="col-7 fw-normal text-secondary">IGV (<%= AlquilerService.IGV.movePointRight(2).stripTrailingZeros().toPlainString() %> %)</dt><dd class="col-5 text-end" id="rIgv">S/ 0.00</dd>
                    <dt class="col-7">Total del alquiler</dt><dd class="col-5 text-end fw-bold" id="rTotal">S/ 0.00</dd>
                    <dt class="col-7 fw-normal text-secondary">Garantía (<%= AlquilerService.GARANTIA.movePointRight(2).stripTrailingZeros().toPlainString() %> %, reembolsable)</dt><dd class="col-5 text-end" id="rGarantia">S/ 0.00</dd>
                    <dt class="col-7">A cobrar al entregar</dt><dd class="col-5 text-end fw-bold" id="rCobrar">S/ 0.00</dd>
                </dl>
                <p class="text-secondary small mt-3 mb-0">Valores estimados: el sistema calcula los montos definitivos al registrar.</p>
                <button type="submit" class="btn btn-naranja w-100 mt-3">Registrar alquiler</button>
                <a class="btn btn-outline-primary w-100 mt-2" href="<%= ctx %>/alquileres">Cancelar</a>
            </section>
        </div>
    </div>
</form>

<script>
    (function () {
        var panel = document.getElementById('resumen');
        var hoy = new Date(panel.dataset.hoy + 'T00:00:00');
        var igv = parseFloat(panel.dataset.igv);
        var garantia = parseFloat(panel.dataset.garantia);

        function soles(n) { return 'S/ ' + n.toFixed(2); }

        function recalcular() {
            var f = document.getElementById('fecha').value;
            var dias = 0;
            if (f) {
                var d = new Date(f + 'T00:00:00');
                dias = Math.max(1, Math.round((d - hoy) / 86400000));
            }
            var marcados = document.querySelectorAll('.chk-equipo:checked');
            var subtotal = 0;
            marcados.forEach(function (c) { subtotal += parseFloat(c.dataset.precio) * dias; });
            var iva = subtotal * igv;
            var gar = subtotal * garantia;
            document.getElementById('rEquipos').textContent = marcados.length;
            document.getElementById('rDias').textContent = dias;
            document.getElementById('rSubtotal').textContent = soles(subtotal);
            document.getElementById('rIgv').textContent = soles(iva);
            document.getElementById('rTotal').textContent = soles(subtotal + iva);
            document.getElementById('rGarantia').textContent = soles(gar);
            document.getElementById('rCobrar').textContent = soles(subtotal + iva + gar);
        }

        document.getElementById('fecha').addEventListener('change', recalcular);
        document.querySelectorAll('.chk-equipo').forEach(function (c) { c.addEventListener('change', recalcular); });
        recalcular();
    })();
</script>

<%@ include file="fragmentos/pie.jspf" %>
