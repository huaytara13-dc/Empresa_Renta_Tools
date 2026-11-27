package com.rentatools.controller;

import com.rentatools.model.Pagina;
import com.rentatools.util.Paginacion;

import com.rentatools.model.Alquiler;
import com.rentatools.model.Comprobante;
import com.rentatools.service.AlquilerService;
import com.rentatools.service.ComprobanteService;
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
 * Comprobantes de pago. Se emiten solos al registrar un alquiler (Observer); aqui se consultan, se imprimen
 * y, si faltara alguno, se emite a mano. Un comprobante emitido no se edita ni se elimina: queda anulado
 * cuando se anula su alquiler.
 */
@WebServlet(name = "ComprobanteServlet", urlPatterns = {"/comprobantes"})
public class ComprobanteServlet extends HttpServlet {

    private static final String VISTAS = "/WEB-INF/vistas/";
    private final ComprobanteService servicio = new ComprobanteService();
    private final AlquilerService alquilerService = new AlquilerService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String accion = request.getParameter("accion");
        try {
            if ("ver".equals(accion)) {
                Comprobante c = servicio.obtener(parseId(request.getParameter("id")));
                if (c == null) {
                    flash(request, "danger", "El comprobante no existe");
                    response.sendRedirect(request.getContextPath() + "/comprobantes");
                    return;
                }
                Alquiler a = alquilerService.obtener(c.getAlquilerId());
                request.setAttribute("comprobante", c);
                request.setAttribute("alquiler", a);
                consumirFlash(request);
                request.getRequestDispatcher(VISTAS + "comprobante-detalle.jsp").forward(request, response);
            } else {
                String qLista = Paginacion.texto(request.getParameter("q"));
                int nPagina = Paginacion.entero(request.getParameter("pagina"), 1);
                Pagina<Comprobante> pagLista = servicio.listarPagina(qLista, nPagina);
                request.setAttribute("comprobantes", pagLista.getItems());
                request.setAttribute("pag", pagLista);
                boolean vivo = "1".equals(request.getParameter("vivo"));   // busqueda en vivo: no recalcular indicadores
                request.setAttribute("ind", vivo ? Paginacion.ceros("emitidos", "boletas", "facturas", "facturado") : servicio.indicadores());
                consumirFlash(request);
                request.getRequestDispatcher(VISTAS + "comprobantes.jsp").forward(request, response);
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
        if (!"emitir".equals(request.getParameter("accion"))) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        int alquilerId = parseId(request.getParameter("alquilerId"));
        try {
            Comprobante c = servicio.emitirPara(alquilerId);
            flash(request, "success", "Comprobante " + c.getNumeroCompleto() + " emitido");
            response.sendRedirect(request.getContextPath() + "/comprobantes?accion=ver&id=" + c.getId());
        } catch (ValidacionException e) {
            flash(request, "danger", e.getMessage());
            response.sendRedirect(request.getContextPath() + "/alquileres?accion=ver&id=" + alquilerId);
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
