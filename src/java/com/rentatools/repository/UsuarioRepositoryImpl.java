package com.rentatools.repository;

import com.rentatools.model.Usuario;
import com.rentatools.model.Pagina;
import com.rentatools.util.ConexionBD;
import com.rentatools.util.Paginacion;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Implementacion del Repository con JDBC y PostgreSQL.
 * Toda la SQL del modulo de usuarios vive aqui. La conexion viene del
 * Singleton y NO se cierra en este archivo (es compartida).
 */
public class UsuarioRepositoryImpl implements UsuarioRepository {

    private static final String COLUMNAS = "id, nombres, apellidos, correo, usuario, "
            + "password_hash, rol, estado, intentos_fallidos, fecha_bloqueo, fecha_creacion";

    private Connection con() throws SQLException {
        return ConexionBD.getInstancia().getConexion();
    }

    @Override
    public List<Usuario> listarTodos() throws SQLException {
        List<Usuario> lista = new ArrayList<>();
        String sql = "SELECT " + COLUMNAS + " FROM usuarios ORDER BY id";
        try (PreparedStatement ps = con().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(mapear(rs));
            }
        }
        return lista;
    }

    @Override
    public Pagina<Usuario> listarPagina(String q, int pagina, int tamano) throws SQLException {
        String[] cols = {"nombres", "apellidos", "usuario", "correo", "rol", "estado"};
        String where = Paginacion.where(q, cols);

        int total;
        try (PreparedStatement ps = con().prepareStatement("SELECT COUNT(*) FROM usuarios" + where)) {
            Paginacion.asignar(ps, q, cols.length);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                total = rs.getInt(1);
            }
        }

        int actual = Paginacion.ajustar(pagina, total, tamano);
        List<Usuario> lista = new ArrayList<>();
        String sql = "SELECT " + COLUMNAS + " FROM usuarios" + where
                + " ORDER BY id LIMIT ? OFFSET ?";
        try (PreparedStatement ps = con().prepareStatement(sql)) {
            int i = Paginacion.asignar(ps, q, cols.length);
            ps.setInt(i++, tamano);
            ps.setInt(i, (actual - 1) * tamano);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapear(rs));
                }
            }
        }
        return new Pagina<>(lista, actual, tamano, total, q);
    }

    @Override
    public Map<String, Integer> contarPorEstado() throws SQLException {
        Map<String, Integer> m = new LinkedHashMap<>();
        String sql = "SELECT estado, COUNT(*) FROM usuarios GROUP BY estado";
        try (PreparedStatement ps = con().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                m.put(rs.getString(1), rs.getInt(2));
            }
        }
        return m;
    }

    @Override
    public Usuario buscarPorId(int id) throws SQLException {
        String sql = "SELECT " + COLUMNAS + " FROM usuarios WHERE id = ?";
        try (PreparedStatement ps = con().prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        }
    }

    @Override
    public Usuario buscarPorUsuario(String usuario) throws SQLException {
        String sql = "SELECT " + COLUMNAS + " FROM usuarios WHERE usuario = ?";
        try (PreparedStatement ps = con().prepareStatement(sql)) {
            ps.setString(1, usuario);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        }
    }

    @Override
    public boolean existeUsuario(String usuario, int excluirId) throws SQLException {
        return existe("SELECT 1 FROM usuarios WHERE usuario = ? AND id <> ?", usuario, excluirId);
    }

    @Override
    public boolean existeCorreo(String correo, int excluirId) throws SQLException {
        return existe("SELECT 1 FROM usuarios WHERE correo = ? AND id <> ?", correo, excluirId);
    }

    @Override
    public int contarTodos() throws SQLException {
        try (PreparedStatement ps = con().prepareStatement("SELECT COUNT(*) FROM usuarios");
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    @Override
    public int contarPorEstado(String estado) throws SQLException {
        try (PreparedStatement ps = con().prepareStatement("SELECT COUNT(*) FROM usuarios WHERE estado = ?")) {
            ps.setString(1, estado);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    @Override
    public void crear(Usuario u) throws SQLException {
        String sql = "INSERT INTO usuarios (nombres, apellidos, correo, usuario, password_hash, rol, estado) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?) RETURNING id";
        try (PreparedStatement ps = con().prepareStatement(sql)) {
            ps.setString(1, u.getNombres());
            ps.setString(2, u.getApellidos());
            ps.setString(3, u.getCorreo());
            ps.setString(4, u.getUsuario());
            ps.setString(5, u.getPasswordHash());
            ps.setString(6, u.getRol());
            ps.setString(7, u.getEstado());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    u.setId(rs.getInt(1));
                }
            }
        }
    }

    @Override
    public void actualizar(Usuario u) throws SQLException {
        String sql = "UPDATE usuarios SET nombres = ?, apellidos = ?, correo = ?, usuario = ?, "
                   + "rol = ?, estado = ? WHERE id = ?";
        try (PreparedStatement ps = con().prepareStatement(sql)) {
            ps.setString(1, u.getNombres());
            ps.setString(2, u.getApellidos());
            ps.setString(3, u.getCorreo());
            ps.setString(4, u.getUsuario());
            ps.setString(5, u.getRol());
            ps.setString(6, u.getEstado());
            ps.setInt(7, u.getId());
            ps.executeUpdate();
        }
    }

    @Override
    public void cambiarPassword(int id, String passwordHash) throws SQLException {
        try (PreparedStatement ps = con().prepareStatement("UPDATE usuarios SET password_hash = ? WHERE id = ?")) {
            ps.setString(1, passwordHash);
            ps.setInt(2, id);
            ps.executeUpdate();
        }
    }

    @Override
    public void eliminar(int id) throws SQLException {
        try (PreparedStatement ps = con().prepareStatement("DELETE FROM usuarios WHERE id = ?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    @Override
    public int incrementarIntentos(int id) throws SQLException {
        String sql = "UPDATE usuarios SET intentos_fallidos = intentos_fallidos + 1 "
                   + "WHERE id = ? RETURNING intentos_fallidos";
        try (PreparedStatement ps = con().prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    @Override
    public void bloquear(int id) throws SQLException {
        String sql = "UPDATE usuarios SET estado = 'BLOQUEADO', fecha_bloqueo = CURRENT_TIMESTAMP WHERE id = ?";
        try (PreparedStatement ps = con().prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    @Override
    public void reiniciarIntentos(int id) throws SQLException {
        String sql = "UPDATE usuarios SET intentos_fallidos = 0, estado = 'ACTIVO', fecha_bloqueo = NULL WHERE id = ?";
        try (PreparedStatement ps = con().prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    // ---------- Auxiliares privados ----------
    private boolean existe(String sql, String valor, int excluirId) throws SQLException {
        try (PreparedStatement ps = con().prepareStatement(sql)) {
            ps.setString(1, valor);
            ps.setInt(2, excluirId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    private Usuario mapear(ResultSet rs) throws SQLException {
        Usuario u = new Usuario();
        u.setId(rs.getInt("id"));
        u.setNombres(rs.getString("nombres"));
        u.setApellidos(rs.getString("apellidos"));
        u.setCorreo(rs.getString("correo"));
        u.setUsuario(rs.getString("usuario"));
        u.setPasswordHash(rs.getString("password_hash"));
        u.setRol(rs.getString("rol"));
        u.setEstado(rs.getString("estado"));
        u.setIntentosFallidos(rs.getInt("intentos_fallidos"));
        u.setFechaBloqueo(rs.getObject("fecha_bloqueo", OffsetDateTime.class));
        u.setFechaCreacion(rs.getObject("fecha_creacion", OffsetDateTime.class));
        return u;
    }
}

