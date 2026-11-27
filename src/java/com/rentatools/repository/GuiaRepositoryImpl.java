package com.rentatools.repository;

import java.util.Map;
import java.util.LinkedHashMap;
import com.rentatools.util.Paginacion;
import com.rentatools.model.Pagina;
import com.rentatools.model.GuiaEntrega;
import com.rentatools.util.ConexionBD;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

/** Implementacion JDBC / PostgreSQL. La emision usa una conexion propia con transaccion. */
public class GuiaRepositoryImpl implements GuiaRepository {

    private static final String SELECT_BASE =
            "SELECT g.id, g.alquiler_id, g.serie, g.numero, g.fecha_emision, g.direccion_entrega, g.estado, "
          + "g.recibido_por, g.documento_receptor, g.fecha_entrega, g.fecha_creacion, "
          + "TRIM(c.nombres || ' ' || c.apellidos) AS cliente_nombre, "
          + "c.tipo_documento || ' ' || c.numero_documento AS cliente_documento "
          + "FROM guias_entrega g JOIN alquileres a ON a.id = g.alquiler_id "
          + "JOIN clientes c ON c.id = a.cliente_id ";

    private Connection con() throws SQLException {
        return ConexionBD.getInstancia().getConexion();
    }

    @Override
    public List<GuiaEntrega> listarTodos() throws SQLException {
        List<GuiaEntrega> lista = new ArrayList<>();
        try (PreparedStatement ps = con().prepareStatement(SELECT_BASE + "ORDER BY g.id DESC");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(mapear(rs));
            }
        }
        return lista;
    }

    @Override
    public Pagina<GuiaEntrega> listarPagina(String q, int pagina, int tamano) throws SQLException {
        String[] cols = {"CAST(g.id AS TEXT)", "g.serie", "CAST(g.numero AS TEXT)", "TRIM(c.nombres || ' ' || c.apellidos)", "c.numero_documento", "g.estado"};
        String where = Paginacion.where(q, cols);
        String base = SELECT_BASE;

        int total;
        try (PreparedStatement ps = con().prepareStatement("SELECT COUNT(*) FROM (" + base + where + ") t")) {
            Paginacion.asignar(ps, q, cols.length);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                total = rs.getInt(1);
            }
        }

        int actual = Paginacion.ajustar(pagina, total, tamano);
        List<GuiaEntrega> lista = new ArrayList<>();
        String sql = base + where + " ORDER BY g.id DESC LIMIT ? OFFSET ?";
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
        String sql = "SELECT estado, COUNT(*) FROM guias_entrega GROUP BY estado";
        try (PreparedStatement ps = con().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                m.put(rs.getString(1), rs.getInt(2));
            }
        }
        return m;
    }

    @Override
    public GuiaEntrega buscarPorId(int id) throws SQLException {
        return buscar(SELECT_BASE + "WHERE g.id = ?", id);
    }

    @Override
    public GuiaEntrega buscarPorAlquiler(int alquilerId) throws SQLException {
        return buscar(SELECT_BASE + "WHERE g.alquiler_id = ?", alquilerId);
    }

    private GuiaEntrega buscar(String sql, int parametro) throws SQLException {
        try (PreparedStatement ps = con().prepareStatement(sql)) {
            ps.setInt(1, parametro);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        }
    }

    @Override
    public void emitir(GuiaEntrega g) throws SQLException {
        try (Connection con = ConexionBD.getInstancia().nuevaConexion()) {
            con.setAutoCommit(false);
            try {
                Object[] siguiente = Correlativos.siguiente(con, "GUIA");
                g.setSerie((String) siguiente[0]);
                g.setNumero((Integer) siguiente[1]);

                String sql = "INSERT INTO guias_entrega (alquiler_id, serie, numero, fecha_emision, direccion_entrega) "
                           + "VALUES (?, ?, ?, ?, ?) RETURNING id";
                try (PreparedStatement ps = con.prepareStatement(sql)) {
                    ps.setInt(1, g.getAlquilerId());
                    ps.setString(2, g.getSerie());
                    ps.setInt(3, g.getNumero());
                    ps.setObject(4, g.getFechaEmision());
                    ps.setString(5, g.getDireccionEntrega());
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            g.setId(rs.getInt(1));
                        }
                    }
                }
                con.commit();
            } catch (SQLException ex) {
                con.rollback();
                throw ex;
            }
        }
    }

    @Override
    public boolean registrarConformidad(int id, String recibidoPor, String documentoReceptor) throws SQLException {
        String sql = "UPDATE guias_entrega SET estado = 'ENTREGADA', recibido_por = ?, documento_receptor = ?, "
                   + "fecha_entrega = CURRENT_TIMESTAMP WHERE id = ? AND estado = 'EMITIDA'";
        try (PreparedStatement ps = con().prepareStatement(sql)) {
            ps.setString(1, recibidoPor);
            ps.setString(2, documentoReceptor);
            ps.setInt(3, id);
            return ps.executeUpdate() > 0;
        }
    }

    private GuiaEntrega mapear(ResultSet rs) throws SQLException {
        GuiaEntrega g = new GuiaEntrega();
        g.setId(rs.getInt("id"));
        g.setAlquilerId(rs.getInt("alquiler_id"));
        g.setSerie(rs.getString("serie"));
        g.setNumero(rs.getInt("numero"));
        g.setFechaEmision(rs.getObject("fecha_emision", LocalDate.class));
        g.setDireccionEntrega(rs.getString("direccion_entrega"));
        g.setEstado(rs.getString("estado"));
        g.setRecibidoPor(rs.getString("recibido_por"));
        g.setDocumentoReceptor(rs.getString("documento_receptor"));
        g.setFechaEntrega(rs.getObject("fecha_entrega", OffsetDateTime.class));
        g.setFechaCreacion(rs.getObject("fecha_creacion", OffsetDateTime.class));
        g.setClienteNombre(rs.getString("cliente_nombre"));
        g.setClienteDocumento(rs.getString("cliente_documento"));
        return g;
    }
}
