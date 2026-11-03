package com.rentatools.repository;

import com.rentatools.model.Cliente;
import java.sql.SQLException;
import java.util.List;

/** Patron Repository para clientes: contrato sin SQL ni JDBC. */
public interface ClienteRepository {

    List<Cliente> listarTodos() throws SQLException;

    Cliente buscarPorId(int id) throws SQLException;

    boolean existeDocumento(String numeroDocumento, int excluirId) throws SQLException;

    int contarTodos() throws SQLException;

    void crear(Cliente cliente) throws SQLException;

    void actualizar(Cliente cliente) throws SQLException;

    void eliminar(int id) throws SQLException;
}
