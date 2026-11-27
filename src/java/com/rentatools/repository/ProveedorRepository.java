package com.rentatools.repository;

import com.rentatools.model.Proveedor;
import com.rentatools.model.Pagina;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/** Patron Repository para proveedores: contrato sin SQL ni JDBC. */
public interface ProveedorRepository {

    List<Proveedor> listarTodos() throws SQLException;

    /** Una pagina del listado (con busqueda opcional). */
    Pagina<Proveedor> listarPagina(String q, int pagina, int tamano) throws SQLException;

    /** Cantidad de registros por estado (para los indicadores). */
    Map<String, Integer> contarPorEstado() throws SQLException;

    Proveedor buscarPorId(int id) throws SQLException;

    boolean existeRuc(String ruc, int excluirId) throws SQLException;

    int contarTodos() throws SQLException;

    void crear(Proveedor proveedor) throws SQLException;

    void actualizar(Proveedor proveedor) throws SQLException;

    void eliminar(int id) throws SQLException;
}
