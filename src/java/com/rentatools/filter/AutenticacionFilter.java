package com.rentatools.filter;

import com.rentatools.model.Usuario;
import com.rentatools.service.UsuarioService;
import java.io.IOException;
import java.sql.SQLException;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/**
 * Protege las paginas internas (/inicio, /usuarios, /clientes, /equipos, /alquileres, /devoluciones, /reportes, /proveedores, /solicitudes, /comprobantes, /guias):
 *  - exige sesion iniciada;
 *  - vuelve a leer al usuario en la BD en cada peticion;
 *  - evita que el navegador guarde las paginas.
 * Al crear un modulo nuevo, se agrega su ruta a urlPatterns.
 */
@WebFilter(filterName = "AutenticacionFilter",
           urlPatterns = {"/inicio", "/usuarios", "/clientes", "/equipos", "/alquileres", "/devoluciones", "/reportes", "/proveedores", "/solicitudes",
                       "/comprobantes", "/guias"})
public class AutenticacionFilter implements Filter {

    private final UsuarioService servicio = new UsuarioService();

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;

        response.setHeader("Cache-Control", "no-store, no-cache, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        response.setHeader("Expires", "0");

        HttpSession sesion = request.getSession(false);
        Integer id = (sesion == null) ? null : (Integer) sesion.getAttribute("idUsuario");
        if (id == null) {
            response.sendRedirect(request.getContextPath() + "/login.jsp");
            return;
        }

        try {
            Usuario actual = servicio.obtener(id);
            if (actual == null || !"ACTIVO".equals(actual.getEstado())) {
                sesion.invalidate();
                response.sendRedirect(request.getContextPath() + "/login.jsp");
                return;
            }
            request.setAttribute("actual", actual);
        } catch (SQLException e) {
            throw new ServletException(e);
        }

        chain.doFilter(request, response);
    }
}
