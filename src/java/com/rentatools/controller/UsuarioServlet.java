package com.rentatools.controller;

import com.rentatools.model.Pagina;
import com.rentatools.model.Usuario;
import com.rentatools.util.Paginacion;
import com.rentatools.service.UsuarioService;
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

/** CRUD de usuarios. Solo el rol ADMIN puede usarlo. */
@WebServlet(name = "UsuarioServlet", urlPatterns = {"/usuarios"})
public class UsuarioServlet extends HttpServlet {

    private static final String VISTAS = "/WEB-INF/vistas/";
    private final UsuarioService servicio = new UsuarioService();

    // ---------------------- GET: pantallas ----------------------
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        if (exigirAdmin(request, response) == null) {
            return;
        }
        String accion = request.getParameter("accion");
        if (accion == null) {
            accion = "listar";
        }

        try {
            switch (accion) {
                case "nuevo": {
                    Usuario u = new Usuario();
                    u.setRol("EMPLEADO");
                    u.setEstado("ACTIVO");
                    mostrarFormulario(request, response, u, "crear", null);
                    break;
                }
                case "editar": {
                    Usuario u = servicio.obtener(parseId(request.getParameter("id")));
                    if (u == null) {
                        flash(request, "danger", "El usuario no existe");
                        response.sendRedirect(request.getContextPath() + "/usuarios");
                    } else {
                        mostrarFormulario(request, response, u, "editar", null);
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

    // ---------------------- POST: acciones ----------------------
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        Usuario actual = exigirAdmin(request, response);
        if (actual == null) {
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
                    actualizar(request, response, actual);
                    break;
                case "eliminar":
                    eliminar(request, response, actual);
                    break;
                case "desbloquear":
                    desbloquear(request, response);
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

        Usuario u = leerFormulario(request);
        try {
            servicio.crear(u, request.getParameter("password"), request.getParameter("password2"));
            flash(request, "success", "Usuario creado correctamente");
            response.sendRedirect(request.getContextPath() + "/usuarios");
        } catch (ValidacionException e) {
            mostrarFormulario(request, response, u, "crear", e.getErrores());
        }
    }

    private void actualizar(HttpServletRequest request, HttpServletResponse response, Usuario actual)
            throws ServletException, IOException, SQLException {

        Usuario u = leerFormulario(request);
        u.setId(parseId(request.getParameter("id")));
        try {
            servicio.actualizar(u, request.getParameter("password"),
                                request.getParameter("password2"), actual.getId());
            flash(request, "success", "Usuario actualizado correctamente");
            response.sendRedirect(request.getContextPath() + "/usuarios");
        } catch (ValidacionException e) {
            mostrarFormulario(request, response, u, "editar", e.getErrores());
        }
    }

    private void eliminar(HttpServletRequest request, HttpServletResponse response, Usuario actual)
            throws IOException, SQLException {
        try {
            servicio.eliminar(parseId(request.getParameter("id")), actual.getId());
            flash(request, "success", "Usuario eliminado");
        } catch (ValidacionException e) {
            flash(request, "danger", e.getMessage());
        }
        response.sendRedirect(request.getContextPath() + "/usuarios");
    }

    private void desbloquear(HttpServletRequest request, HttpServletResponse response)
            throws IOException, SQLException {
        try {
            servicio.desbloquear(parseId(request.getParameter("id")));
            flash(request, "success", "Usuario desbloqueado");
        } catch (ValidacionException e) {
            flash(request, "danger", e.getMessage());
        }
        response.sendRedirect(request.getContextPath() + "/usuarios");
    }

    // ---------------------- vistas ----------------------
    private void listar(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException, SQLException {

        String q = Paginacion.texto(request.getParameter("q"));
        int numPagina = Paginacion.entero(request.getParameter("pagina"), 1);
        Pagina<Usuario> pag = servicio.listarPagina(q, numPagina);
        request.setAttribute("usuarios", pag.getItems());
        request.setAttribute("pag", pag);
        boolean vivo = "1".equals(request.getParameter("vivo"));   // busqueda en vivo: no recalcular indicadores
        request.setAttribute("conteos", vivo ? java.util.Collections.<String, Integer>emptyMap() : servicio.contarPorEstado());
        request.setAttribute("csrf", CsrfUtil.token(request.getSession()));

        HttpSession sesion = request.getSession();
        request.setAttribute("flashTipo", sesion.getAttribute("flashTipo"));
        request.setAttribute("flashMensaje", sesion.getAttribute("flashMensaje"));
        sesion.removeAttribute("flashTipo");
        sesion.removeAttribute("flashMensaje");

        request.getRequestDispatcher(VISTAS + "usuarios.jsp").forward(request, response);
    }

    private void mostrarFormulario(HttpServletRequest request, HttpServletResponse response,
                                   Usuario u, String modo, List<String> errores)
            throws ServletException, IOException {

        request.setAttribute("usuarioForm", u);
        request.setAttribute("modo", modo);
        request.setAttribute("errores", errores);
        request.setAttribute("csrf", CsrfUtil.token(request.getSession()));
        request.getRequestDispatcher(VISTAS + "usuario-form.jsp").forward(request, response);
    }

    // ---------------------- auxiliares ----------------------
    /** Solo ADMIN. El filtro ya dejo al usuario actual (leido de la BD) en el request. */
    private Usuario exigirAdmin(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        Usuario actual = (Usuario) request.getAttribute("actual");
        if (actual == null || !"ADMIN".equals(actual.getRol())) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Acceso denegado: solo el administrador");
            return null;
        }
        return actual;
    }

    private Usuario leerFormulario(HttpServletRequest request) {
        Usuario u = new Usuario();
        u.setNombres(request.getParameter("nombres"));
        u.setApellidos(request.getParameter("apellidos"));
        u.setCorreo(request.getParameter("correo"));
        u.setUsuario(request.getParameter("usuario"));
        u.setRol(request.getParameter("rol"));
        u.setEstado(request.getParameter("estado"));
        return u;
    }

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
}

