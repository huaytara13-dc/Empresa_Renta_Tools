package com.rentatools.service;

import com.rentatools.model.Proveedor;
import com.rentatools.repository.ProveedorRepository;
import com.rentatools.repository.ProveedorRepositoryImpl;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

/** Reglas de negocio del modulo Proveedores. */
public class ProveedorService {

    public static final List<String> RUBROS = Collections.unmodifiableList(Arrays.asList(
            "Equipos y herramientas", "Repuestos", "Accesorios e insumos",
            "Transporte", "Mantenimiento", "Otros"));

    private static final Pattern RUC = Pattern.compile("^[0-9]{11}$");
    private static final Pattern TELEFONO = Pattern.compile("^[0-9+() -]{6,20}$");
    private static final Pattern CORREO = Pattern.compile("^[\\w.+-]+@[\\w-]+(\\.[\\w-]+)+$");

    // Depende de la interfaz, no de la implementacion JDBC
    private final ProveedorRepository repositorio = new ProveedorRepositoryImpl();

    // ---------- Consultas ----------
    public List<Proveedor> listar() throws SQLException {
        return repositorio.listarTodos();
    }

    public Proveedor obtener(int id) throws SQLException {
        return repositorio.buscarPorId(id);
    }

    public int contarTodos() throws SQLException {
        return repositorio.contarTodos();
    }

    // ---------- CRUD ----------
    public void crear(Proveedor p) throws ValidacionException, SQLException {
        normalizar(p);
        lanzarSiHay(validar(p, 0));
        try {
            repositorio.crear(p);
        } catch (SQLException e) {
            traducirDuplicado(e);
        }
    }

    public void actualizar(Proveedor p) throws ValidacionException, SQLException {
        if (repositorio.buscarPorId(p.getId()) == null) {
            throw new ValidacionException("El proveedor no existe");
        }
        normalizar(p);
        lanzarSiHay(validar(p, p.getId()));
        try {
            repositorio.actualizar(p);
        } catch (SQLException e) {
            traducirDuplicado(e);
        }
    }

    public void eliminar(int id) throws ValidacionException, SQLException {
        if (repositorio.buscarPorId(id) == null) {
            throw new ValidacionException("El proveedor no existe");
        }
        try {
            repositorio.eliminar(id);
        } catch (SQLException e) {
            // 23503 = clave foranea: el proveedor ya esta referenciado por otro registro
            if ("23503".equals(e.getSQLState())) {
                throw new ValidacionException(
                        "El proveedor tiene registros asociados y no se puede eliminar. Cámbielo a Inactivo.");
            }
            throw e;
        }
    }

    // ---------- Validaciones ----------
    private void normalizar(Proveedor p) {
        p.setRuc(recortar(p.getRuc()));
        p.setRazonSocial(recortar(p.getRazonSocial()));
        p.setContacto(recortar(p.getContacto()));
        p.setTelefono(recortar(p.getTelefono()));
        p.setCorreo(recortar(p.getCorreo()).toLowerCase());
        p.setDireccion(recortar(p.getDireccion()));
        p.setRubro(recortar(p.getRubro()));
        p.setEstado(recortar(p.getEstado()));
    }

    private String recortar(String s) {
        return s == null ? "" : s.trim();
    }

    private List<String> validar(Proveedor p, int idExcluir) throws SQLException {
        List<String> errores = new ArrayList<>();

        if (!RUC.matcher(p.getRuc()).matches()) {
            errores.add("El RUC debe tener 11 dígitos");
        } else if (repositorio.existeRuc(p.getRuc(), idExcluir)) {
            errores.add("Ya existe un proveedor con ese RUC");
        }

        if (p.getRazonSocial().isEmpty()) {
            errores.add("La razón social es obligatoria");
        } else if (p.getRazonSocial().length() > 150) {
            errores.add("La razón social no puede superar 150 caracteres");
        }
        if (p.getContacto().length() > 100) {
            errores.add("El nombre de contacto no puede superar 100 caracteres");
        }
        if (!p.getTelefono().isEmpty() && !TELEFONO.matcher(p.getTelefono()).matches()) {
            errores.add("El teléfono solo puede tener números, espacios y los símbolos + ( ) -");
        }
        if (!p.getCorreo().isEmpty()
                && (!CORREO.matcher(p.getCorreo()).matches() || p.getCorreo().length() > 120)) {
            errores.add("Ingrese un correo electrónico válido");
        }
        if (p.getDireccion().length() > 200) {
            errores.add("La dirección no puede superar 200 caracteres");
        }
        if (!RUBROS.contains(p.getRubro())) {
            errores.add("Seleccione un rubro válido");
        }
        if (!"ACTIVO".equals(p.getEstado()) && !"INACTIVO".equals(p.getEstado())) {
            errores.add("El estado seleccionado no es válido");
        }
        return errores;
    }

    private void lanzarSiHay(List<String> errores) throws ValidacionException {
        if (!errores.isEmpty()) {
            throw new ValidacionException(errores);
        }
    }

    private void traducirDuplicado(SQLException e) throws ValidacionException, SQLException {
        if ("23505".equals(e.getSQLState())) {
            throw new ValidacionException("Ya existe un proveedor con ese RUC");
        }
        throw e;
    }
}
