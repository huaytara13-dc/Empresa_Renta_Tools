package com.rentatools.repository;

import java.util.Map;
import com.rentatools.model.Pagina;
import com.rentatools.model.Alquiler;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

/** Patron Repository para alquileres (cabecera + detalle). */
public interface AlquilerRepository {

    List<Alquiler> listarTodos() throws SQLException;

    /** Una pagina del listado (con busqueda opcional). */
    Pagina<Alquiler> listarPagina(String q, int pagina, int tamano) throws SQLException;


    /** Alquileres activos que vencen hasta la fecha limite (incluye los ya vencidos). */
    List<Alquiler> listarAlertas(LocalDate limite) throws SQLException;

    /** Devuelve el alquiler con su detalle, o null. */
    Alquiler buscarPorId(int id) throws SQLException;

    int contarPorEstado(String estado) throws SQLException;

    int contarVencidos(LocalDate hoy) throws SQLException;

    int contarPorVencer(LocalDate desde, LocalDate hasta) throws SQLException;

    /**
     * Registra el alquiler y su detalle y marca los equipos como ALQUILADO, todo en una transaccion.
     * Si el alquiler nace de una solicitud, tambien la marca como ATENDIDA.
     * Devuelve los motivos por los que no se pudo registrar (equipo ocupado, solicitud ya atendida);
     * si la lista no esta vacia, no se guardo nada.
     */
    List<String> registrar(Alquiler alquiler) throws SQLException;

    /** Anula un alquiler ACTIVO y libera sus equipos. Devuelve false si no estaba activo. */
    boolean anular(int id) throws SQLException;
}
