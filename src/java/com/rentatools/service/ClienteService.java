package com.rentatools.service;

import com.rentatools.model.Cliente;
import com.rentatools.repository.ClienteRepository;
import com.rentatools.repository.ClienteRepositoryImpl;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/** Reglas de negocio del modulo Clientes. */
public class ClienteService {

    private static final Pattern DNI = Pattern.compile("^[0-9]{8}$");
    private static final Pattern RUC = Pattern.compile("^[0-9]{11}$");
    private static final Pattern CE = Pattern.compile("^[A-Z0-9]{9,12}$");
    private static final Pattern TELEFONO = Pattern.compile("^[0-9+() -]{6,20}$");
    private static final Pattern CORREO = Pattern.compile("^[\\w.+-]+@[\\w-]+(\\.[\\w-]+)+$");

    // Depende de la interfaz, no de la implementacion JDBC
    private final ClienteRepository repositorio = new ClienteRepositoryImpl();

    // ---------- Consultas ----------
    /** Una pagina del listado, con busqueda opcional (Etapa de paginacion). */
    public com.rentatools.model.Pagina<Cliente> listarPagina(String q, int pagina) throws SQLException {
        return repositorio.listarPagina(q, pagina, com.rentatools.util.Paginacion.TAMANO);
    }

    /** Cantidad de registros por estado, para los indicadores de la pantalla. */
    public java.util.Map<String, Integer> contarPorEstado() throws SQLException {
        return repositorio.contarPorEstado();
    }

    public List<Cliente> listar() throws SQLException {
        return repositorio.listarTodos();
    }

    public Cliente obtener(int id) throws SQLException {
        return repositorio.buscarPorId(id);
    }

    public int contarTodos() throws SQLException {
        return repositorio.contarTodos();
    }

    // ---------- CRUD ----------
    public void crear(Cliente c) throws ValidacionException, SQLException {
        normalizar(c);
        List<String> errores = validar(c, 0);
        lanzarSiHay(errores);
        try {
            repositorio.crear(c);
        } catch (SQLException e) {
            traducirDuplicado(e);
        }
    }

    public void actualizar(Cliente c) throws ValidacionException, SQLException {
        if (repositorio.buscarPorId(c.getId()) == null) {
            throw new ValidacionException("El cliente no existe");
        }
        normalizar(c);
        List<String> errores = validar(c, c.getId());
        lanzarSiHay(errores);
        try {
            repositorio.actualizar(c);
        } catch (SQLException e) {
            traducirDuplicado(e);
        }
    }

    public void eliminar(int id) throws ValidacionException, SQLException {
        if (repositorio.buscarPorId(id) == null) {
            throw new ValidacionException("El cliente no existe");
        }
        try {
            repositorio.eliminar(id);
        } catch (SQLException e) {
            // 23503 = clave foranea: el cliente ya tiene alquileres registrados
            if ("23503".equals(e.getSQLState())) {
                throw new ValidacionException(
                        "El cliente tiene alquileres registrados y no se puede eliminar. Cámbielo a Inactivo.");
            }
            throw e;
        }
    }

    // ---------- Validaciones ----------
    private void normalizar(Cliente c) {
        c.setTipoDocumento(recortar(c.getTipoDocumento()).toUpperCase());
        c.setNumeroDocumento(recortar(c.getNumeroDocumento()).toUpperCase());
        c.setNombres(recortar(c.getNombres()));
        c.setApellidos(recortar(c.getApellidos()));
        c.setTelefono(recortar(c.getTelefono()));
        c.setCorreo(recortar(c.getCorreo()).toLowerCase());
        c.setDireccion(recortar(c.getDireccion()));
        c.setEstado(recortar(c.getEstado()));
    }

    private String recortar(String s) {
        return s == null ? "" : s.trim();
    }

    private List<String> validar(Cliente c, int idExcluir) throws SQLException {
        List<String> errores = new ArrayList<>();
        String tipo = c.getTipoDocumento();

        Pattern formato = null;
        String ayuda = "";
        if ("DNI".equals(tipo)) {
            formato = DNI;
            ayuda = "El DNI debe tener 8 dígitos";
        } else if ("RUC".equals(tipo)) {
            formato = RUC;
            ayuda = "El RUC debe tener 11 dígitos";
        } else if ("CE".equals(tipo)) {
            formato = CE;
            ayuda = "El carnet de extranjería debe tener de 9 a 12 letras o números";
        } else {
            errores.add("Seleccione un tipo de documento válido");
        }

        if (formato != null) {
            if (!formato.matcher(c.getNumeroDocumento()).matches()) {
                errores.add(ayuda);
            } else if (repositorio.existeDocumento(c.getNumeroDocumento(), idExcluir)) {
                errores.add("Ya existe un cliente con ese número de documento");
            }
        }

        if (c.getNombres().isEmpty()) {
            errores.add("Los nombres o la razón social son obligatorios");
        } else if (c.getNombres().length() > 100) {
            errores.add("Los nombres no pueden superar 100 caracteres");
        }

        if (!"RUC".equals(tipo) && c.getApellidos().isEmpty()) {
            errores.add("Los apellidos son obligatorios");
        }
        if (c.getApellidos().length() > 100) {
            errores.add("Los apellidos no pueden superar 100 caracteres");
        }

        if (!c.getTelefono().isEmpty() && !TELEFONO.matcher(c.getTelefono()).matches()) {
            errores.add("El teléfono solo puede tener números, espacios y los símbolos + ( ) -");
        }
        if (!c.getCorreo().isEmpty()
                && (!CORREO.matcher(c.getCorreo()).matches() || c.getCorreo().length() > 120)) {
            errores.add("Ingrese un correo electrónico válido");
        }
        if (c.getDireccion().length() > 200) {
            errores.add("La dirección no puede superar 200 caracteres");
        }
        if (!"ACTIVO".equals(c.getEstado()) && !"INACTIVO".equals(c.getEstado())) {
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
            throw new ValidacionException("Ya existe un cliente con ese número de documento");
        }
        throw e;
    }
}
