package com.rentatools.controller;
 
import com.rentatools.repository.AuditoriaRepository;
import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
 
@WebServlet(name = "LogoutServlet", urlPatterns = {"/logout"})
public class LogoutServlet extends HttpServlet {
 
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession sesion = request.getSession(false);
        if (sesion != null) {
            Integer id = (Integer) sesion.getAttribute("idUsuario");
            String usuario = (String) sesion.getAttribute("usuario");
            if (id != null && usuario != null) {
                new AuditoriaRepository().registrarAcceso(id, usuario, "LOGOUT", request.getRemoteAddr());
            }
            sesion.invalidate();
        }
        response.sendRedirect("login.jsp");
    }
}
 