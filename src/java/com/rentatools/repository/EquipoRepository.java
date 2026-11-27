package com.rentatools.repository;

import com.rentatools.model.Equipo;
import com.rentatools.model.Pagina;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/** Patron Repository para equipos: contrato sin SQL ni JDBC. */
public interface EquipoRepository {

    List<Equipo> listarTodos() throws SQLException;

    /** Una pagina del listado (con busqueda opcional). */
    Pagina<Equipo> listarPagina(String q, int pagina, int tamano) throws SQLException;

    /** Cantidad de registros por estado (para los indicadores). */
    Map<String, Integer> contarPorEstado() throws SQLException;

    Equipo buscarPorId(int id) throws SQLException;

    boolean existeCodigo(String codigo, int excluirId) throws SQLException;

    int contarTodos() throws SQLException;

    int contarPorEstado(String estado) throws SQLException;

    void crear(Equipo equipo) throws SQLException;

    void actualizar(Equipo equipo) throws SQLException;

    void eliminar(int id) throws SQLException;
}
