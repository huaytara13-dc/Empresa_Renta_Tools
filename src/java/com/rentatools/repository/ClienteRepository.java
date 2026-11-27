package com.rentatools.repository;

import com.rentatools.model.Cliente;
import com.rentatools.model.Pagina;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/** Patron Repository para clientes: contrato sin SQL ni JDBC. */
public interface ClienteRepository {

    List<Cliente> listarTodos() throws SQLException;

    /** Una pagina del listado (con busqueda opcional). */
    Pagina<Cliente> listarPagina(String q, int pagina, int tamano) throws SQLException;

    /** Cantidad de registros por estado (para los indicadores). */
    Map<String, Integer> contarPorEstado() throws SQLException;

    Cliente buscarPorId(int id) throws SQLException;

    boolean existeDocumento(String numeroDocumento, int excluirId) throws SQLException;

    int contarTodos() throws SQLException;

    void crear(Cliente cliente) throws SQLException;

    void actualizar(Cliente cliente) throws SQLException;

    void eliminar(int id) throws SQLException;
}
