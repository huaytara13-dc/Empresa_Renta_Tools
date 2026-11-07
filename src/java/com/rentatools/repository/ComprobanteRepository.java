package com.rentatools.repository;

import com.rentatools.model.Comprobante;
import java.sql.SQLException;
import java.util.List;

/** Patron Repository para comprobantes de pago. */
public interface ComprobanteRepository {

    List<Comprobante> listarTodos() throws SQLException;

    Comprobante buscarPorId(int id) throws SQLException;

    /** Comprobante del alquiler, o null si aun no se emitio. */
    Comprobante buscarPorAlquiler(int alquilerId) throws SQLException;

    /**
     * Asigna serie y numero correlativo y guarda el comprobante, en una transaccion.
     * Lanza SQLException con estado 23505 si el alquiler ya tenia comprobante.
     */
    void emitir(Comprobante comprobante) throws SQLException;
}
