package com.rentatools.controller;

import com.rentatools.model.Pagina;
import com.rentatools.util.Paginacion;

import com.rentatools.model.Alquiler;
import com.rentatools.model.GuiaEntrega;
import com.rentatools.service.AlquilerService;
import com.rentatools.service.GuiaService;
import com.rentatools.service.ValidacionException;
import com.rentatools.util.CsrfUtil;
import java.io.IOException;
import java.sql.SQLException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/**
 * Guias de entrega. Se emiten solas al registrar un alquiler (Observer); aqui se consultan, se imprimen,
 * se registra la conformidad del cliente y, si faltara alguna, se emite a mano.
 */
@WebServlet(name = "GuiaServlet", urlPatterns = {"/guias"})
public class GuiaServlet extends HttpServlet {

    private static final String VISTAS = "/WEB-INF/vistas/";
    private final GuiaService servicio = new GuiaService();
    private final AlquilerService alquilerService = new AlquilerService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String accion = request.getParameter("accion");
        try {
            if ("ver".equals(accion)) {
                GuiaEntrega g = servicio.obtener(parseId(request.getParameter("id")));
                if (g == null) {
                    flash(request, "danger", "La guía no existe");
                    response.sendRedirect(request.getContextPath() + "/guias");
                    return;
                }
                Alquiler a = alquilerService.obtener(g.getAlquilerId());
                request.setAttribute("guia", g);
                request.setAttribute("alquiler", a);
                request.setAttribute("csrf", CsrfUtil.token(request.getSession()));
                consumirFlash(request);
                request.getRequestDispatcher(VISTAS + "guia-detalle.jsp").forward(request, response);
            } else {
                String qLista = Paginacion.texto(request.getParameter("q"));
                int nPagina = Paginacion.entero(request.getParameter("pagina"), 1);
                Pagina<GuiaEntrega> pagLista = servicio.listarPagina(qLista, nPagina);
                request.setAttribute("guias", pagLista.getItems());
                request.setAttribute("pag", pagLista);
                boolean vivo = "1".equals(request.getParameter("vivo"));   // busqueda en vivo: no recalcular indicadores
                request.setAttribute("conteos", vivo ? java.util.Collections.<String, Integer>emptyMap() : servicio.contarPorEstado());
                consumirFlash(request);
                request.getRequestDispatcher(VISTAS + "guias.jsp").forward(request, response);
            }
        } catch (SQLException e) {
            throw new ServletException(e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        if (!CsrfUtil.valido(request)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Solicitud no valida");
            return;
        }
        String accion = request.getParameter("accion");
        if (accion == null) {
            accion = "";
        }

        try {
            switch (accion) {
                case "emitir": {
                    int alquilerId = parseId(request.getParameter("alquilerId"));
                    try {
                        GuiaEntrega g = servicio.emitirPara(alquilerId);
                        flash(request, "success", "Guía " + g.getNumeroCompleto() + " emitida");
                        response.sendRedirect(request.getContextPath() + "/guias?accion=ver&id=" + g.getId());
                    } catch (ValidacionException e) {
                        flash(request, "danger", e.getMessage());
                        response.sendRedirect(request.getContextPath() + "/alquileres?accion=ver&id=" + alquilerId);
                    }
                    break;
                }
                case "conformidad": {
                    int id = parseId(request.getParameter("id"));
                    try {
                        servicio.registrarConformidad(id, request.getParameter("recibidoPor"),
                                                      request.getParameter("documentoReceptor"));
                        flash(request, "success", "Conformidad de entrega registrada");
                    } catch (ValidacionException e) {
                        flash(request, "danger", e.getMessage());
                    }
                    response.sendRedirect(request.getContextPath() + "/guias?accion=ver&id=" + id);
                    break;
                }
                default:
                    response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            }
        } catch (SQLException e) {
            throw new ServletException(e);
        }
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
