package com.rentatools.repository;

import java.util.Map;
import com.rentatools.model.Pagina;
import com.rentatools.model.Devolucion;
import java.sql.SQLException;
import java.util.List;

/** Patron Repository para devoluciones (cabecera, detalle e incidencias). */
public interface DevolucionRepository {

    List<Devolucion> listarTodos() throws SQLException;

    /** Una pagina del listado (con busqueda opcional). */
    Pagina<Devolucion> listarPagina(String q, int pagina, int tamano) throws SQLException;

    /** Indicadores de la pantalla calculados con SQL sobre toda la tabla. */
    Map<String, java.math.BigDecimal> indicadores() throws SQLException;

    /** Devuelve la devolucion con su detalle e incidencias, o null. */
    Devolucion buscarPorId(int id) throws SQLException;

    /** Devolucion de un alquiler, o null si aun no se devolvio. */
    Devolucion buscarPorAlquiler(int alquilerId) throws SQLException;

    /**
     * Registra la devolucion en una transaccion: finaliza el alquiler, guarda cabecera, detalle e
     * incidencias y actualiza el estado de cada equipo. Devuelve false si el alquiler ya no estaba activo.
     */
    boolean registrar(Devolucion devolucion) throws SQLException;
}
