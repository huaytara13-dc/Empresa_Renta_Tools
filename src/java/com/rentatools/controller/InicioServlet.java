package com.rentatools.controller;

import com.rentatools.model.Alquiler;
import com.rentatools.model.Usuario;
import com.rentatools.service.AlquilerService;
import com.rentatools.service.ClienteService;
import com.rentatools.service.EquipoService;
import com.rentatools.service.UsuarioService;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet(name = "InicioServlet", urlPatterns = {"/inicio"})
public class InicioServlet extends HttpServlet {

    private final UsuarioService usuarioService = new UsuarioService();
    private final ClienteService clienteService = new ClienteService();
    private final EquipoService equipoService = new EquipoService();
    private final AlquilerService alquilerService = new AlquilerService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        Usuario actual = (Usuario) request.getAttribute("actual"); // lo deja el filtro
        try {
            request.setAttribute("totalClientes", clienteService.contarTodos());
            request.setAttribute("totalEquipos", equipoService.contarTodos());
            request.setAttribute("equiposDisponibles", equipoService.contarPorEstado("DISPONIBLE"));

            request.setAttribute("alquileresActivos", alquilerService.contarActivos());
            request.setAttribute("alquileresPorVencer", alquilerService.contarPorVencer());
            request.setAttribute("alquileresVencidos", alquilerService.contarVencidos());
            List<Alquiler> alertas = alquilerService.alertas();
            request.setAttribute("alertas", alertas.size() > 6 ? alertas.subList(0, 6) : alertas);
            request.setAttribute("totalAlertas", alertas.size());
            request.setAttribute("hoy", AlquilerService.hoy());

            if (actual != null && "ADMIN".equals(actual.getRol())) {
                request.setAttribute("totalUsuarios", usuarioService.contarTodos());
                request.setAttribute("usuariosActivos", usuarioService.contarPorEstado("ACTIVO"));
                request.setAttribute("usuariosBloqueados", usuarioService.contarPorEstado("BLOQUEADO"));
            }
        } catch (SQLException e) {
            throw new ServletException(e);
        }
        request.getRequestDispatcher("/WEB-INF/vistas/inicio.jsp").forward(request, response);
    }
}
