package com.rentatools.controller;

import com.rentatools.model.Cliente;
import com.rentatools.model.DetalleSolicitud;
import com.rentatools.model.Equipo;
import com.rentatools.model.Solicitud;
import com.rentatools.model.Usuario;
import com.rentatools.service.ClienteService;
import com.rentatools.service.EquipoService;
import com.rentatools.service.SolicitudService;
import com.rentatools.service.ValidacionException;
import com.rentatools.util.CsrfUtil;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/**
 * Solicitudes de alquiler (SOLPED). Administrador y empleado pueden registrar, editar (si esta pendiente),
 * consultar y convertir en alquiler; solo el administrador aprueba, rechaza y anula.
 */
@WebServlet(name = "SolicitudServlet", urlPatterns = {"/solicitudes"})
public class SolicitudServlet extends HttpServlet {

    private static final String VISTAS = "/WEB-INF/vistas/";
    private final SolicitudService servicio = new SolicitudService();
    private final ClienteService clienteService = new ClienteService();
    private final EquipoService equipoService = new EquipoService();

    // ---------------------- GET ----------------------
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String accion = request.getParameter("accion");
        if (accion == null) {
            accion = "listar";
        }

        try {
            switch (accion) {
                case "nueva":
                    mostrarFormulario(request, response, "crear", 0, null, 0, "1", null, "");
                    break;
                case "editar": {
                    Solicitud s = servicio.obtener(parseId(request.getParameter("id")));
                    if (s == null || !"PENDIENTE".equals(s.getEstado())) {
                        flash(request, "danger", "La solicitud no existe o ya no se puede editar");
                        response.sendRedirect(request.getContextPath() + "/solicitudes");
                    } else {
                        Set<Integer> ids = new LinkedHashSet<>();
                        for (DetalleSolicitud d : s.getDetalles()) {
                            ids.add(d.getEquipoId());
                        }
                        mostrarFormulario(request, response, "editar", s.getId(), null, s.getClienteId(),
                                String.valueOf(s.getDiasSolicitados()), ids, s.getObservaciones());
                    }
                    break;
                }
                case "ver": {
                    Solicitud s = servicio.obtener(parseId(request.getParameter("id")));
                    if (s == null) {
                        flash(request, "danger", "La solicitud no existe");
                        response.sendRedirect(request.getContextPath() + "/solicitudes");
                    } else {
                        request.setAttribute("solicitud", s);
                        request.setAttribute("csrf", CsrfUtil.token(request.getSession()));
                        consumirFlash(request);
                        request.getRequestDispatcher(VISTAS + "solicitud-detalle.jsp").forward(request, response);
                    }
                    break;
                }
                default:
                    request.setAttribute("solicitudes", servicio.listar());
                    consumirFlash(request);
                    request.getRequestDispatcher(VISTAS + "solicitudes.jsp").forward(request, response);
            }
        } catch (SQLException e) {
            throw new ServletException(e);
        }
    }

    // ---------------------- POST ----------------------
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        if (!CsrfUtil.valido(request)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Solicitud no valida");
            return;
        }
        Usuario actual = (Usuario) request.getAttribute("actual");
        boolean admin = actual != null && "ADMIN".equals(actual.getRol());
        String accion = request.getParameter("accion");
        if (accion == null) {
            accion = "";
        }

        try {
            switch (accion) {
                case "crear":
                    crear(request, response, actual);
                    break;
                case "actualizar":
                    actualizar(request, response);
                    break;
                case "convertir":
                    convertir(request, response, actual);
                    break;
                case "aprobar":
                case "rechazar":
                case "anular":
                    if (!admin) {
                        response.sendError(HttpServletResponse.SC_FORBIDDEN,
                                "Solo el administrador puede aprobar, rechazar o anular solicitudes");
                        return;
                    }
                    resolver(request, response, actual, accion);
                    break;
                default:
                    response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            }
        } catch (SQLException e) {
            throw new ServletException(e);
        }
    }

    // ---------------------- acciones ----------------------
    private void crear(HttpServletRequest request, HttpServletResponse response, Usuario actual)
            throws ServletException, IOException, SQLException {

        int clienteId = parseId(request.getParameter("clienteId"));
        String diasTexto = request.getParameter("dias");
        Set<Integer> equipoIds = leerEquipos(request);
        String obs = request.getParameter("observaciones");

        try {
            int numero = servicio.crear(clienteId, actual.getId(), parseId(diasTexto),
                                        new ArrayList<>(equipoIds), obs);
            flash(request, "success", "Solicitud N.º " + numero + " registrada. Queda pendiente de aprobación");
            response.sendRedirect(request.getContextPath() + "/solicitudes?accion=ver&id=" + numero);
        } catch (ValidacionException e) {
            mostrarFormulario(request, response, "crear", 0, e.getErrores(), clienteId, diasTexto,
                              equipoIds, obs == null ? "" : obs);
        }
    }

    private void actualizar(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException, SQLException {

        int id = parseId(request.getParameter("id"));
        int clienteId = parseId(request.getParameter("clienteId"));
        String diasTexto = request.getParameter("dias");
        Set<Integer> equipoIds = leerEquipos(request);
        String obs = request.getParameter("observaciones");

        try {
            servicio.actualizar(id, clienteId, parseId(diasTexto), new ArrayList<>(equipoIds), obs);
            flash(request, "success", "Solicitud actualizada correctamente");
            response.sendRedirect(request.getContextPath() + "/solicitudes?accion=ver&id=" + id);
        } catch (ValidacionException e) {
            mostrarFormulario(request, response, "editar", id, e.getErrores(), clienteId, diasTexto,
                              equipoIds, obs == null ? "" : obs);
        }
    }

    private void resolver(HttpServletRequest request, HttpServletResponse response, Usuario actual, String accion)
            throws IOException, SQLException {

        int id = parseId(request.getParameter("id"));
        try {
            if ("aprobar".equals(accion)) {
                servicio.aprobar(id, actual.getId());
                flash(request, "success", "Solicitud aprobada. Ya se puede convertir en alquiler");
            } else if ("rechazar".equals(accion)) {
                servicio.rechazar(id, actual.getId());
                flash(request, "success", "Solicitud rechazada");
            } else {
                servicio.anular(id, actual.getId());
                flash(request, "success", "Solicitud anulada");
            }
        } catch (ValidacionException e) {
            flash(request, "danger", e.getMessage());
        }
        response.sendRedirect(request.getContextPath() + "/solicitudes?accion=ver&id=" + id);
    }

    private void convertir(HttpServletRequest request, HttpServletResponse response, Usuario actual)
            throws IOException, SQLException {

        int id = parseId(request.getParameter("id"));
        try {
            int alquilerId = servicio.convertir(id, actual.getId());
            flash(request, "success", "Solicitud N.º " + id + " convertida en el alquiler N.º " + alquilerId);
            response.sendRedirect(request.getContextPath() + "/alquileres?accion=ver&id=" + alquilerId);
        } catch (ValidacionException e) {
            flash(request, "danger", e.getMessage());
            response.sendRedirect(request.getContextPath() + "/solicitudes?accion=ver&id=" + id);
        }
    }

    // ---------------------- vistas ----------------------
    private void mostrarFormulario(HttpServletRequest request, HttpServletResponse response, String modo,
                                   int solicitudId, List<String> errores, int clienteId, String diasTexto,
                                   Set<Integer> equipoIds, String observaciones)
            throws ServletException, IOException, SQLException {

        List<Cliente> clientesActivos = new ArrayList<>();
        for (Cliente c : clienteService.listar()) {
            if ("ACTIVO".equals(c.getEstado())) {
                clientesActivos.add(c);
            }
        }
        List<Equipo> disponibles = new ArrayList<>();
        for (Equipo e : equipoService.listar()) {
            if ("DISPONIBLE".equals(e.getEstado())) {
                disponibles.add(e);
            }
        }

        request.setAttribute("modo", modo);
        request.setAttribute("solicitudId", solicitudId);
        request.setAttribute("clientes", clientesActivos);
        request.setAttribute("equipos", disponibles);
        request.setAttribute("errores", errores);
        request.setAttribute("formClienteId", clienteId);
        request.setAttribute("formDias", diasTexto == null ? "1" : diasTexto);
        request.setAttribute("formEquipoIds", equipoIds != null ? equipoIds : new LinkedHashSet<Integer>());
        request.setAttribute("formObservaciones", observaciones);
        request.setAttribute("csrf", CsrfUtil.token(request.getSession()));
        request.getRequestDispatcher(VISTAS + "solicitud-form.jsp").forward(request, response);
    }

    // ---------------------- auxiliares ----------------------
    private Set<Integer> leerEquipos(HttpServletRequest request) {
        Set<Integer> ids = new LinkedHashSet<>();
        String[] valores = request.getParameterValues("equipoId");
        if (valores != null) {
            for (String v : valores) {
                int id = parseId(v);
                if (id > 0) {
                    ids.add(id);
                }
            }
        }
        return ids;
    }

    private int parseId(String texto) {
        try {
            return Integer.parseInt(texto.trim());
        } catch (NumberFormatException | NullPointerException e) {
            return -1;
        }
    }

    private void flash(HttpServletRequest request, String tipo, String mensaje) {
        HttpSession sesion = request.getSession();
        sesion.setAttribute("flashTipo", tipo);
        sesion.setAttribute("flashMensaje", mensaje);
    }

    private void consumirFlash(HttpServletRequest request) {
        HttpSession sesion = request.getSession();
        request.setAttribute("flashTipo", sesion.getAttribute("flashTipo"));
        request.setAttribute("flashMensaje", sesion.getAttribute("flashMensaje"));
        sesion.removeAttribute("flashTipo");
        sesion.removeAttribute("flashMensaje");
    }
}
