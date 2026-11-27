package com.rentatools.controller;

import com.rentatools.model.Proveedor;
import com.rentatools.model.Pagina;
import com.rentatools.model.Usuario;
import com.rentatools.util.Paginacion;
import com.rentatools.service.ProveedorService;
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

/** CRUD de proveedores. Solo para el administrador (el control se hace en el servidor). */
@WebServlet(name = "ProveedorServlet", urlPatterns = {"/proveedores"})
public class ProveedorServlet extends HttpServlet {

    private static final String VISTAS = "/WEB-INF/vistas/";
    private final ProveedorService servicio = new ProveedorService();

    // ---------------------- GET ----------------------
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        if (!esAdmin(request)) {
            denegar(response);
            return;
        }
        String accion = request.getParameter("accion");
        if (accion == null) {
            accion = "listar";
        }

        try {
            switch (accion) {
                case "nuevo": {
                    Proveedor p = new Proveedor();
                    p.setEstado("ACTIVO");
                    mostrarFormulario(request, response, p, "crear", null);
                    break;
                }
                case "editar": {
                    Proveedor p = servicio.obtener(parseId(request.getParameter("id")));
                    if (p == null) {
                        flash(request, "danger", "El proveedor no existe");
                        response.sendRedirect(request.getContextPath() + "/proveedores");
                    } else {
                        mostrarFormulario(request, response, p, "editar", null);
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

        Proveedor p = leerFormulario(request);
        try {
            servicio.crear(p);
            flash(request, "success", "Proveedor registrado correctamente");
            response.sendRedirect(request.getContextPath() + "/proveedores");
        } catch (ValidacionException e) {
            mostrarFormulario(request, response, p, "crear", e.getErrores());
        }
    }

    private void actualizar(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException, SQLException {

        Proveedor p = leerFormulario(request);
        p.setId(parseId(request.getParameter("id")));
        try {
            servicio.actualizar(p);
            flash(request, "success", "Proveedor actualizado correctamente");
            response.sendRedirect(request.getContextPath() + "/proveedores");
        } catch (ValidacionException e) {
            mostrarFormulario(request, response, p, "editar", e.getErrores());
        }
    }

    private void eliminar(HttpServletRequest request, HttpServletResponse response)
            throws IOException, SQLException {
        try {
            servicio.eliminar(parseId(request.getParameter("id")));
            flash(request, "success", "Proveedor eliminado");
        } catch (ValidacionException e) {
            flash(request, "danger", e.getMessage());
        }
        response.sendRedirect(request.getContextPath() + "/proveedores");
    }

    // ---------------------- vistas ----------------------
    private void listar(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException, SQLException {

        String q = Paginacion.texto(request.getParameter("q"));
        int numPagina = Paginacion.entero(request.getParameter("pagina"), 1);
        Pagina<Proveedor> pag = servicio.listarPagina(q, numPagina);
        request.setAttribute("proveedores", pag.getItems());
        request.setAttribute("pag", pag);
        boolean vivo = "1".equals(request.getParameter("vivo"));   // busqueda en vivo: no recalcular indicadores
        request.setAttribute("conteos", vivo ? java.util.Collections.<String, Integer>emptyMap() : servicio.contarPorEstado());
        request.setAttribute("csrf", CsrfUtil.token(request.getSession()));

        HttpSession sesion = request.getSession();
        request.setAttribute("flashTipo", sesion.getAttribute("flashTipo"));
        request.setAttribute("flashMensaje", sesion.getAttribute("flashMensaje"));
        sesion.removeAttribute("flashTipo");
        sesion.removeAttribute("flashMensaje");

        request.getRequestDispatcher(VISTAS + "proveedores.jsp").forward(request, response);
    }

    private void mostrarFormulario(HttpServletRequest request, HttpServletResponse response,
                                   Proveedor p, String modo, List<String> errores)
            throws ServletException, IOException {

        request.setAttribute("proveedorForm", p);
        request.setAttribute("modo", modo);
        request.setAttribute("errores", errores);
        request.setAttribute("csrf", CsrfUtil.token(request.getSession()));
        request.getRequestDispatcher(VISTAS + "proveedor-form.jsp").forward(request, response);
    }

    // ---------------------- auxiliares ----------------------
    private boolean esAdmin(HttpServletRequest request) {
        Usuario actual = (Usuario) request.getAttribute("actual");
        return actual != null && "ADMIN".equals(actual.getRol());
    }

    private void denegar(HttpServletResponse response) throws IOException {
        response.sendError(HttpServletResponse.SC_FORBIDDEN, "Acceso denegado: solo el administrador");
    }

    private Proveedor leerFormulario(HttpServletRequest request) {
        Proveedor p = new Proveedor();
        p.setRuc(request.getParameter("ruc"));
        p.setRazonSocial(request.getParameter("razonSocial"));
        p.setContacto(request.getParameter("contacto"));
        p.setTelefono(request.getParameter("telefono"));
        p.setCorreo(request.getParameter("correo"));
        p.setDireccion(request.getParameter("direccion"));
        p.setRubro(request.getParameter("rubro"));
        p.setEstado(request.getParameter("estado"));
        return p;
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
