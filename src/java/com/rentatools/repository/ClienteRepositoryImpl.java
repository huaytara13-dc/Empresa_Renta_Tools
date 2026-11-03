package com.rentatools.repository;

import com.rentatools.model.Cliente;
import com.rentatools.util.ConexionBD;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

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
