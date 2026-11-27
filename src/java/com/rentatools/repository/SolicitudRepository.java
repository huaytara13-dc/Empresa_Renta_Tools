package com.rentatools.repository;

import java.util.Map;
import com.rentatools.model.Pagina;
import com.rentatools.model.Solicitud;
import java.sql.SQLException;
import java.util.List;

/** Patron Repository para solicitudes de alquiler (cabecera + detalle). */
public interface SolicitudRepository {

    List<Solicitud> listarTodos() throws SQLException;

    /** Una pagina del listado (con busqueda opcional). */
    Pagina<Solicitud> listarPagina(String q, int pagina, int tamano) throws SQLException;

    /** Cantidad de registros por estado (indicadores). */
    Map<String, Integer> contarPorEstado() throws SQLException;

    /** Devuelve la solicitud con su detalle y, si ya se atendio, el numero del alquiler; o null. */
    Solicitud buscarPorId(int id) throws SQLException;

    int contarPorEstado(String estado) throws SQLException;

    /** Guarda cabecera y detalle en una transaccion. Deja el numero generado en la solicitud. */
    void crear(Solicitud solicitud) throws SQLException;

    /** Reemplaza cabecera y detalle (transaccion). Devuelve false si ya no estaba PENDIENTE. */
    boolean actualizar(Solicitud solicitud) throws SQLException;

    /**
     * Cambia el estado solo si el actual es uno de los indicados (evita decisiones duplicadas).
     * Devuelve false si no cambio ninguna fila.
     */
    boolean cambiarEstado(int id, String nuevoEstado, int usuarioId, String... estadosPermitidos) throws SQLException;
}
