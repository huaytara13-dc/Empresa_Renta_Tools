package com.rentatools.filter;

import com.rentatools.repository.AuditoriaRepository;
import java.io.IOException;
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
 * Etapa 7: registra en la tabla "auditoria" cada operacion que modifica datos
 * (crear, actualizar, eliminar, aprobar, anular...). Una operacion se considera exitosa
 * cuando el servlet termina redirigiendo (patron POST -> redirect). Si hay error de
 * validacion el servlet vuelve a mostrar el formulario y no se registra.
 */
@WebFilter(filterName = "AuditoriaFilter",
           urlPatterns = {"/usuarios", "/clientes", "/equipos", "/alquileres", "/devoluciones",
                          "/proveedores", "/solicitudes", "/comprobantes", "/guias"})
public class AuditoriaFilter implements Filter {

    private final AuditoriaRepository auditoria = new AuditoriaRepository();

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {

        chain.doFilter(req, res);

        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;
        if (!"POST".equalsIgnoreCase(request.getMethod())) {
            return;
        }
        int status = response.getStatus();
        if (status < 300 || status >= 400) {
            return;
        }
        HttpSession sesion = request.getSession(false);
        if (sesion == null) {
            return;
        }
        Integer id = (Integer) sesion.getAttribute("idUsuario");
        String usuario = (String) sesion.getAttribute("usuario");
        if (id == null || usuario == null) {
            return;
        }
        String modulo = request.getServletPath().replace("/", "");
        String accion = request.getParameter("accion");
        if (accion == null || accion.isEmpty()) {
            accion = "guardar";
        }
        String idReg = request.getParameter("id");
        String detalle = (idReg != null && idReg.matches("\\d{1,10}")) ? "registro " + idReg : null;
        auditoria.registrarActividad(id, usuario, modulo, accion, detalle);
    }
}
