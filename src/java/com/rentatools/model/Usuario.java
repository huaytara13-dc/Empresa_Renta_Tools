package com.rentatools.model;

import java.time.OffsetDateTime;

public class Usuario {

    private int id;
    private String nombres;
    private String apellidos;
    private String correo;
    private String usuario;
    private String passwordHash;
    private String rol;
    private String estado;
    private int intentosFallidos;
    private OffsetDateTime fechaBloqueo;
    private OffsetDateTime fechaCreacion;

    public Usuario() { }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNombres() { return nombres; }
    public void setNombres(String nombres) { this.nombres = nombres; }

    public String getApellidos() { return apellidos; }
    public void setApellidos(String apellidos) { this.apellidos = apellidos; }

    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }

    public String getUsuario() { return usuario; }
    public void setUsuario(String usuario) { this.usuario = usuario; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public String getRol() { return rol; }
    public void setRol(String rol) { this.rol = rol; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public int getIntentosFallidos() { return intentosFallidos; }
    public void setIntentosFallidos(int intentosFallidos) { this.intentosFallidos = intentosFallidos; }

    public OffsetDateTime getFechaBloqueo() { return fechaBloqueo; }
    public void setFechaBloqueo(OffsetDateTime fechaBloqueo) { this.fechaBloqueo = fechaBloqueo; }

    public OffsetDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(OffsetDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }
}