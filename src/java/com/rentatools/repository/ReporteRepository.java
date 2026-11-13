package com.rentatools.repository;

import com.rentatools.model.ReporteDef;
import com.rentatools.model.ResultadoReporte;
import com.rentatools.util.ConexionBD;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Ejecuta los reportes del catalogo. Solo lectura: las consultas son fijas (no vienen del
 * usuario) y las fechas llegan como parametros de PreparedStatement.
 */
public class ReporteRepository {

    public ResultadoReporte ejecutar(ReporteDef def, LocalDate desde, LocalDate hasta) throws SQLException {
        Connection con = ConexionBD.getInstancia().getConexion();
        try (PreparedStatement ps = con.prepareStatement(def.getSql())) {
            if (def.isUsaFechas()) {
                asignar(ps, 1, desde);
                asignar(ps, 2, desde);
                asignar(ps, 3, hasta);
                asignar(ps, 4, hasta);
            }
            try (ResultSet rs = ps.executeQuery()) {
                ResultSetMetaData md = rs.getMetaData();
                int n = md.getColumnCount();
                List<String> cols = new ArrayList<>();
                for (int i = 1; i <= n; i++) {
                    cols.add(md.getColumnLabel(i));
                }
                List<List<Object>> filas = new ArrayList<>();
                while (rs.next()) {
                    List<Object> f = new ArrayList<>(n);
                    for (int i = 1; i <= n; i++) {
                        Object v = rs.getObject(i);
                        if (v instanceof java.sql.Date || v instanceof java.sql.Timestamp
                                || v instanceof java.time.temporal.TemporalAccessor) {
                            v = v.toString();
                        }
                        f.add(v);
                    }
                    filas.add(f);
                }
                return new ResultadoReporte(cols, filas);
            }
        }
    }

    private static void asignar(PreparedStatement ps, int i, LocalDate d) throws SQLException {
        if (d == null) {
            ps.setNull(i, Types.DATE);
        } else {
            ps.setDate(i, java.sql.Date.valueOf(d));
        }
    }
}
