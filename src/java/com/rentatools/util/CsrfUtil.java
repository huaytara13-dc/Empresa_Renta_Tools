package com.rentatools.util;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

/** Token anti-CSRF: cada formulario POST lo envia y el servidor lo compara con el de la sesion. */
public final class CsrfUtil {

    private static final String ATRIBUTO = "csrfToken";
    private static final SecureRandom RNG = new SecureRandom();

    private CsrfUtil() { }

    public static String token(HttpSession sesion) {
        String token = (String) sesion.getAttribute(ATRIBUTO);
        if (token == null) {
            byte[] bytes = new byte[32];
            RNG.nextBytes(bytes);
            token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
            sesion.setAttribute(ATRIBUTO, token);
        }
        return token;
    }

    public static boolean valido(HttpServletRequest request) {
        HttpSession sesion = request.getSession(false);
        if (sesion == null) {
            return false;
        }
        String esperado = (String) sesion.getAttribute(ATRIBUTO);
        String recibido = request.getParameter("csrf");
        if (esperado == null || recibido == null) {
            return false;
        }
        return MessageDigest.isEqual(esperado.getBytes(StandardCharsets.UTF_8),
                                     recibido.getBytes(StandardCharsets.UTF_8));
    }
}
