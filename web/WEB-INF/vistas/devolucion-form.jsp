<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="java.math.BigDecimal"%>
<%@page import="java.time.LocalDate"%>
<%@page import="java.util.List"%>
<%@page import="com.rentatools.model.Alquiler"%>
<%@page import="com.rentatools.model.DetalleAlquiler"%>
<%@page import="com.rentatools.service.DevolucionService"%>
<%@page import="com.rentatools.service.EquipoService"%>
<% String tituloPagina = "Registrar devolución"; String menuActivo = "devoluciones"; %>
<%@ include file="fragmentos/cabecera.jspf" %>
<%
    Alquiler al = (Alquiler) request.getAttribute("alquiler");
    LocalDate hoy = (LocalDate) request.getAttribute("hoy");
    int diasRetraso = (Integer) request.getAttribute("diasRetraso");
    @SuppressWarnings("unchecked")
    List<String> errores = (List<String>) request.getAttribute("errores");
    String csrf = (String) request.getAttribute("csrf");

    BigDecimal totalRetraso = BigDecimal.ZERO;
    for (DetalleAlquiler x : al.getDetalles()) {
        totalRetraso = totalRetraso.add(DevolucionService.penalidadRetraso(x, diasRetraso));
    }
    String vObs = request.getParameter("observaciones");
    if (vObs == null) vObs = "";
%>

<div class="mb-4">
    <h1 class="h3 fw-bold mb-1">Registrar devolución del alquiler N.º <%= al.getId() %></h1>
    <p class="text-secondary mb-0">Cliente: <%= Html.esc(al.getClienteNombre()) %> · Fecha de devolución: hoy (<%= hoy %>)</p>
</div>

<% if (errores != null && !errores.isEmpty()) { %>
<div class="alert alert-danger" role="alert">
    <ul class="mb-0">
        <% for (String err : errores) { %><li><%= Html.esc(err) %></li><% } %>
    </ul>
</div>
<% } %>

<% if (diasRetraso > 0) { %>
<div class="alert alert-warning" role="alert">
    Este alquiler debía devolverse el <strong><%= al.getFechaDevolucionPrevista() %></strong>:
    hay <strong><%= diasRetraso %> día<%= diasRetraso == 1 ? "" : "s" %> de retraso</strong>.
    El sistema aplica un recargo del <%= DevolucionService.RECARGO_RETRASO.movePointRight(2).stripTrailingZeros().toPlainString() %> %
    del precio diario por cada día y por equipo.
</div>
<% } %>

<form method="post" action="<%= ctx %>/devoluciones" novalidate>
    <input type="hidden" name="accion" value="registrar">
    <input type="hidden" name="alquilerId" value="<%= al.getId() %>">
    <input type="hidden" name="csrf" value="<%= Html.esc(csrf) %>">

    <div class="row g-3">
        <div class="col-12 col-xl-9">
            <section class="card p-4 mb-3">
                <h2 class="h5 fw-bold mb-1">Estado de los equipos</h2>
                <p class="text-secondary small mb-3">Indique cómo queda cada equipo y registre una incidencia si hubo daños, pérdida u otro problema.</p>
                <div class="table-responsive">
                    <table class="table tabla align-top mb-0">
                        <thead>
                            <tr><th>Equipo</th><th>Estado al devolver</th><th>Incidencia</th><th>Descripción</th><th style="width: 120px">Monto (S/)</th></tr>
                        </thead>
                        <tbody>
                        <% for (DetalleAlquiler d : al.getDetalles()) {
                               int id = d.getEquipoId();
                               String vEstado = request.getParameter("estado_" + id);
                               if (vEstado == null) vEstado = "DISPONIBLE";
                               String vTipo = request.getParameter("tipo_" + id);
                               if (vTipo == null) vTipo = "";
                               String vDesc = request.getParameter("desc_" + id);
                               if (vDesc == null) vDesc = "";
                               String vMonto = request.getParameter("monto_" + id);
                               if (vMonto == null) vMonto = "";
                               BigDecimal penRet = DevolucionService.penalidadRetraso(d, diasRetraso);
                        %>
                            <tr class="fila-dev">
                                <td class="text-nowrap"><span class="fw-semibold"><%= Html.esc(d.getEquipoCodigo()) %></span>
                                    <div class="small"><%= Html.esc(d.getEquipoNombre()) %></div>
                                    <% if (penRet.signum() > 0) { %><div class="small text-danger">Retraso: S/ <%= penRet.toPlainString() %></div><% } %></td>
                                <td>
                                    <select name="estado_<%= id %>" class="form-select form-select-sm" aria-label="Estado del equipo <%= Html.esc(d.getEquipoCodigo()) %>">
                                        <% for (String est : DevolucionService.ESTADOS_DEVOLUCION) { %>
                                        <option value="<%= est %>" <%= est.equals(vEstado) ? "selected" : "" %>><%= Html.esc(EquipoService.etiquetaEstado(est)) %></option>
                                        <% } %>
                                    </select>
                                </td>
                                <td>
                                    <select name="tipo_<%= id %>" class="form-select form-select-sm in-tipo" aria-label="Incidencia del equipo <%= Html.esc(d.getEquipoCodigo()) %>">
                                        <option value="">Sin incidencia</option>
                                        <% for (String tp : DevolucionService.TIPOS_MANUALES) { %>
                                        <option value="<%= tp %>" <%= tp.equals(vTipo) ? "selected" : "" %>><%= Html.esc(DevolucionService.etiquetaTipo(tp)) %></option>
                                        <% } %>
                                    </select>
                                </td>
                                <td><input type="text" name="desc_<%= id %>" class="form-control form-control-sm" maxlength="255"
                                           aria-label="Descripción de la incidencia de <%= Html.esc(d.getEquipoCodigo()) %>"
                                           value="<%= Html.esc(vDesc) %>"></td>
                                <td><input type="text" name="monto_<%= id %>" class="form-control form-control-sm in-monto" inputmode="decimal"
                                           aria-label="Monto de la incidencia de <%= Html.esc(d.getEquipoCodigo()) %>"
                                           value="<%= Html.esc(vMonto) %>"></td>
                            </tr>
                        <% } %>
                        </tbody>
                    </table>
                </div>
                <div class="form-text mt-2">
                    Un equipo con daño no puede quedar Disponible, y uno perdido debe quedar De baja.
                    El monto de una incidencia se descuenta de la garantía.
                </div>
            </section>

            <section class="card p-4">
                <label for="observaciones" class="form-label fw-semibold">Observaciones</label>
                <textarea id="observaciones" name="observaciones" class="form-control" rows="2" maxlength="255"><%= Html.esc(vObs) %></textarea>
            </section>
        </div>

        <div class="col-12 col-xl-3">
            <section class="card p-4" id="resumen"
                     data-garantia="<%= al.getGarantia().toPlainString() %>"
                     data-retraso="<%= totalRetraso.toPlainString() %>">
                <h2 class="h5 fw-bold mb-3">Liquidación estimada</h2>
                <dl class="row mb-0">
                    <dt class="col-7 fw-normal text-secondary">Garantía</dt><dd class="col-5 text-end">S/ <%= al.getGarantia().toPlainString() %></dd>
                    <dt class="col-7 fw-normal text-secondary">Retraso</dt><dd class="col-5 text-end" id="rRetraso">S/ 0.00</dd>
                    <dt class="col-7 fw-normal text-secondary">Incidencias</dt><dd class="col-5 text-end" id="rIncid">S/ 0.00</dd>
                    <dt class="col-7">Penalidad total</dt><dd class="col-5 text-end fw-bold" id="rTotal">S/ 0.00</dd>
                    <dt class="col-7">Garantía a devolver</dt><dd class="col-5 text-end fw-bold text-success" id="rDevuelta">S/ 0.00</dd>
                    <dt class="col-7">Cobro adicional</dt><dd class="col-5 text-end fw-bold text-danger" id="rAdicional">S/ 0.00</dd>
                </dl>
                <p class="text-secondary small mt-3 mb-0">Valores estimados: el sistema calcula los montos definitivos al registrar.</p>
                <button type="submit" class="btn btn-naranja w-100 mt-3">Registrar devolución</button>
                <a class="btn btn-outline-primary w-100 mt-2" href="<%= ctx %>/alquileres?accion=ver&amp;id=<%= al.getId() %>">Cancelar</a>
            </section>
        </div>
    </div>
</form>

<script>
    (function () {
        var panel = document.getElementById('resumen');
        var garantia = parseFloat(panel.dataset.garantia);
        var retraso = parseFloat(panel.dataset.retraso);

        function soles(n) { return 'S/ ' + n.toFixed(2); }

        function recalcular() {
            var incidencias = 0;
            document.querySelectorAll('.fila-dev').forEach(function (fila) {
                var tipo = fila.querySelector('.in-tipo').value;
                var v = parseFloat((fila.querySelector('.in-monto').value || '0').replace(',', '.'));
                if (tipo && !isNaN(v) && v > 0) { incidencias += v; }
            });
            var total = retraso + incidencias;
            document.getElementById('rRetraso').textContent = soles(retraso);
            document.getElementById('rIncid').textContent = soles(incidencias);
            document.getElementById('rTotal').textContent = soles(total);
            document.getElementById('rDevuelta').textContent = soles(Math.max(0, garantia - total));
            document.getElementById('rAdicional').textContent = soles(Math.max(0, total - garantia));
        }

        document.querySelectorAll('.in-tipo, .in-monto').forEach(function (el) {
            el.addEventListener('input', recalcular);
            el.addEventListener('change', recalcular);
        });
        recalcular();
    })();
</script>

<%@ include file="fragmentos/pie.jspf" %>
