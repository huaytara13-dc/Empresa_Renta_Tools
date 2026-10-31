package com.rentatools.controller;

import com.rentatools.model.Usuario;
import com.rentatools.service.UsuarioService;
import java.io.IOException;
import java.sql.SQLException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet(name = "InicioServlet", urlPatterns = {"/inicio"})
public class InicioServlet extends HttpServlet {

    private final UsuarioService servicio = new UsuarioService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        Usuario actual = (Usuario) request.getAttribute("actual"); // lo deja el filtro
        try {
            if (actual != null && "ADMIN".equals(actual.getRol())) {
                request.setAttribute("totalUsuarios", servicio.contarTodos());
                request.setAttribute("usuariosActivos", servicio.contarPorEstado("ACTIVO"));
                request.setAttribute("usuariosBloqueados", servicio.contarPorEstado("BLOQUEADO"));
            }
        } catch (SQLException e) {
            throw new ServletException(e);
        }
        request.getRequestDispatcher("/WEB-INF/vistas/inicio.jsp").forward(request, response);
    }
}
