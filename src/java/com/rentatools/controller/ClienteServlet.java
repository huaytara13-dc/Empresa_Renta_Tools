package com.rentatools.controller;

import com.rentatools.model.Cliente;
import com.rentatools.model.Usuario;
import com.rentatools.service.ClienteService;
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
 * CRUD de clientes. Administrador y empleado pueden listar, crear y editar;
 * solo el administrador puede eliminar.
 */
@WebServlet(name = "ClienteServlet", urlPatterns = {"/clientes"})
public class ClienteServlet extends HttpServlet {

    private static final String VISTAS = "/WEB-INF/vistas/";
    private final ClienteService servicio = new ClienteService();

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
                case "nuevo": {
                    Cliente c = new Cliente();
                    c.setTipoDocumento("DNI");
                    c.setEstado("ACTIVO");
                    mostrarFormulario(request, response, c, "crear", null);
                    break;
                }
                case "editar": {
                    Cliente c = servicio.obtener(parseId(request.getParameter("id")));
                    if (c == null) {
                        flash(request, "danger", "El cliente no existe");
                        response.sendRedirect(request.getContextPath() + "/clientes");
                    } else {
                        mostrarFormulario(request, response, c, "editar", null);
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
                case "crear":
                    crear(request, response);
                    break;
                case "actualizar":
                    actualizar(request, response);
                    break;
                case "eliminar":
                    if (actual == null || !"ADMIN".equals(actual.getRol())) {
                        response.sendError(HttpServletResponse.SC_FORBIDDEN,
                                "Solo el administrador puede eliminar clientes");
                        return;
                    }
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

        Cliente c = leerFormulario(request);
        try {
            servicio.crear(c);
            flash(request, "success", "Cliente registrado correctamente");
            response.sendRedirect(request.getContextPath() + "/clientes");
        } catch (ValidacionException e) {
            mostrarFormulario(request, response, c, "crear", e.getErrores());
        }
    }

    private void actualizar(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException, SQLException {

        Cliente c = leerFormulario(request);
        c.setId(parseId(request.getParameter("id")));
        try {
            servicio.actualizar(c);
            flash(request, "success", "Cliente actualizado correctamente");
            response.sendRedirect(request.getContextPath() + "/clientes");
        } catch (ValidacionException e) {
            mostrarFormulario(request, response, c, "editar", e.getErrores());
        }
    }

    private void eliminar(HttpServletRequest request, HttpServletResponse response)
            throws IOException, SQLException {
        try {
            servicio.eliminar(parseId(request.getParameter("id")));
            flash(request, "success", "Cliente eliminado");
        } catch (ValidacionException e) {
            flash(request, "danger", e.getMessage());
        }
        response.sendRedirect(request.getContextPath() + "/clientes");
    }

    // ---------------------- vistas ----------------------
    private void listar(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException, SQLException {

        request.setAttribute("clientes", servicio.listar());
        request.setAttribute("csrf", CsrfUtil.token(request.getSession()));

        HttpSession sesion = request.getSession();
        request.setAttribute("flashTipo", sesion.getAttribute("flashTipo"));
        request.setAttribute("flashMensaje", sesion.getAttribute("flashMensaje"));
        sesion.removeAttribute("flashTipo");
        sesion.removeAttribute("flashMensaje");

        request.getRequestDispatcher(VISTAS + "clientes.jsp").forward(request, response);
    }

    private void mostrarFormulario(HttpServletRequest request, HttpServletResponse response,
                                   Cliente c, String modo, List<String> errores)
            throws ServletException, IOException {

        request.setAttribute("clienteForm", c);
        request.setAttribute("modo", modo);
        request.setAttribute("errores", errores);
        request.setAttribute("csrf", CsrfUtil.token(request.getSession()));
        request.getRequestDispatcher(VISTAS + "cliente-form.jsp").forward(request, response);
    }

    // ---------------------- auxiliares ----------------------
    private Cliente leerFormulario(HttpServletRequest request) {
        Cliente c = new Cliente();
        c.setTipoDocumento(request.getParameter("tipoDocumento"));
        c.setNumeroDocumento(request.getParameter("numeroDocumento"));
        c.setNombres(request.getParameter("nombres"));
        c.setApellidos(request.getParameter("apellidos"));
        c.setTelefono(request.getParameter("telefono"));
        c.setCorreo(request.getParameter("correo"));
        c.setDireccion(request.getParameter("direccion"));
        c.setEstado(request.getParameter("estado"));
        return c;
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
