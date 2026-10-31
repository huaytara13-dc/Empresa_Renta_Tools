package com.rentatools.repository;

import com.rentatools.model.Usuario;
import java.sql.SQLException;
import java.util.List;

/**
 * Patron REPOSITORY (Etapa 4).
 * Contrato que describe QUE operaciones existen sobre los usuarios,
 * sin decir COMO se guardan (SQL, JDBC, PostgreSQL...). El servicio depende
 * de esta interfaz y no de la base de datos.
 */
public interface UsuarioRepository {

    List<Usuario> listarTodos() throws SQLException;

    Usuario buscarPorId(int id) throws SQLException;

    Usuario buscarPorUsuario(String usuario) throws SQLException;

    boolean existeUsuario(String usuario, int excluirId) throws SQLException;

    boolean existeCorreo(String correo, int excluirId) throws SQLException;

    int contarTodos() throws SQLException;

    int contarPorEstado(String estado) throws SQLException;

    void crear(Usuario usuario) throws SQLException;

    void actualizar(Usuario usuario) throws SQLException;

    void cambiarPassword(int id, String passwordHash) throws SQLException;

    void eliminar(int id) throws SQLException;

    // --- Control de intentos del login ---
    int incrementarIntentos(int id) throws SQLException;

    void bloquear(int id) throws SQLException;

    void reiniciarIntentos(int id) throws SQLException;
}

