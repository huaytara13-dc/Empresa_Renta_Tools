package com.rentatools.repository;

import java.util.Map;
import java.util.LinkedHashMap;
import com.rentatools.util.Paginacion;
import com.rentatools.model.Pagina;
import com.rentatools.model.Comprobante;
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
public class ComprobanteRepositoryImpl implements ComprobanteRepository {

    private static final String COLUMNAS = "id, alquiler_id, tipo, serie, numero, fecha_emision, "
            + "cliente_documento, cliente_nombre, cliente_direccion, subtotal, igv, total, estado, fecha_creacion";

    private Connection con() throws SQLException {
        return ConexionBD.getInstancia().getConexion();
    }

    @Override
    public List<Comprobante> listarTodos() throws SQLException {
        List<Comprobante> lista = new ArrayList<>();
        try (PreparedStatement ps = con().prepareStatement(
                     "SELECT " + COLUMNAS + " FROM comprobantes ORDER BY id DESC");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(mapear(rs));
            }
        }
        return lista;
    }

    @Override
    public Pagina<Comprobante> listarPagina(String q, int pagina, int tamano) throws SQLException {
        String[] cols = {"CAST(id AS TEXT)", "serie", "CAST(numero AS TEXT)", "cliente_nombre", "cliente_documento", "tipo", "estado"};
        String where = Paginacion.where(q, cols);
        String base = ("SELECT " + COLUMNAS + " FROM comprobantes ");

        int total;
        try (PreparedStatement ps = con().prepareStatement("SELECT COUNT(*) FROM (" + base + where + ") t")) {
            Paginacion.asignar(ps, q, cols.length);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                total = rs.getInt(1);
            }
        }

        int actual = Paginacion.ajustar(pagina, total, tamano);
        List<Comprobante> lista = new ArrayList<>();
        String sql = base + where + " ORDER BY id DESC LIMIT ? OFFSET ?";
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
    public Map<String, java.math.BigDecimal> indicadores() throws SQLException {
        Map<String, java.math.BigDecimal> m = new LinkedHashMap<>();
        String sql = "SELECT COUNT(*) FILTER (WHERE estado = 'EMITIDO') AS emitidos, COUNT(*) FILTER (WHERE estado = 'EMITIDO' AND tipo = 'BOLETA') AS boletas, COUNT(*) FILTER (WHERE estado = 'EMITIDO' AND tipo = 'FACTURA') AS facturas, COALESCE(SUM(total) FILTER (WHERE estado = 'EMITIDO'), 0) AS facturado FROM comprobantes";
        try (PreparedStatement ps = con().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                m.put("emitidos", rs.getBigDecimal("emitidos"));
                m.put("boletas", rs.getBigDecimal("boletas"));
                m.put("facturas", rs.getBigDecimal("facturas"));
                m.put("facturado", rs.getBigDecimal("facturado"));
            }
        }
        return m;
    }

    @Override
    public Comprobante buscarPorId(int id) throws SQLException {
        return buscar("SELECT " + COLUMNAS + " FROM comprobantes WHERE id = ?", id);
    }

    @Override
    public Comprobante buscarPorAlquiler(int alquilerId) throws SQLException {
        return buscar("SELECT " + COLUMNAS + " FROM comprobantes WHERE alquiler_id = ?", alquilerId);
    }

    private Comprobante buscar(String sql, int parametro) throws SQLException {
        try (PreparedStatement ps = con().prepareStatement(sql)) {
            ps.setInt(1, parametro);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        }
    }

    @Override
    public void emitir(Comprobante c) throws SQLException {
        try (Connection con = ConexionBD.getInstancia().nuevaConexion()) {
            con.setAutoCommit(false);
            try {
                Object[] siguiente = Correlativos.siguiente(con, c.getTipo());
                c.setSerie((String) siguiente[0]);
                c.setNumero((Integer) siguiente[1]);

                String sql = "INSERT INTO comprobantes (alquiler_id, tipo, serie, numero, fecha_emision, "
                           + "cliente_documento, cliente_nombre, cliente_direccion, subtotal, igv, total) "
                           + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) RETURNING id";
                try (PreparedStatement ps = con.prepareStatement(sql)) {
                    ps.setInt(1, c.getAlquilerId());
                    ps.setString(2, c.getTipo());
                    ps.setString(3, c.getSerie());
                    ps.setInt(4, c.getNumero());
                    ps.setObject(5, c.getFechaEmision());
                    ps.setString(6, c.getClienteDocumento());
                    ps.setString(7, c.getClienteNombre());
                    ps.setString(8, c.getClienteDireccion());
                    ps.setBigDecimal(9, c.getSubtotal());
                    ps.setBigDecimal(10, c.getIgv());
                    ps.setBigDecimal(11, c.getTotal());
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            c.setId(rs.getInt(1));
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

    private Comprobante mapear(ResultSet rs) throws SQLException {
        Comprobante c = new Comprobante();
        c.setId(rs.getInt("id"));
        c.setAlquilerId(rs.getInt("alquiler_id"));
        c.setTipo(rs.getString("tipo"));
        c.setSerie(rs.getString("serie"));
        c.setNumero(rs.getInt("numero"));
        c.setFechaEmision(rs.getObject("fecha_emision", LocalDate.class));
        c.setClienteDocumento(rs.getString("cliente_documento"));
        c.setClienteNombre(rs.getString("cliente_nombre"));
        c.setClienteDireccion(rs.getString("cliente_direccion"));
        c.setSubtotal(rs.getBigDecimal("subtotal"));
        c.setIgv(rs.getBigDecimal("igv"));
        c.setTotal(rs.getBigDecimal("total"));
        c.setEstado(rs.getString("estado"));
        c.setFechaCreacion(rs.getObject("fecha_creacion", OffsetDateTime.class));
        return c;
    }
}
