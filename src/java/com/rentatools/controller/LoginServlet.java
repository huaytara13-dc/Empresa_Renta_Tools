package com.rentatools.controller;

import com.rentatools.repository.AuditoriaRepository;
import com.rentatools.service.ResultadoLogin;
import com.rentatools.service.UsuarioService;
import java.io.IOException;
import java.sql.SQLException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet(name = "LoginServlet", urlPatterns = {"/login", "/LoginServlet"})
public class LoginServlet extends HttpServlet {

    private final UsuarioService usuarioService = new UsuarioService();
    private final AuditoriaRepository auditoria = new AuditoriaRepository();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.sendRedirect(request.getContextPath() + "/login.jsp");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String usuario = request.getParameter("usuario");
        String password = request.getParameter("password");

        if (usuario == null || password == null
                || usuario.trim().isEmpty() || password.isEmpty()) {
            mostrarError(request, response, "Ingrese usuario y contraseña");
            return;
        }

        try {
            ResultadoLogin r = usuarioService.autenticar(usuario.trim(), password);

            switch (r.getEstado()) {
                case OK:                  
                    HttpSession sesion = request.getSession();
                    request.changeSessionId();
                    sesion.setAttribute("idUsuario", r.getUsuario().getId());
                    sesion.setAttribute("usuario", r.getUsuario().getUsuario());
                    sesion.setAttribute("rol", r.getUsuario().getRol());
                    auditoria.registrarAcceso(r.getUsuario().getId(), r.getUsuario().getUsuario(),
                            "LOGIN_OK", ip(request));
                    response.sendRedirect(request.getContextPath() + "/inicio");
                    break;

                case BLOQUEADO:
                    auditoria.registrarAcceso(null, usuario.trim(), "BLOQUEO", ip(request));
                    response.sendRedirect(request.getContextPath() + "/bloqueado.jsp");
                    break;

                case INACTIVO:
                    auditoria.registrarAcceso(null, usuario.trim(), "LOGIN_FALLIDO", ip(request));
                    mostrarError(request, response, "Usuario inactivo. Contacte al administrador");
                    break;

                default: // INCORRECTO
                    auditoria.registrarAcceso(null, usuario.trim(), "LOGIN_FALLIDO", ip(request));
                    String msg = "Credenciales incorrectas";
                    if (r.getIntentosRestantes() > 0) {
                        msg += ". Intentos restantes: " + r.getIntentosRestantes();
                    }
                    mostrarError(request, response, msg);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            mostrarError(request, response, "No se pudo conectar con la base de datos");
        }
    }

    /** IP del cliente (detras de Render llega en X-Forwarded-For). */
    private String ip(HttpServletRequest request) {
        String f = request.getHeader("X-Forwarded-For");
        if (f != null && !f.isEmpty()) {
            return f.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private void mostrarError(HttpServletRequest request, HttpServletResponse response, String mensaje)
            throws ServletException, IOException {
        request.setAttribute("error", mensaje);
        request.getRequestDispatcher("login.jsp").forward(request, response);
    }
}