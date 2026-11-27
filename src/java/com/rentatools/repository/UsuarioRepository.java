package com.rentatools.repository;

import com.rentatools.model.Usuario;
import com.rentatools.model.Pagina;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * Patron REPOSITORY (Etapa 4).
 * Contrato que describe QUE operaciones existen sobre los usuarios,
 * sin decir COMO se guardan (SQL, JDBC, PostgreSQL...). El servicio depende
 * de esta interfaz y no de la base de datos.
 */
public interface UsuarioRepository {

    List<Usuario> listarTodos() throws SQLException;

    /** Una pagina del listado (con busqueda opcional). */
    Pagina<Usuario> listarPagina(String q, int pagina, int tamano) throws SQLException;

    /** Cantidad de registros por estado (para los indicadores). */
    Map<String, Integer> contarPorEstado() throws SQLException;

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

