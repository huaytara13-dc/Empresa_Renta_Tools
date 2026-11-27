package com.rentatools.repository;

import com.rentatools.model.Cliente;
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
public class ClienteRepositoryImpl implements ClienteRepository {

    private static final String COLUMNAS = "id, tipo_documento, numero_documento, nombres, apellidos, "
            + "telefono, correo, direccion, estado, fecha_creacion";

    private Connection con() throws SQLException {
        return ConexionBD.getInstancia().getConexion();
    }

    @Override
    public List<Cliente> listarTodos() throws SQLException {
        List<Cliente> lista = new ArrayList<>();
        String sql = "SELECT " + COLUMNAS + " FROM clientes ORDER BY id DESC";
        try (PreparedStatement ps = con().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(mapear(rs));
            }
        }
        return lista;
    }

    @Override
    public Pagina<Cliente> listarPagina(String q, int pagina, int tamano) throws SQLException {
        String[] cols = {"numero_documento", "nombres", "apellidos", "(nombres || ' ' || apellidos)", "telefono", "correo", "estado"};
        String where = Paginacion.where(q, cols);

        int total;
        try (PreparedStatement ps = con().prepareStatement("SELECT COUNT(*) FROM clientes" + where)) {
            Paginacion.asignar(ps, q, cols.length);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                total = rs.getInt(1);
            }
        }

        int actual = Paginacion.ajustar(pagina, total, tamano);
        List<Cliente> lista = new ArrayList<>();
        String sql = "SELECT " + COLUMNAS + " FROM clientes" + where
                + " ORDER BY id DESC LIMIT ? OFFSET ?";
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
        String sql = "SELECT estado, COUNT(*) FROM clientes GROUP BY estado";
        try (PreparedStatement ps = con().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                m.put(rs.getString(1), rs.getInt(2));
            }
        }
        return m;
    }

    @Override
    public Cliente buscarPorId(int id) throws SQLException {
        String sql = "SELECT " + COLUMNAS + " FROM clientes WHERE id = ?";
        try (PreparedStatement ps = con().prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        }
    }

    @Override
    public boolean existeDocumento(String numeroDocumento, int excluirId) throws SQLException {
        String sql = "SELECT 1 FROM clientes WHERE numero_documento = ? AND id <> ?";
        try (PreparedStatement ps = con().prepareStatement(sql)) {
            ps.setString(1, numeroDocumento);
            ps.setInt(2, excluirId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    @Override
    public int contarTodos() throws SQLException {
        try (PreparedStatement ps = con().prepareStatement("SELECT COUNT(*) FROM clientes");
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    @Override
    public void crear(Cliente c) throws SQLException {
        String sql = "INSERT INTO clientes (tipo_documento, numero_documento, nombres, apellidos, "
                   + "telefono, correo, direccion, estado) VALUES (?, ?, ?, ?, ?, ?, ?, ?) RETURNING id";
        try (PreparedStatement ps = con().prepareStatement(sql)) {
            ps.setString(1, c.getTipoDocumento());
            ps.setString(2, c.getNumeroDocumento());
            ps.setString(3, c.getNombres());
            ps.setString(4, c.getApellidos());
            ps.setString(5, c.getTelefono());
            ps.setString(6, c.getCorreo());
            ps.setString(7, c.getDireccion());
            ps.setString(8, c.getEstado());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    c.setId(rs.getInt(1));
                }
            }
        }
    }

    @Override
    public void actualizar(Cliente c) throws SQLException {
        String sql = "UPDATE clientes SET tipo_documento = ?, numero_documento = ?, nombres = ?, "
                   + "apellidos = ?, telefono = ?, correo = ?, direccion = ?, estado = ? WHERE id = ?";
        try (PreparedStatement ps = con().prepareStatement(sql)) {
            ps.setString(1, c.getTipoDocumento());
            ps.setString(2, c.getNumeroDocumento());
            ps.setString(3, c.getNombres());
            ps.setString(4, c.getApellidos());
            ps.setString(5, c.getTelefono());
            ps.setString(6, c.getCorreo());
            ps.setString(7, c.getDireccion());
            ps.setString(8, c.getEstado());
            ps.setInt(9, c.getId());
            ps.executeUpdate();
        }
    }

    @Override
    public void eliminar(int id) throws SQLException {
        try (PreparedStatement ps = con().prepareStatement("DELETE FROM clientes WHERE id = ?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    private Cliente mapear(ResultSet rs) throws SQLException {
        Cliente c = new Cliente();
        c.setId(rs.getInt("id"));
        c.setTipoDocumento(rs.getString("tipo_documento"));
        c.setNumeroDocumento(rs.getString("numero_documento"));
        c.setNombres(rs.getString("nombres"));
        c.setApellidos(rs.getString("apellidos"));
        c.setTelefono(rs.getString("telefono"));
        c.setCorreo(rs.getString("correo"));
        c.setDireccion(rs.getString("direccion"));
        c.setEstado(rs.getString("estado"));
        c.setFechaCreacion(rs.getObject("fecha_creacion", OffsetDateTime.class));
        return c;
    }
}
