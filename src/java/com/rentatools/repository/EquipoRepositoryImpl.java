package com.rentatools.repository;

import com.rentatools.model.Equipo;
import com.rentatools.util.ConexionBD;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

/** Implementacion JDBC / PostgreSQL. La conexion viene del Singleton y no se cierra aqui. */
public class EquipoRepositoryImpl implements EquipoRepository {

    private static final String COLUMNAS = "id, codigo, nombre, descripcion, categoria, marca, "
            + "precio_dia, estado, fecha_creacion";

    private Connection con() throws SQLException {
        return ConexionBD.getInstancia().getConexion();
    }

    @Override
    public List<Equipo> listarTodos() throws SQLException {
        List<Equipo> lista = new ArrayList<>();
        String sql = "SELECT " + COLUMNAS + " FROM equipos ORDER BY codigo";
        try (PreparedStatement ps = con().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(mapear(rs));
            }
        }
        return lista;
    }

    @Override
    public Equipo buscarPorId(int id) throws SQLException {
        String sql = "SELECT " + COLUMNAS + " FROM equipos WHERE id = ?";
        try (PreparedStatement ps = con().prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        }
    }

    @Override
    public boolean existeCodigo(String codigo, int excluirId) throws SQLException {
        String sql = "SELECT 1 FROM equipos WHERE codigo = ? AND id <> ?";
        try (PreparedStatement ps = con().prepareStatement(sql)) {
            ps.setString(1, codigo);
            ps.setInt(2, excluirId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    @Override
    public int contarTodos() throws SQLException {
        try (PreparedStatement ps = con().prepareStatement("SELECT COUNT(*) FROM equipos");
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    @Override
    public int contarPorEstado(String estado) throws SQLException {
        try (PreparedStatement ps = con().prepareStatement("SELECT COUNT(*) FROM equipos WHERE estado = ?")) {
            ps.setString(1, estado);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    @Override
    public void crear(Equipo e) throws SQLException {
        String sql = "INSERT INTO equipos (codigo, nombre, descripcion, categoria, marca, precio_dia, estado) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?) RETURNING id";
        try (PreparedStatement ps = con().prepareStatement(sql)) {
            ps.setString(1, e.getCodigo());
            ps.setString(2, e.getNombre());
            ps.setString(3, e.getDescripcion());
            ps.setString(4, e.getCategoria());
            ps.setString(5, e.getMarca());
            ps.setBigDecimal(6, e.getPrecioDia());
            ps.setString(7, e.getEstado());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    e.setId(rs.getInt(1));
                }
            }
        }
    }

    @Override
    public void actualizar(Equipo e) throws SQLException {
        String sql = "UPDATE equipos SET codigo = ?, nombre = ?, descripcion = ?, categoria = ?, "
                   + "marca = ?, precio_dia = ?, estado = ? WHERE id = ?";
        try (PreparedStatement ps = con().prepareStatement(sql)) {
            ps.setString(1, e.getCodigo());
            ps.setString(2, e.getNombre());
            ps.setString(3, e.getDescripcion());
            ps.setString(4, e.getCategoria());
            ps.setString(5, e.getMarca());
            ps.setBigDecimal(6, e.getPrecioDia());
            ps.setString(7, e.getEstado());
            ps.setInt(8, e.getId());
            ps.executeUpdate();
        }
    }

    @Override
    public void eliminar(int id) throws SQLException {
        try (PreparedStatement ps = con().prepareStatement("DELETE FROM equipos WHERE id = ?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    private Equipo mapear(ResultSet rs) throws SQLException {
        Equipo e = new Equipo();
        e.setId(rs.getInt("id"));
        e.setCodigo(rs.getString("codigo"));
        e.setNombre(rs.getString("nombre"));
        e.setDescripcion(rs.getString("descripcion"));
        e.setCategoria(rs.getString("categoria"));
        e.setMarca(rs.getString("marca"));
        e.setPrecioDia(rs.getBigDecimal("precio_dia"));
        e.setEstado(rs.getString("estado"));
        e.setFechaCreacion(rs.getObject("fecha_creacion", OffsetDateTime.class));
        return e;
    }
}
