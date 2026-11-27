package com.rentatools.repository;

import java.util.Map;
import java.util.LinkedHashMap;
import com.rentatools.util.Paginacion;
import com.rentatools.model.Pagina;
import com.rentatools.model.Alquiler;
import com.rentatools.model.DetalleAlquiler;
import com.rentatools.util.ConexionBD;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementacion JDBC / PostgreSQL.
 * Las consultas usan la conexion compartida del Singleton; las operaciones que escriben en
 * varias tablas (registrar y anular) usan una conexion propia con transaccion.
 */
public class AlquilerRepositoryImpl implements AlquilerRepository {

    private static final String SELECT_BASE =
            "SELECT a.id, a.solicitud_id, a.cliente_id, a.usuario_id, a.fecha_inicio, a.fecha_devolucion_prevista, "
          + "a.fecha_devolucion_real, a.subtotal, a.igv, a.garantia, a.total, a.estado, a.observaciones, "
          + "a.fecha_creacion, c.nombres AS cli_nombres, c.apellidos AS cli_apellidos, "
          + "c.tipo_documento, c.numero_documento, u.usuario AS usuario_nombre, "
          + "(SELECT COUNT(*) FROM detalle_alquiler d WHERE d.alquiler_id = a.id) AS cant_equipos "
          + "FROM alquileres a "
          + "JOIN clientes c ON c.id = a.cliente_id "
          + "JOIN usuarios u ON u.id = a.usuario_id ";

    private Connection con() throws SQLException {
        return ConexionBD.getInstancia().getConexion();
    }

    @Override
    public List<Alquiler> listarTodos() throws SQLException {
        List<Alquiler> lista = new ArrayList<>();
        String sql = SELECT_BASE + "ORDER BY a.id DESC";
        try (PreparedStatement ps = con().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(mapear(rs));
            }
        }
        return lista;
    }

    @Override
    public Pagina<Alquiler> listarPagina(String q, int pagina, int tamano) throws SQLException {
        String[] cols = {"CAST(a.id AS TEXT)", "c.nombres", "c.apellidos", "(c.nombres || ' ' || c.apellidos)", "c.numero_documento", "u.usuario", "a.estado"};
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
        List<Alquiler> lista = new ArrayList<>();
        String sql = base + where + " ORDER BY a.id DESC LIMIT ? OFFSET ?";
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
    public List<Alquiler> listarAlertas(LocalDate limite) throws SQLException {
        List<Alquiler> lista = new ArrayList<>();
        String sql = SELECT_BASE
                + "WHERE a.estado = 'ACTIVO' AND a.fecha_devolucion_prevista <= ? "
                + "ORDER BY a.fecha_devolucion_prevista, a.id";
        try (PreparedStatement ps = con().prepareStatement(sql)) {
            ps.setObject(1, limite);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapear(rs));
                }
            }
        }
        return lista;
    }

    @Override
    public Alquiler buscarPorId(int id) throws SQLException {
        Alquiler a = null;
        try (PreparedStatement ps = con().prepareStatement(SELECT_BASE + "WHERE a.id = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    a = mapear(rs);
                }
            }
        }
        if (a != null) {
            String sql = "SELECT d.id, d.alquiler_id, d.equipo_id, d.dias, d.precio_dia, d.subtotal, "
                       + "e.codigo, e.nombre FROM detalle_alquiler d "
                       + "JOIN equipos e ON e.id = d.equipo_id WHERE d.alquiler_id = ? ORDER BY d.id";
            try (PreparedStatement ps = con().prepareStatement(sql)) {
                ps.setInt(1, id);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        DetalleAlquiler d = new DetalleAlquiler();
                        d.setId(rs.getInt("id"));
                        d.setAlquilerId(rs.getInt("alquiler_id"));
                        d.setEquipoId(rs.getInt("equipo_id"));
                        d.setDias(rs.getInt("dias"));
                        d.setPrecioDia(rs.getBigDecimal("precio_dia"));
                        d.setSubtotal(rs.getBigDecimal("subtotal"));
                        d.setEquipoCodigo(rs.getString("codigo"));
                        d.setEquipoNombre(rs.getString("nombre"));
                        a.getDetalles().add(d);
                    }
                }
            }
        }
        return a;
    }

    @Override
    public int contarPorEstado(String estado) throws SQLException {
        try (PreparedStatement ps = con().prepareStatement("SELECT COUNT(*) FROM alquileres WHERE estado = ?")) {
            ps.setString(1, estado);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    @Override
    public int contarVencidos(LocalDate hoy) throws SQLException {
        String sql = "SELECT COUNT(*) FROM alquileres WHERE estado = 'ACTIVO' AND fecha_devolucion_prevista < ?";
        try (PreparedStatement ps = con().prepareStatement(sql)) {
            ps.setObject(1, hoy);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    @Override
    public int contarPorVencer(LocalDate desde, LocalDate hasta) throws SQLException {
        String sql = "SELECT COUNT(*) FROM alquileres WHERE estado = 'ACTIVO' "
                   + "AND fecha_devolucion_prevista >= ? AND fecha_devolucion_prevista <= ?";
        try (PreparedStatement ps = con().prepareStatement(sql)) {
            ps.setObject(1, desde);
            ps.setObject(2, hasta);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    // ---------------------- transacciones ----------------------
    @Override
    public List<String> registrar(Alquiler a) throws SQLException {
        List<String> conflictos = new ArrayList<>();

        try (Connection con = ConexionBD.getInstancia().nuevaConexion()) {
            con.setAutoCommit(false);
            try {
                // 0) Si nace de una solicitud, se marca ATENDIDA (solo si seguia APROBADA: evita convertirla dos veces)
                if (a.getSolicitudId() > 0) {
                    try (PreparedStatement ps = con.prepareStatement(
                            "UPDATE solicitudes_alquiler SET estado = 'ATENDIDA' WHERE id = ? AND estado = 'APROBADA'")) {
                        ps.setInt(1, a.getSolicitudId());
                        if (ps.executeUpdate() == 0) {
                            con.rollback();
                            conflictos.add("La solicitud ya no está aprobada o ya fue atendida");
                            return conflictos;
                        }
                    }
                }

                // 1) Reservar cada equipo: solo si sigue DISPONIBLE (evita alquilar el mismo equipo dos veces)
                String reservar = "UPDATE equipos SET estado = 'ALQUILADO' WHERE id = ? AND estado = 'DISPONIBLE'";
                try (PreparedStatement ps = con.prepareStatement(reservar)) {
                    for (DetalleAlquiler d : a.getDetalles()) {
                        ps.setInt(1, d.getEquipoId());
                        if (ps.executeUpdate() == 0) {
                            conflictos.add("El equipo " + d.getEquipoCodigo() + " ya no está disponible");
                        }
                    }
                }
                if (!conflictos.isEmpty()) {
                    con.rollback();
                    return conflictos;
                }

                // 2) Cabecera
                String insertar = "INSERT INTO alquileres (cliente_id, usuario_id, fecha_inicio, "
                        + "fecha_devolucion_prevista, subtotal, igv, garantia, total, estado, observaciones, solicitud_id) "
                        + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, 'ACTIVO', ?, ?) RETURNING id";
                try (PreparedStatement ps = con.prepareStatement(insertar)) {
                    ps.setInt(1, a.getClienteId());
                    ps.setInt(2, a.getUsuarioId());
                    ps.setObject(3, a.getFechaInicio());
                    ps.setObject(4, a.getFechaDevolucionPrevista());
                    ps.setBigDecimal(5, a.getSubtotal());
                    ps.setBigDecimal(6, a.getIgv());
                    ps.setBigDecimal(7, a.getGarantia());
                    ps.setBigDecimal(8, a.getTotal());
                    ps.setString(9, a.getObservaciones());
                    if (a.getSolicitudId() > 0) {
                        ps.setInt(10, a.getSolicitudId());
                    } else {
                        ps.setNull(10, Types.INTEGER);
                    }
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            a.setId(rs.getInt(1));
                        }
                    }
                }

                // 3) Detalle
                String detalle = "INSERT INTO detalle_alquiler (alquiler_id, equipo_id, dias, precio_dia, subtotal) "
                               + "VALUES (?, ?, ?, ?, ?)";
                try (PreparedStatement ps = con.prepareStatement(detalle)) {
                    for (DetalleAlquiler d : a.getDetalles()) {
                        ps.setInt(1, a.getId());
                        ps.setInt(2, d.getEquipoId());
                        ps.setInt(3, d.getDias());
                        ps.setBigDecimal(4, d.getPrecioDia());
                        ps.setBigDecimal(5, d.getSubtotal());
                        ps.addBatch();
                    }
                    ps.executeBatch();
                }

                con.commit();
                return conflictos;

            } catch (SQLException ex) {
                con.rollback();
                throw ex;
            }
        }
    }

    @Override
    public boolean anular(int id) throws SQLException {
        try (Connection con = ConexionBD.getInstancia().nuevaConexion()) {
            con.setAutoCommit(false);
            try {
                int filas;
                try (PreparedStatement ps = con.prepareStatement(
                        "UPDATE alquileres SET estado = 'ANULADO' WHERE id = ? AND estado = 'ACTIVO'")) {
                    ps.setInt(1, id);
                    filas = ps.executeUpdate();
                }
                if (filas == 0) {
                    con.rollback();
                    return false;
                }
                try (PreparedStatement ps = con.prepareStatement(
                        "UPDATE equipos SET estado = 'DISPONIBLE' WHERE estado = 'ALQUILADO' AND id IN "
                      + "(SELECT equipo_id FROM detalle_alquiler WHERE alquiler_id = ?)")) {
                    ps.setInt(1, id);
                    ps.executeUpdate();
                }
                // Sus documentos tambien quedan anulados
                try (PreparedStatement ps = con.prepareStatement(
                        "UPDATE comprobantes SET estado = 'ANULADO' WHERE alquiler_id = ?")) {
                    ps.setInt(1, id);
                    ps.executeUpdate();
                }
                try (PreparedStatement ps = con.prepareStatement(
                        "UPDATE guias_entrega SET estado = 'ANULADA' WHERE alquiler_id = ?")) {
                    ps.setInt(1, id);
                    ps.executeUpdate();
                }
                con.commit();
                return true;
            } catch (SQLException ex) {
                con.rollback();
                throw ex;
            }
        }
    }

    // ---------------------- auxiliar ----------------------
    private Alquiler mapear(ResultSet rs) throws SQLException {
        Alquiler a = new Alquiler();
        a.setId(rs.getInt("id"));
        int idSolicitud = rs.getInt("solicitud_id");
        a.setSolicitudId(rs.wasNull() ? 0 : idSolicitud);
        a.setClienteId(rs.getInt("cliente_id"));
        a.setUsuarioId(rs.getInt("usuario_id"));
        a.setFechaInicio(rs.getObject("fecha_inicio", LocalDate.class));
        a.setFechaDevolucionPrevista(rs.getObject("fecha_devolucion_prevista", LocalDate.class));
        a.setFechaDevolucionReal(rs.getObject("fecha_devolucion_real", LocalDate.class));
        a.setSubtotal(rs.getBigDecimal("subtotal"));
        a.setIgv(rs.getBigDecimal("igv"));
        a.setGarantia(rs.getBigDecimal("garantia"));
        a.setTotal(rs.getBigDecimal("total"));
        a.setEstado(rs.getString("estado"));
        a.setObservaciones(rs.getString("observaciones"));
        a.setFechaCreacion(rs.getObject("fecha_creacion", OffsetDateTime.class));
        a.setClienteNombre((rs.getString("cli_nombres") + " " + rs.getString("cli_apellidos")).trim());
        a.setClienteDocumento(rs.getString("tipo_documento") + " " + rs.getString("numero_documento"));
        a.setUsuarioNombre(rs.getString("usuario_nombre"));
        a.setCantidadEquipos(rs.getInt("cant_equipos"));
        return a;
    }
}
