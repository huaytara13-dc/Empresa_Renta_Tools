package com.rentatools.service;

import com.rentatools.model.Usuario;

public class ResultadoLogin {

    public enum Estado { OK, INCORRECTO, BLOQUEADO, INACTIVO }

    private final Estado estado;
    private final Usuario usuario;
    private final int intentosRestantes;

    private ResultadoLogin(Estado estado, Usuario usuario, int intentosRestantes) {
        this.estado = estado;
        this.usuario = usuario;
        this.intentosRestantes = intentosRestantes;
    }

    public static ResultadoLogin ok(Usuario u)          { return new ResultadoLogin(Estado.OK, u, 0); }
    public static ResultadoLogin incorrecto(int quedan) { return new ResultadoLogin(Estado.INCORRECTO, null, quedan); }
    public static ResultadoLogin bloqueado()            { return new ResultadoLogin(Estado.BLOQUEADO, null, 0); }
    public static ResultadoLogin inactivo()             { return new ResultadoLogin(Estado.INACTIVO, null, 0); }

    public Estado getEstado() { return estado; }
    public Usuario getUsuario() { return usuario; }
    public int getIntentosRestantes() { return intentosRestantes; }
}