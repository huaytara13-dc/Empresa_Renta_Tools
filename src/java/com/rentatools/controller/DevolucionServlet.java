package com.rentatools.controller;

import com.rentatools.model.Pagina;
import com.rentatools.util.Paginacion;

import com.rentatools.model.Alquiler;
import com.rentatools.model.DetalleAlquiler;
import com.rentatools.model.Devolucion;
import com.rentatools.model.Usuario;
import com.rentatools.service.AlquilerService;
import com.rentatools.service.DevolucionService;
import com.rentatools.service.ItemDevolucion;
import com.rentatools.service.ValidacionException;
import com.rentatools.util.CsrfUtil;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/**
 * Devoluciones: administrador y empleado pueden registrar y consultar.
 * Una devolucion registrada no se edita ni se elimina: queda como constancia.
 */
@WebServlet(name = "DevolucionServlet", urlPatterns = {"/devoluciones"})
public class DevolucionServlet extends HttpServlet {

    private static final String VISTAS = "/WEB-INF/vistas/";
    private final DevolucionService servicio = new DevolucionService();
    private final AlquilerService alquilerService = new AlquilerService();

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
                case "nueva": {
                    int alquilerId = parseId(request.getParameter("alquilerId"));
                    if (alquilerId <= 0) {
                        seleccionarAlquiler(request, response);
                        break;
                    }
                    Alquiler a = alquilerService.obtener(alquilerId);
                    if (a == null || !"ACTIVO".equals(a.getEstado())) {
                        flash(request, "danger", "El alquiler no existe o ya fue devuelto");
                        response.sendRedirect(request.getContextPath() + "/alquileres");
                    } else {
                        mostrarFormulario(request, response, a, null);
                    }
                    break;
                }
                case "ver": {
                    Devolucion d = servicio.obtener(parseId(request.getParameter("id")));
                    if (d == null) {
                        flash(request, "danger", "La devolución no existe");
                        response.sendRedirect(request.getContextPath() + "/devoluciones");
                    } else {
                        request.setAttribute("devolucion", d);
                        consumirFlash(request);
                        request.getRequestDispatcher(VISTAS + "devolucion-detalle.jsp").forward(request, response);
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
        if (!"registrar".equals(request.getParameter("accion"))) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        Usuario actual = (Usuario) request.getAttribute("actual");
        int alquilerId = parseId(request.getParameter("alquilerId"));

        try {
            Alquiler a = alquilerService.obtener(alquilerId);
            if (a == null || !"ACTIVO".equals(a.getEstado())) {
                flash(request, "danger", "El alquiler no existe o ya fue devuelto");
                response.sendRedirect(request.getContextPath() + "/alquileres");
                return;
            }

            List<ItemDevolucion> items = new ArrayList<>();
            for (DetalleAlquiler d : a.getDetalles()) {
                int id = d.getEquipoId();
                items.add(new ItemDevolucion(id,
                        request.getParameter("estado_" + id),
                        request.getParameter("tipo_" + id),
                        request.getParameter("desc_" + id),
                        request.getParameter("monto_" + id)));
            }

            try {
                int numero = servicio.registrar(alquilerId, actual.getId(), items,
                                                request.getParameter("observaciones"));
                flash(request, "success", "Devolución N.º " + numero + " registrada correctamente");
                response.sendRedirect(request.getContextPath() + "/devoluciones?accion=ver&id=" + numero);
            } catch (ValidacionException e) {
                // Los parametros del formulario siguen en el request, asi la pantalla conserva lo escrito
                mostrarFormulario(request, response, a, e.getErrores());
            }
        } catch (SQLException e) {
            throw new ServletException(e);
        }
    }

    // ---------------------- vistas ----------------------
    private void listar(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException, SQLException {

        String qLista = Paginacion.texto(request.getParameter("q"));
        int nPagina = Paginacion.entero(request.getParameter("pagina"), 1);
        Pagina<Devolucion> pagLista = servicio.listarPagina(qLista, nPagina);
        request.setAttribute("devoluciones", pagLista.getItems());
        request.setAttribute("pag", pagLista);
        boolean vivo = "1".equals(request.getParameter("vivo"));   // busqueda en vivo: no recalcular indicadores
        request.setAttribute("ind", vivo ? Paginacion.ceros("total", "conRetraso", "conPenalidad", "sumaPenalidades") : servicio.indicadores());
        consumirFlash(request);
        request.getRequestDispatcher(VISTAS + "devoluciones.jsp").forward(request, response);
    }

    private void seleccionarAlquiler(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException, SQLException {

        List<Alquiler> activos = new ArrayList<>();
        for (Alquiler a : alquilerService.listar()) {
            if ("ACTIVO".equals(a.getEstado())) {
                activos.add(a);
            }
        }
        request.setAttribute("activos", activos);
        request.setAttribute("hoy", AlquilerService.hoy());
        consumirFlash(request);
        request.getRequestDispatcher(VISTAS + "devolucion-seleccion.jsp").forward(request, response);
    }

    private void mostrarFormulario(HttpServletRequest request, HttpServletResponse response,
                                   Alquiler a, List<String> errores)
            throws ServletException, IOException {

        LocalDate hoy = AlquilerService.hoy();
        request.setAttribute("alquiler", a);
        request.setAttribute("hoy", hoy);
        request.setAttribute("diasRetraso", DevolucionService.diasRetraso(a, hoy));
        request.setAttribute("errores", errores);
        request.setAttribute("csrf", CsrfUtil.token(request.getSession()));
        request.getRequestDispatcher(VISTAS + "devolucion-form.jsp").forward(request, response);
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
