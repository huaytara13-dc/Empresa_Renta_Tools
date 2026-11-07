package com.rentatools.controller;

import com.rentatools.model.Alquiler;
import com.rentatools.model.Cliente;
import com.rentatools.model.Comprobante;
import com.rentatools.model.Devolucion;
import com.rentatools.model.Equipo;
import com.rentatools.model.GuiaEntrega;
import com.rentatools.model.Usuario;
import com.rentatools.service.AlquilerService;
import com.rentatools.service.ClienteService;
import com.rentatools.service.ComprobanteService;
import com.rentatools.service.DevolucionService;
import com.rentatools.service.EquipoService;
import com.rentatools.service.GuiaService;
import com.rentatools.service.ValidacionException;
import com.rentatools.util.CsrfUtil;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
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
 * Alquileres: administrador y empleado pueden registrar y consultar;
 * solo el administrador puede anular.
 */
@WebServlet(name = "AlquilerServlet", urlPatterns = {"/alquileres"})
public class AlquilerServlet extends HttpServlet {

    private static final String VISTAS = "/WEB-INF/vistas/";
    private final AlquilerService servicio = new AlquilerService();
    private final ClienteService clienteService = new ClienteService();
    private final EquipoService equipoService = new EquipoService();
    private final DevolucionService devolucionService = new DevolucionService();
    private final ComprobanteService comprobanteService = new ComprobanteService();
    private final GuiaService guiaService = new GuiaService();

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
                case "nuevo":
                    mostrarFormulario(request, response, null, 0, null, null, "");
                    break;
                case "ver": {
                    Alquiler a = servicio.obtener(parseId(request.getParameter("id")));
                    if (a == null) {
                        flash(request, "danger", "El alquiler no existe");
                        response.sendRedirect(request.getContextPath() + "/alquileres");
                    } else {
                        request.setAttribute("alquiler", a);
                        Devolucion dev = "FINALIZADO".equals(a.getEstado())
                                ? devolucionService.obtenerPorAlquiler(a.getId()) : null;
                        request.setAttribute("devolucion", dev);
                        Comprobante comprobante = comprobanteService.obtenerPorAlquiler(a.getId());
                        GuiaEntrega guia = guiaService.obtenerPorAlquiler(a.getId());
                        request.setAttribute("comprobante", comprobante);
                        request.setAttribute("guia", guia);
                        request.setAttribute("hoy", AlquilerService.hoy());
                        request.setAttribute("csrf", CsrfUtil.token(request.getSession()));
                        consumirFlash(request);
                        request.getRequestDispatcher(VISTAS + "alquiler-detalle.jsp").forward(request, response);
                    }
                    break;
                }
                default:
                    listar(request, response);
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
        String accion = request.getParameter("accion");
        if (accion == null) {
            accion = "";
        }

        try {
            switch (accion) {
                case "registrar":
                    registrar(request, response, actual);
                    break;
                case "anular":
                    if (actual == null || !"ADMIN".equals(actual.getRol())) {
                        response.sendError(HttpServletResponse.SC_FORBIDDEN,
                                "Solo el administrador puede anular alquileres");
                        return;
                    }
                    anular(request, response);
                    break;
                default:
                    response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            }
        } catch (SQLException e) {
            throw new ServletException(e);
        }
    }

    // ---------------------- acciones ----------------------
    private void registrar(HttpServletRequest request, HttpServletResponse response, Usuario actual)
            throws ServletException, IOException, SQLException {

        int clienteId = parseId(request.getParameter("clienteId"));
        String fechaTexto = request.getParameter("fechaDevolucion");
        Set<Integer> equipoIds = new LinkedHashSet<>();
        String[] valores = request.getParameterValues("equipoId");
        if (valores != null) {
            for (String v : valores) {
                int id = parseId(v);
                if (id > 0) {
                    equipoIds.add(id);
                }
            }
        }
        String observaciones = request.getParameter("observaciones");

        LocalDate prevista = null;
        try {
            prevista = LocalDate.parse(fechaTexto);
        } catch (DateTimeParseException | NullPointerException e) {
            // se deja en null: el servicio informa el error
        }

        try {
            int numero = servicio.registrar(clienteId, actual.getId(), prevista,
                                            new ArrayList<>(equipoIds), observaciones);
            flash(request, "success", "Alquiler N.º " + numero + " registrado correctamente");
            response.sendRedirect(request.getContextPath() + "/alquileres?accion=ver&id=" + numero);
        } catch (ValidacionException e) {
            mostrarFormulario(request, response, e.getErrores(), clienteId, fechaTexto,
                              equipoIds, observaciones == null ? "" : observaciones);
        }
    }

    private void anular(HttpServletRequest request, HttpServletResponse response)
            throws IOException, SQLException {

        int id = parseId(request.getParameter("id"));
        try {
            servicio.anular(id);
            flash(request, "success", "Alquiler anulado: los equipos quedaron disponibles");
        } catch (ValidacionException e) {
            flash(request, "danger", e.getMessage());
        }
        response.sendRedirect(request.getContextPath() + "/alquileres?accion=ver&id=" + id);
    }

    // ---------------------- vistas ----------------------
    private void listar(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException, SQLException {

        request.setAttribute("alquileres", servicio.listar());
        request.setAttribute("hoy", AlquilerService.hoy());
        consumirFlash(request);
        request.getRequestDispatcher(VISTAS + "alquileres.jsp").forward(request, response);
    }

    private void mostrarFormulario(HttpServletRequest request, HttpServletResponse response,
                                   List<String> errores, int clienteId, String fechaTexto,
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

        LocalDate hoy = AlquilerService.hoy();
        request.setAttribute("clientes", clientesActivos);
        request.setAttribute("equipos", disponibles);
        request.setAttribute("errores", errores);
        request.setAttribute("formClienteId", clienteId);
        request.setAttribute("formFecha", fechaTexto != null ? fechaTexto : hoy.plusDays(1).toString());
        request.setAttribute("formEquipoIds", equipoIds != null ? equipoIds : new LinkedHashSet<Integer>());
        request.setAttribute("formObservaciones", observaciones);
        request.setAttribute("hoy", hoy);
        request.setAttribute("csrf", CsrfUtil.token(request.getSession()));
        request.getRequestDispatcher(VISTAS + "alquiler-form.jsp").forward(request, response);
    }

    // ---------------------- auxiliares ----------------------
    private int parseId(String texto) {
        try {
            return Integer.parseInt(texto);
        } catch (NumberFormatException e) {
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
