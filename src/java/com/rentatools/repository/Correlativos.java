package com.rentatools.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Entrega el siguiente numero de cada serie. Debe usarse dentro de una transaccion: el UPDATE bloquea
 * la fila, asi dos emisiones simultaneas nunca reciben el mismo numero, y si la emision falla, el numero
 * no se consume (la transaccion se revierte).
 */
final class Correlativos {

    private Correlativos() { }

    /** Devuelve {serie, numero} para el tipo indicado (BOLETA, FACTURA o GUIA). */
    static Object[] siguiente(Connection con, String tipo) throws SQLException {
        String sql = "UPDATE correlativos SET ultimo_numero = ultimo_numero + 1 "
                   + "WHERE tipo = ? RETURNING serie, ultimo_numero";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, tipo);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new SQLException("No hay una serie configurada para " + tipo);
                }
                return new Object[]{rs.getString(1), rs.getInt(2)};
            }
        }
    }
}
