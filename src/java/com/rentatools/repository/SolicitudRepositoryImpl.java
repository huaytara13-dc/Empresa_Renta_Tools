package com.rentatools.repository;

import com.rentatools.model.DetalleSolicitud;
import com.rentatools.model.Solicitud;
import com.rentatools.util.ConexionBD;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementacion JDBC / PostgreSQL. Las consultas y los cambios de una sola sentencia usan la conexion
 * compartida del Singleton; crear y actualizar (varias tablas) usan una conexion propia con transaccion.
 */
public class SolicitudRepositoryImpl implements SolicitudRepository {

    private static final String SELECT_BASE =
            "SELECT s.id, s.cliente_id, s.usuario_id, s.dias_solicitados, s.subtotal, s.igv, s.garantia, "
          + "s.total, s.estado, s.observaciones, s.aprobado_por, s.fecha_resolucion, s.fecha_creacion, "
          + "c.nombres AS cli_nombres, c.apellidos AS cli_apellidos, c.tipo_documento, c.numero_documento, "
          + "u.usuario AS usuario_nombre, ua.usuario AS aprobador_nombre, "
          + "(SELECT COUNT(*) FROM detalle_solicitud d WHERE d.solicitud_id = s.id) AS cant_equipos, "
          + "(SELECT al.id FROM alquileres al WHERE al.solicitud_id = s.id) AS alquiler_id "
          + "FROM solicitudes_alquiler s "
          + "JOIN clientes c ON c.id = s.cliente_id "
          + "JOIN usuarios u ON u.id = s.usuario_id "
          + "LEFT JOIN usuarios ua ON ua.id = s.aprobado_por ";

    private Connection con() throws SQLException {
        return ConexionBD.getInstancia().getConexion();
    }

    @Override
    public List<Solicitud> listarTodos() throws SQLException {
        List<Solicitud> lista = new ArrayList<>();
        try (PreparedStatement ps = con().prepareStatement(SELECT_BASE + "ORDER BY s.id DESC");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(mapear(rs));
            }
        }
        return lista;
    }

    @Override
    public Solicitud buscarPorId(int id) throws SQLException {
        Solicitud s = null;
        try (PreparedStatement ps = con().prepareStatement(SELECT_BASE + "WHERE s.id = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    s = mapear(rs);
                }
            }
        }
        if (s != null) {
            String sql = "SELECT d.id, d.solicitud_id, d.equipo_id, d.dias, d.precio_dia, d.subtotal, "
                       + "e.codigo, e.nombre, e.estado FROM detalle_solicitud d "
                       + "JOIN equipos e ON e.id = d.equipo_id WHERE d.solicitud_id = ? ORDER BY d.id";
            try (PreparedStatement ps = con().prepareStatement(sql)) {
                ps.setInt(1, id);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        DetalleSolicitud d = new DetalleSolicitud();
                        d.setId(rs.getInt("id"));
                        d.setSolicitudId(rs.getInt("solicitud_id"));
                        d.setEquipoId(rs.getInt("equipo_id"));
                        d.setDias(rs.getInt("dias"));
                        d.setPrecioDia(rs.getBigDecimal("precio_dia"));
                        d.setSubtotal(rs.getBigDecimal("subtotal"));
                        d.setEquipoCodigo(rs.getString("codigo"));
                        d.setEquipoNombre(rs.getString("nombre"));
                        d.setEquipoEstado(rs.getString("estado"));
                        s.getDetalles().add(d);
                    }
                }
            }
        }
        return s;
    }

    @Override
    public int contarPorEstado(String estado) throws SQLException {
        try (PreparedStatement ps = con().prepareStatement("SELECT COUNT(*) FROM solicitudes_alquiler WHERE estado = ?")) {
            ps.setString(1, estado);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    // ---------------------- escritura ----------------------
    @Override
    public void crear(Solicitud s) throws SQLException {
        try (Connection con = ConexionBD.getInstancia().nuevaConexion()) {
            con.setAutoCommit(false);
            try {
                String sql = "INSERT INTO solicitudes_alquiler (cliente_id, usuario_id, dias_solicitados, subtotal, "
                           + "igv, garantia, total, estado, observaciones) "
                           + "VALUES (?, ?, ?, ?, ?, ?, ?, 'PENDIENTE', ?) RETURNING id";
                try (PreparedStatement ps = con.prepareStatement(sql)) {
                    ps.setInt(1, s.getClienteId());
                    ps.setInt(2, s.getUsuarioId());
                    ps.setInt(3, s.getDiasSolicitados());
                    ps.setBigDecimal(4, s.getSubtotal());
                    ps.setBigDecimal(5, s.getIgv());
                    ps.setBigDecimal(6, s.getGarantia());
                    ps.setBigDecimal(7, s.getTotal());
                    ps.setString(8, s.getObservaciones());
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            s.setId(rs.getInt(1));
                        }
                    }
                }
                insertarDetalle(con, s);
                con.commit();
            } catch (SQLException ex) {
                con.rollback();
                throw ex;
            }
        }
    }

    @Override
    public boolean actualizar(Solicitud s) throws SQLException {
        try (Connection con = ConexionBD.getInstancia().nuevaConexion()) {
            con.setAutoCommit(false);
            try {
                int filas;
                String sql = "UPDATE solicitudes_alquiler SET cliente_id = ?, dias_solicitados = ?, subtotal = ?, "
                           + "igv = ?, garantia = ?, total = ?, observaciones = ? "
                           + "WHERE id = ? AND estado = 'PENDIENTE'";
                try (PreparedStatement ps = con.prepareStatement(sql)) {
                    ps.setInt(1, s.getClienteId());
                    ps.setInt(2, s.getDiasSolicitados());
                    ps.setBigDecimal(3, s.getSubtotal());
                    ps.setBigDecimal(4, s.getIgv());
                    ps.setBigDecimal(5, s.getGarantia());
                    ps.setBigDecimal(6, s.getTotal());
                    ps.setString(7, s.getObservaciones());
                    ps.setInt(8, s.getId());
                    filas = ps.executeUpdate();
                }
                if (filas == 0) {
                    con.rollback();
                    return false;
                }
                try (PreparedStatement ps = con.prepareStatement("DELETE FROM detalle_solicitud WHERE solicitud_id = ?")) {
                    ps.setInt(1, s.getId());
                    ps.executeUpdate();
                }
                insertarDetalle(con, s);
                con.commit();
                return true;
            } catch (SQLException ex) {
                con.rollback();
                throw ex;
            }
        }
    }

    @Override
    public boolean cambiarEstado(int id, String nuevoEstado, int usuarioId, String... estadosPermitidos)
            throws SQLException {
        StringBuilder in = new StringBuilder();
        for (int i = 0; i < estadosPermitidos.length; i++) {
            in.append(i == 0 ? "?" : ", ?");
        }
        String sql = "UPDATE solicitudes_alquiler SET estado = ?, aprobado_por = ?, "
                   + "fecha_resolucion = CURRENT_TIMESTAMP WHERE id = ? AND estado IN (" + in + ")";
        try (PreparedStatement ps = con().prepareStatement(sql)) {
            ps.setString(1, nuevoEstado);
            ps.setInt(2, usuarioId);
            ps.setInt(3, id);
            for (int i = 0; i < estadosPermitidos.length; i++) {
                ps.setString(4 + i, estadosPermitidos[i]);
            }
            return ps.executeUpdate() > 0;
        }
    }

    // ---------------------- auxiliares ----------------------
    private void insertarDetalle(Connection con, Solicitud s) throws SQLException {
        String sql = "INSERT INTO detalle_solicitud (solicitud_id, equipo_id, dias, precio_dia, subtotal) "
                   + "VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            for (DetalleSolicitud d : s.getDetalles()) {
                ps.setInt(1, s.getId());
                ps.setInt(2, d.getEquipoId());
                ps.setInt(3, d.getDias());
                ps.setBigDecimal(4, d.getPrecioDia());
                ps.setBigDecimal(5, d.getSubtotal());
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    private Solicitud mapear(ResultSet rs) throws SQLException {
        Solicitud s = new Solicitud();
        s.setId(rs.getInt("id"));
        s.setClienteId(rs.getInt("cliente_id"));
        s.setUsuarioId(rs.getInt("usuario_id"));
        s.setDiasSolicitados(rs.getInt("dias_solicitados"));
        s.setSubtotal(rs.getBigDecimal("subtotal"));
        s.setIgv(rs.getBigDecimal("igv"));
        s.setGarantia(rs.getBigDecimal("garantia"));
        s.setTotal(rs.getBigDecimal("total"));
        s.setEstado(rs.getString("estado"));
        s.setObservaciones(rs.getString("observaciones"));
        int aprobado = rs.getInt("aprobado_por");
        s.setAprobadoPor(rs.wasNull() ? 0 : aprobado);
        s.setFechaResolucion(rs.getObject("fecha_resolucion", OffsetDateTime.class));
        s.setFechaCreacion(rs.getObject("fecha_creacion", OffsetDateTime.class));
        s.setClienteNombre((rs.getString("cli_nombres") + " " + rs.getString("cli_apellidos")).trim());
        s.setClienteDocumento(rs.getString("tipo_documento") + " " + rs.getString("numero_documento"));
        s.setUsuarioNombre(rs.getString("usuario_nombre"));
        String aprobador = rs.getString("aprobador_nombre");
        s.setAprobadorNombre(aprobador == null ? "" : aprobador);
        s.setCantidadEquipos(rs.getInt("cant_equipos"));
        int alquiler = rs.getInt("alquiler_id");
        s.setAlquilerId(rs.wasNull() ? 0 : alquiler);
        return s;
    }
}
