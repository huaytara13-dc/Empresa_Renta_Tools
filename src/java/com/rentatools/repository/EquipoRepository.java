package com.rentatools.repository;

import com.rentatools.model.Equipo;
import java.sql.SQLException;
import java.util.List;

/** Patron Repository para equipos: contrato sin SQL ni JDBC. */
public interface EquipoRepository {

    List<Equipo> listarTodos() throws SQLException;

    Equipo buscarPorId(int id) throws SQLException;

    boolean existeCodigo(String codigo, int excluirId) throws SQLException;

    int contarTodos() throws SQLException;

    int contarPorEstado(String estado) throws SQLException;

    void crear(Equipo equipo) throws SQLException;

    void actualizar(Equipo equipo) throws SQLException;

    void eliminar(int id) throws SQLException;
}
