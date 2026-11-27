package com.rentatools.repository;

import java.util.Map;
import com.rentatools.model.Pagina;
import com.rentatools.model.GuiaEntrega;
import java.sql.SQLException;
import java.util.List;

/** Patron Repository para guias de entrega. */
public interface GuiaRepository {

    List<GuiaEntrega> listarTodos() throws SQLException;

    /** Una pagina del listado (con busqueda opcional). */
    Pagina<GuiaEntrega> listarPagina(String q, int pagina, int tamano) throws SQLException;

    /** Cantidad de registros por estado (indicadores). */
    Map<String, Integer> contarPorEstado() throws SQLException;

    GuiaEntrega buscarPorId(int id) throws SQLException;

    /** Guia del alquiler, o null si aun no se emitio. */
    GuiaEntrega buscarPorAlquiler(int alquilerId) throws SQLException;

    /** Asigna serie y numero correlativo y guarda la guia, en una transaccion. */
    void emitir(GuiaEntrega guia) throws SQLException;

    /** Registra la conformidad del cliente. Devuelve false si la guia ya no estaba EMITIDA. */
    boolean registrarConformidad(int id, String recibidoPor, String documentoReceptor) throws SQLException;
}
