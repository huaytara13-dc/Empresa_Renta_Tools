package com.rentatools.repository;

import com.rentatools.model.Proveedor;
import com.rentatools.util.ConexionBD;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

/** Implementacion JDBC / PostgreSQL. La conexion viene del Singleton y no se cierra aqui. */
public class ProveedorRepositoryImpl implements ProveedorRepository {

    private static final String COLUMNAS = "id, ruc, razon_social, contacto, telefono, correo, "
            + "direccion, rubro, estado, fecha_creacion";

    private Connection con() throws SQLException {
        return ConexionBD.getInstancia().getConexion();
    }

    @Override
    public List<Proveedor> listarTodos() throws SQLException {
        List<Proveedor> lista = new ArrayList<>();
        String sql = "SELECT " + COLUMNAS + " FROM proveedores ORDER BY razon_social";
        try (PreparedStatement ps = con().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(mapear(rs));
            }
        }
        return lista;
    }

    @Override
    public Proveedor buscarPorId(int id) throws SQLException {
        String sql = "SELECT " + COLUMNAS + " FROM proveedores WHERE id = ?";
        try (PreparedStatement ps = con().prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        }
    }

    @Override
    public boolean existeRuc(String ruc, int excluirId) throws SQLException {
        String sql = "SELECT 1 FROM proveedores WHERE ruc = ? AND id <> ?";
        try (PreparedStatement ps = con().prepareStatement(sql)) {
            ps.setString(1, ruc);
            ps.setInt(2, excluirId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    @Override
    public int contarTodos() throws SQLException {
        try (PreparedStatement ps = con().prepareStatement("SELECT COUNT(*) FROM proveedores");
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    @Override
    public void crear(Proveedor p) throws SQLException {
        String sql = "INSERT INTO proveedores (ruc, razon_social, contacto, telefono, correo, direccion, rubro, estado) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?, ?) RETURNING id";
        try (PreparedStatement ps = con().prepareStatement(sql)) {
            ps.setString(1, p.getRuc());
            ps.setString(2, p.getRazonSocial());
            ps.setString(3, p.getContacto());
            ps.setString(4, p.getTelefono());
            ps.setString(5, p.getCorreo());
            ps.setString(6, p.getDireccion());
            ps.setString(7, p.getRubro());
            ps.setString(8, p.getEstado());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    p.setId(rs.getInt(1));
                }
            }
        }
    }

    @Override
    public void actualizar(Proveedor p) throws SQLException {
        String sql = "UPDATE proveedores SET ruc = ?, razon_social = ?, contacto = ?, telefono = ?, "
                   + "correo = ?, direccion = ?, rubro = ?, estado = ? WHERE id = ?";
        try (PreparedStatement ps = con().prepareStatement(sql)) {
            ps.setString(1, p.getRuc());
            ps.setString(2, p.getRazonSocial());
            ps.setString(3, p.getContacto());
            ps.setString(4, p.getTelefono());
            ps.setString(5, p.getCorreo());
            ps.setString(6, p.getDireccion());
            ps.setString(7, p.getRubro());
            ps.setString(8, p.getEstado());
            ps.setInt(9, p.getId());
            ps.executeUpdate();
        }
    }

    @Override
    public void eliminar(int id) throws SQLException {
        try (PreparedStatement ps = con().prepareStatement("DELETE FROM proveedores WHERE id = ?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    private Proveedor mapear(ResultSet rs) throws SQLException {
        Proveedor p = new Proveedor();
        p.setId(rs.getInt("id"));
        p.setRuc(rs.getString("ruc"));
        p.setRazonSocial(rs.getString("razon_social"));
        p.setContacto(rs.getString("contacto"));
        p.setTelefono(rs.getString("telefono"));
        p.setCorreo(rs.getString("correo"));
        p.setDireccion(rs.getString("direccion"));
        p.setRubro(rs.getString("rubro"));
        p.setEstado(rs.getString("estado"));
        p.setFechaCreacion(rs.getObject("fecha_creacion", OffsetDateTime.class));
        return p;
    }
}
