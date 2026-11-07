<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="java.util.List"%>
<%@page import="java.util.Set"%>
<%@page import="com.rentatools.model.Cliente"%>
<%@page import="com.rentatools.model.Equipo"%>
<%@page import="com.rentatools.service.AlquilerService"%>
<% String tituloPagina = "Solicitud de alquiler"; String menuActivo = "solicitudes"; %>
<%@ include file="fragmentos/cabecera.jspf" %>
<%
    boolean editar = "editar".equals(request.getAttribute("modo"));
    int solicitudId = (Integer) request.getAttribute("solicitudId");
    @SuppressWarnings("unchecked")
    List<Cliente> clientes = (List<Cliente>) request.getAttribute("clientes");
    @SuppressWarnings("unchecked")
    List<Equipo> equipos = (List<Equipo>) request.getAttribute("equipos");
    @SuppressWarnings("unchecked")
    List<String> errores = (List<String>) request.getAttribute("errores");
    @SuppressWarnings("unchecked")
    Set<Integer> marcados = (Set<Integer>) request.getAttribute("formEquipoIds");
    int formClienteId = (Integer) request.getAttribute("formClienteId");
    String formDias = (String) request.getAttribute("formDias");
    String formObs = (String) request.getAttribute("formObservaciones");
    String csrf = (String) request.getAttribute("csrf");
%>

<div class="mb-4">
    <h1 class="h3 fw-bold mb-1"><%= editar ? "Editar solicitud N.º " + solicitudId : "Nueva solicitud de alquiler" %></h1>
    <p class="text-secondary mb-0">Los montos son estimados. Los equipos se reservan recién cuando la solicitud se convierte en alquiler.</p>
</div>

<% if (errores != null && !errores.isEmpty()) { %>
<div class="alert alert-danger" role="alert">
    <ul class="mb-0"><% for (String err : errores) { %><li><%= Html.esc(err) %></li><% } %></ul>
</div>
<% } %>

<form method="post" action="<%= ctx %>/solicitudes" novalidate>
    <input type="hidden" name="accion" value="<%= editar ? "actualizar" : "crear" %>">
    <input type="hidden" name="csrf" value="<%= Html.esc(csrf) %>">
    <% if (editar) { %><input type="hidden" name="id" value="<%= solicitudId %>"><% } %>

    <div class="row g-3">
        <div class="col-12 col-xl-8">
            <section class="card p-4 mb-3">
                <h2 class="h5 fw-bold mb-3">Cliente y plazo</h2>
                <div class="row g-3">
                    <div class="col-md-8">
                        <label for="clienteId" class="form-label fw-semibold">Cliente *</label>
                        <select id="clienteId" name="clienteId" class="form-select">
                            <option value="">Seleccione…</option>
                            <% for (Cliente c : clientes) { %>
                            <option value="<%= c.getId() %>" <%= c.getId() == formClienteId ? "selected" : "" %>>
                                <%= Html.esc((c.getNombres() + " " + c.getApellidos()).trim()) %> · <%= Html.esc(c.getTipoDocumento()) %> <%= Html.esc(c.getNumeroDocumento()) %>
                            </option>
                            <% } %>
                        </select>
                    </div>
                    <div class="col-md-4">
                        <label for="dias" class="form-label fw-semibold">Días de alquiler *</label>
                        <input type="number" id="dias" name="dias" class="form-control" min="1" max="<%= AlquilerService.DIAS_MAXIMO %>"
                               value="<%= Html.esc(formDias) %>">
                    </div>
                    <div class="col-12">
                        <label for="observaciones" class="form-label fw-semibold">Observaciones</label>
                        <textarea id="observaciones" name="observaciones" class="form-control" rows="2"
                                  maxlength="255"><%= Html.esc(formObs) %></textarea>
                    </div>
                </div>
            </section>

            <section class="card p-4">
                <h2 class="h5 fw-bold mb-3">Equipos solicitados</h2>
                <% if (equipos.isEmpty()) { %>
                <p class="text-secondary mb-0">No hay equipos disponibles en este momento.</p>
                <% } else { %>
                <div class="table-responsive">
                    <table class="table tabla align-middle mb-0">
                        <thead><tr><th style="width: 40px"></th><th>Código</th><th>Equipo</th><th>Categoría</th><th>Precio por día</th></tr></thead>
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
                     data-igv="<%= AlquilerService.IGV.toPlainString() %>"
                     data-garantia="<%= AlquilerService.GARANTIA.toPlainString() %>">
                <h2 class="h5 fw-bold mb-3">Resumen estimado</h2>
                <dl class="row mb-0">
                    <dt class="col-7 fw-normal text-secondary">Equipos</dt><dd class="col-5 text-end" id="rEquipos">0</dd>
                    <dt class="col-7 fw-normal text-secondary">Días</dt><dd class="col-5 text-end" id="rDias">0</dd>
                    <dt class="col-7 fw-normal text-secondary">Subtotal</dt><dd class="col-5 text-end" id="rSubtotal">S/ 0.00</dd>
                    <dt class="col-7 fw-normal text-secondary">IGV</dt><dd class="col-5 text-end" id="rIgv">S/ 0.00</dd>
                    <dt class="col-7">Total</dt><dd class="col-5 text-end fw-bold" id="rTotal">S/ 0.00</dd>
                    <dt class="col-7 fw-normal text-secondary">Garantía</dt><dd class="col-5 text-end" id="rGarantia">S/ 0.00</dd>
                </dl>
                <button type="submit" class="btn btn-naranja w-100 mt-3"><%= editar ? "Guardar cambios" : "Registrar solicitud" %></button>
                <a class="btn btn-outline-primary w-100 mt-2"
                   href="<%= ctx %>/solicitudes<%= editar ? "?accion=ver&amp;id=" + solicitudId : "" %>">Cancelar</a>
            </section>
        </div>
    </div>
</form>

<script>
    (function () {
        var panel = document.getElementById('resumen');
        var igv = parseFloat(panel.dataset.igv);
        var garantia = parseFloat(panel.dataset.garantia);
        function soles(n) { return 'S/ ' + n.toFixed(2); }

        function recalcular() {
            var dias = Math.max(0, parseInt(document.getElementById('dias').value, 10) || 0);
            var marcados = document.querySelectorAll('.chk-equipo:checked');
            var subtotal = 0;
            marcados.forEach(function (c) { subtotal += parseFloat(c.dataset.precio) * dias; });
            document.getElementById('rEquipos').textContent = marcados.length;
            document.getElementById('rDias').textContent = dias;
            document.getElementById('rSubtotal').textContent = soles(subtotal);
            document.getElementById('rIgv').textContent = soles(subtotal * igv);
            document.getElementById('rTotal').textContent = soles(subtotal * (1 + igv));
            document.getElementById('rGarantia').textContent = soles(subtotal * garantia);
        }

        document.getElementById('dias').addEventListener('input', recalcular);
        document.querySelectorAll('.chk-equipo').forEach(function (c) { c.addEventListener('change', recalcular); });
        recalcular();
    })();
</script>

<%@ include file="fragmentos/pie.jspf" %>
