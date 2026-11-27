package com.rentatools.repository;

import java.util.Map;
import com.rentatools.model.Pagina;
import com.rentatools.model.Comprobante;
import java.sql.SQLException;
import java.util.List;

/** Patron Repository para comprobantes de pago. */
public interface ComprobanteRepository {

    List<Comprobante> listarTodos() throws SQLException;

    /** Una pagina del listado (con busqueda opcional). */
    Pagina<Comprobante> listarPagina(String q, int pagina, int tamano) throws SQLException;

    /** Indicadores de la pantalla calculados con SQL sobre toda la tabla. */
    Map<String, java.math.BigDecimal> indicadores() throws SQLException;

    Comprobante buscarPorId(int id) throws SQLException;

    /** Comprobante del alquiler, o null si aun no se emitio. */
    Comprobante buscarPorAlquiler(int alquilerId) throws SQLException;

    /**
     * Asigna serie y numero correlativo y guarda el comprobante, en una transaccion.
     * Lanza SQLException con estado 23505 si el alquiler ya tenia comprobante.
     */
    void emitir(Comprobante comprobante) throws SQLException;
}
