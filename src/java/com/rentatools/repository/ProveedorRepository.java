package com.rentatools.repository;

import com.rentatools.model.Proveedor;
import java.sql.SQLException;
import java.util.List;

/** Patron Repository para proveedores: contrato sin SQL ni JDBC. */
public interface ProveedorRepository {

    List<Proveedor> listarTodos() throws SQLException;

    Proveedor buscarPorId(int id) throws SQLException;

    boolean existeRuc(String ruc, int excluirId) throws SQLException;

    int contarTodos() throws SQLException;

    void crear(Proveedor proveedor) throws SQLException;

    void actualizar(Proveedor proveedor) throws SQLException;

    void eliminar(int id) throws SQLException;
}
