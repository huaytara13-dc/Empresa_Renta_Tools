package com.rentatools.repository;

import com.rentatools.model.Equipo;
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
    public Pagina<Equipo> listarPagina(String q, int pagina, int tamano) throws SQLException {
        String[] cols = {"codigo", "nombre", "categoria", "marca", "estado"};
        String where = Paginacion.where(q, cols);

        int total;
        try (PreparedStatement ps = con().prepareStatement("SELECT COUNT(*) FROM equipos" + where)) {
            Paginacion.asignar(ps, q, cols.length);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                total = rs.getInt(1);
            }
        }

        int actual = Paginacion.ajustar(pagina, total, tamano);
        List<Equipo> lista = new ArrayList<>();
        String sql = "SELECT " + COLUMNAS + " FROM equipos" + where
                + " ORDER BY codigo LIMIT ? OFFSET ?";
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
        String sql = "SELECT estado, COUNT(*) FROM equipos GROUP BY estado";
        try (PreparedStatement ps = con().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                m.put(rs.getString(1), rs.getInt(2));
            }
        }
        return m;
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
