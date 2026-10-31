package com.rentatools.service;

import java.util.Collections;
import java.util.List;

/** Error de regla de negocio o de validacion, con uno o varios mensajes para el usuario. */
public class ValidacionException extends Exception {

    private final List<String> errores;

    public ValidacionException(String mensaje) {
        super(mensaje);
        this.errores = Collections.singletonList(mensaje);
    }

    public ValidacionException(List<String> errores) {
        super(String.join("; ", errores));
        this.errores = errores;
    }

    public List<String> getErrores() {
        return errores;
    }
}
