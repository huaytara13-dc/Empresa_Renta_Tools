package com.rentatools.repository;

import com.rentatools.model.DetalleDevolucion;
import com.rentatools.model.Devolucion;
import com.rentatools.model.Incidencia;
import com.rentatools.util.ConexionBD;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementacion JDBC / PostgreSQL. Las consultas usan la conexion compartida del Singleton;
 * registrar() usa una conexion propia con transaccion.
 */
public class DevolucionRepositoryImpl implements DevolucionRepository {

    private static final String SELECT_BASE =
            "SELECT d.id, d.alquiler_id, d.usuario_id, d.fecha_devolucion, d.dias_retraso, "
          + "d.penalidad_retraso, d.penalidad_incidencias, d.penalidad_total, d.garantia_devuelta, "
          + "d.monto_adicional, d.observaciones, d.fecha_creacion, "
          + "c.nombres AS cli_nombres, c.apellidos AS cli_apellidos, u.usuario AS usuario_nombre, "
          + "(SELECT COUNT(*) FROM incidencias i WHERE i.devolucion_id = d.id) AS cant_incidencias "
          + "FROM devoluciones d "
          + "JOIN alquileres a ON a.id = d.alquiler_id "
          + "JOIN clientes c ON c.id = a.cliente_id "
          + "JOIN usuarios u ON u.id = d.usuario_id ";

    private Connection con() throws SQLException {
        return ConexionBD.getInstancia().getConexion();
    }

    @Override
    public List<Devolucion> listarTodos() throws SQLException {
        List<Devolucion> lista = new ArrayList<>();
        try (PreparedStatement ps = con().prepareStatement(SELECT_BASE + "ORDER BY d.id DESC");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(mapear(rs));
            }
        }
        return lista;
    }

    @Override
    public Devolucion buscarPorId(int id) throws SQLException {
        return buscar(SELECT_BASE + "WHERE d.id = ?", id);
    }

    @Override
    public Devolucion buscarPorAlquiler(int alquilerId) throws SQLException {
        return buscar(SELECT_BASE + "WHERE d.alquiler_id = ?", alquilerId);
    }

    private Devolucion buscar(String sql, int parametro) throws SQLException {
        Devolucion dev = null;
        try (PreparedStatement ps = con().prepareStatement(sql)) {
            ps.setInt(1, parametro);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    dev = mapear(rs);
                }
            }
        }
        if (dev == null) {
            return null;
        }

        String sqlDetalle = "SELECT dd.equipo_id, dd.estado_equipo, e.codigo, e.nombre "
                + "FROM detalle_devolucion dd JOIN equipos e ON e.id = dd.equipo_id "
                + "WHERE dd.devolucion_id = ? ORDER BY dd.id";
        try (PreparedStatement ps = con().prepareStatement(sqlDetalle)) {
            ps.setInt(1, dev.getId());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    DetalleDevolucion d = new DetalleDevolucion();
                    d.setEquipoId(rs.getInt("equipo_id"));
                    d.setEstadoEquipo(rs.getString("estado_equipo"));
                    d.setEquipoCodigo(rs.getString("codigo"));
                    d.setEquipoNombre(rs.getString("nombre"));
                    dev.getDetalles().add(d);
                }
            }
        }

        String sqlIncidencias = "SELECT i.id, i.equipo_id, i.tipo, i.descripcion, i.monto, e.codigo "
                + "FROM incidencias i JOIN equipos e ON e.id = i.equipo_id "
                + "WHERE i.devolucion_id = ? ORDER BY i.id";
        try (PreparedStatement ps = con().prepareStatement(sqlIncidencias)) {
            ps.setInt(1, dev.getId());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Incidencia i = new Incidencia();
                    i.setId(rs.getInt("id"));
                    i.setEquipoId(rs.getInt("equipo_id"));
                    i.setTipo(rs.getString("tipo"));
                    i.setDescripcion(rs.getString("descripcion"));
                    i.setMonto(rs.getBigDecimal("monto"));
                    i.setEquipoCodigo(rs.getString("codigo"));
                    dev.getIncidencias().add(i);
                }
            }
        }
        return dev;
    }

    @Override
    public boolean registrar(Devolucion dev) throws SQLException {
        try (Connection con = ConexionBD.getInstancia().nuevaConexion()) {
            con.setAutoCommit(false);
            try {
                // 1) Finalizar el alquiler (solo si sigue ACTIVO)
                int filas;
                try (PreparedStatement ps = con.prepareStatement(
                        "UPDATE alquileres SET estado = 'FINALIZADO', fecha_devolucion_real = ? "
                      + "WHERE id = ? AND estado = 'ACTIVO'")) {
                    ps.setObject(1, dev.getFechaDevolucion());
                    ps.setInt(2, dev.getAlquilerId());
                    filas = ps.executeUpdate();
                }
                if (filas == 0) {
                    con.rollback();
                    return false;
                }

                // 2) Cabecera
                String insertar = "INSERT INTO devoluciones (alquiler_id, usuario_id, fecha_devolucion, dias_retraso, "
                        + "penalidad_retraso, penalidad_incidencias, penalidad_total, garantia_devuelta, "
                        + "monto_adicional, observaciones) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?) RETURNING id";
                try (PreparedStatement ps = con.prepareStatement(insertar)) {
                    ps.setInt(1, dev.getAlquilerId());
                    ps.setInt(2, dev.getUsuarioId());
                    ps.setObject(3, dev.getFechaDevolucion());
                    ps.setInt(4, dev.getDiasRetraso());
                    ps.setBigDecimal(5, dev.getPenalidadRetraso());
                    ps.setBigDecimal(6, dev.getPenalidadIncidencias());
                    ps.setBigDecimal(7, dev.getPenalidadTotal());
                    ps.setBigDecimal(8, dev.getGarantiaDevuelta());
                    ps.setBigDecimal(9, dev.getMontoAdicional());
                    ps.setString(10, dev.getObservaciones());
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            dev.setId(rs.getInt(1));
                        }
                    }
                }

                // 3) Detalle y estado final de cada equipo
                try (PreparedStatement insDetalle = con.prepareStatement(
                             "INSERT INTO detalle_devolucion (devolucion_id, equipo_id, estado_equipo) VALUES (?, ?, ?)");
                     PreparedStatement updEquipo = con.prepareStatement(
                             "UPDATE equipos SET estado = ? WHERE id = ?")) {
                    for (DetalleDevolucion d : dev.getDetalles()) {
                        insDetalle.setInt(1, dev.getId());
                        insDetalle.setInt(2, d.getEquipoId());
                        insDetalle.setString(3, d.getEstadoEquipo());
                        insDetalle.addBatch();

                        updEquipo.setString(1, d.getEstadoEquipo());
                        updEquipo.setInt(2, d.getEquipoId());
                        updEquipo.addBatch();
                    }
                    insDetalle.executeBatch();
                    updEquipo.executeBatch();
                }

                // 4) Incidencias
                try (PreparedStatement ps = con.prepareStatement(
                        "INSERT INTO incidencias (devolucion_id, equipo_id, tipo, descripcion, monto) "
                      + "VALUES (?, ?, ?, ?, ?)")) {
                    for (Incidencia i : dev.getIncidencias()) {
                        ps.setInt(1, dev.getId());
                        ps.setInt(2, i.getEquipoId());
                        ps.setString(3, i.getTipo());
                        ps.setString(4, i.getDescripcion());
                        ps.setBigDecimal(5, i.getMonto());
                        ps.addBatch();
                    }
                    ps.executeBatch();
                }

                con.commit();
                return true;

            } catch (SQLException ex) {
                con.rollback();
                throw ex;
            }
        }
    }

    private Devolucion mapear(ResultSet rs) throws SQLException {
        Devolucion d = new Devolucion();
        d.setId(rs.getInt("id"));
        d.setAlquilerId(rs.getInt("alquiler_id"));
        d.setUsuarioId(rs.getInt("usuario_id"));
        d.setFechaDevolucion(rs.getObject("fecha_devolucion", LocalDate.class));
        d.setDiasRetraso(rs.getInt("dias_retraso"));
        d.setPenalidadRetraso(rs.getBigDecimal("penalidad_retraso"));
        d.setPenalidadIncidencias(rs.getBigDecimal("penalidad_incidencias"));
        d.setPenalidadTotal(rs.getBigDecimal("penalidad_total"));
        d.setGarantiaDevuelta(rs.getBigDecimal("garantia_devuelta"));
        d.setMontoAdicional(rs.getBigDecimal("monto_adicional"));
        d.setObservaciones(rs.getString("observaciones"));
        d.setFechaCreacion(rs.getObject("fecha_creacion", OffsetDateTime.class));
        d.setClienteNombre((rs.getString("cli_nombres") + " " + rs.getString("cli_apellidos")).trim());
        d.setUsuarioNombre(rs.getString("usuario_nombre"));
        d.setCantidadIncidencias(rs.getInt("cant_incidencias"));
        return d;
    }
}
