package com.rentatools.repository;

import com.rentatools.util.ConexionBD;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Guarda accesos y actividades para los reportes de usuarios (Etapa 7).
 * Nunca debe romper la operacion principal: si falla, solo se escribe en el log.
 */
public class AuditoriaRepository {

    private static String cortar(String s, int max) {
        if (s == null) return null;
        return s.length() > max ? s.substring(0, max) : s;
    }

    public void registrarAcceso(Integer usuarioId, String usuario, String evento, String ip) {
        String sql = "INSERT INTO registro_accesos (usuario_id, usuario, evento, ip) VALUES (?,?,?,?)";
        try {
            Connection con = ConexionBD.getInstancia().getConexion();
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                if (usuarioId == null) ps.setNull(1, java.sql.Types.INTEGER); else ps.setInt(1, usuarioId);
                ps.setString(2, cortar(usuario, 50));
                ps.setString(3, evento);
                ps.setString(4, cortar(ip, 60));
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            System.err.println("[Auditoria] no se pudo registrar acceso: " + e.getMessage());
        }
    }

    public void registrarActividad(Integer usuarioId, String usuario, String modulo, String accion, String detalle) {
        String sql = "INSERT INTO auditoria (usuario_id, usuario, modulo, accion, detalle) VALUES (?,?,?,?,?)";
        try {
            Connection con = ConexionBD.getInstancia().getConexion();
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                if (usuarioId == null) ps.setNull(1, java.sql.Types.INTEGER); else ps.setInt(1, usuarioId);
                ps.setString(2, cortar(usuario, 50));
                ps.setString(3, cortar(modulo, 40));
                ps.setString(4, cortar(accion, 40));
                ps.setString(5, cortar(detalle, 200));
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            System.err.println("[Auditoria] no se pudo registrar actividad: " + e.getMessage());
        }
    }
}
