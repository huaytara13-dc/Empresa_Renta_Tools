package com.rentatools.controller;

import com.rentatools.model.Equipo;
import com.rentatools.model.Usuario;
import com.rentatools.service.EquipoService;
import com.rentatools.service.ValidacionException;
import com.rentatools.util.CsrfUtil;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/**
 * Equipos: todos los usuarios pueden consultar la lista;
 * solo el administrador puede registrar, editar y eliminar.
 */
@WebServlet(name = "EquipoServlet", urlPatterns = {"/equipos"})
public class EquipoServlet extends HttpServlet {

    private static final String VISTAS = "/WEB-INF/vistas/";
    private final EquipoService servicio = new EquipoService();

    // ---------------------- GET ----------------------
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String accion = request.getParameter("accion");
        if (accion == null) {
            accion = "listar";
        }
        if (("nuevo".equals(accion) || "editar".equals(accion)) && !esAdmin(request)) {
            denegar(response);
            return;
        }

        try {
            switch (accion) {
                case "nuevo": {
                    Equipo e = new Equipo();
                    e.setEstado("DISPONIBLE");
                    mostrarFormulario(request, response, e, "crear", null, null);
                    break;
                }
                case "editar": {
                    Equipo e = servicio.obtener(parseId(request.getParameter("id")));
                    if (e == null) {
                        flash(request, "danger", "El equipo no existe");
                        response.sendRedirect(request.getContextPath() + "/equipos");
                    } else {
                        mostrarFormulario(request, response, e, "editar", null, null);
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

        if (!esAdmin(request)) {
            denegar(response);
            return;
        }
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
                case "crear":
                    crear(request, response);
                    break;
                case "actualizar":
                    actualizar(request, response);
                    break;
                case "eliminar":
                    eliminar(request, response);
                    break;
                default:
                    response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            }
        } catch (SQLException e) {
            throw new ServletException(e);
        }
    }

    // ---------------------- acciones ----------------------
    private void crear(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException, SQLException {

        Equipo e = leerFormulario(request);
        String precio = request.getParameter("precioDia");
        try {
            servicio.crear(e, precio);
            flash(request, "success", "Equipo registrado correctamente");
            response.sendRedirect(request.getContextPath() + "/equipos");
        } catch (ValidacionException ex) {
            mostrarFormulario(request, response, e, "crear", ex.getErrores(), precio);
        }
    }

    private void actualizar(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException, SQLException {

        Equipo e = leerFormulario(request);
        e.setId(parseId(request.getParameter("id")));
        String precio = request.getParameter("precioDia");
        try {
            servicio.actualizar(e, precio);
            flash(request, "success", "Equipo actualizado correctamente");
            response.sendRedirect(request.getContextPath() + "/equipos");
        } catch (ValidacionException ex) {
            mostrarFormulario(request, response, e, "editar", ex.getErrores(), precio);
        }
    }

    private void eliminar(HttpServletRequest request, HttpServletResponse response)
            throws IOException, SQLException {
        try {
            servicio.eliminar(parseId(request.getParameter("id")));
            flash(request, "success", "Equipo eliminado");
        } catch (ValidacionException ex) {
            flash(request, "danger", ex.getMessage());
        }
        response.sendRedirect(request.getContextPath() + "/equipos");
    }

    // ---------------------- vistas ----------------------
    private void listar(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException, SQLException {

        request.setAttribute("equipos", servicio.listar());
        request.setAttribute("csrf", CsrfUtil.token(request.getSession()));

        HttpSession sesion = request.getSession();
        request.setAttribute("flashTipo", sesion.getAttribute("flashTipo"));
        request.setAttribute("flashMensaje", sesion.getAttribute("flashMensaje"));
        sesion.removeAttribute("flashTipo");
        sesion.removeAttribute("flashMensaje");

        request.getRequestDispatcher(VISTAS + "equipos.jsp").forward(request, response);
    }

    private void mostrarFormulario(HttpServletRequest request, HttpServletResponse response,
                                   Equipo e, String modo, List<String> errores, String precioTexto)
            throws ServletException, IOException {

        request.setAttribute("equipoForm", e);
        request.setAttribute("modo", modo);
        request.setAttribute("errores", errores);
        request.setAttribute("precioTexto", precioTexto);
        request.setAttribute("csrf", CsrfUtil.token(request.getSession()));
        request.getRequestDispatcher(VISTAS + "equipo-form.jsp").forward(request, response);
    }

    // ---------------------- auxiliares ----------------------
    private boolean esAdmin(HttpServletRequest request) {
        Usuario actual = (Usuario) request.getAttribute("actual");
        return actual != null && "ADMIN".equals(actual.getRol());
    }

    private void denegar(HttpServletResponse response) throws IOException {
        response.sendError(HttpServletResponse.SC_FORBIDDEN,
                "Acceso denegado: solo el administrador puede modificar equipos");
    }

    private Equipo leerFormulario(HttpServletRequest request) {
        Equipo e = new Equipo();
        e.setCodigo(request.getParameter("codigo"));
        e.setNombre(request.getParameter("nombre"));
        e.setDescripcion(request.getParameter("descripcion"));
        e.setCategoria(request.getParameter("categoria"));
        e.setMarca(request.getParameter("marca"));
        e.setEstado(request.getParameter("estado"));
        return e;
    }

    private int parseId(String texto) {
        try {
            return Integer.parseInt(texto);
        } catch (NumberFormatException ex) {
            return -1;
        }
    }

    private void flash(HttpServletRequest request, String tipo, String mensaje) {
        HttpSession sesion = request.getSession();
        sesion.setAttribute("flashTipo", tipo);
        sesion.setAttribute("flashMensaje", mensaje);
    }
}
