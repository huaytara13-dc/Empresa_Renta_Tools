package com.rentatools.service;

import com.rentatools.model.Alquiler;
import com.rentatools.model.Cliente;
import com.rentatools.model.GuiaEntrega;
import com.rentatools.repository.AlquilerRepository;
import com.rentatools.repository.AlquilerRepositoryImpl;
import com.rentatools.repository.ClienteRepository;
import com.rentatools.repository.ClienteRepositoryImpl;
import com.rentatools.repository.GuiaRepository;
import com.rentatools.repository.GuiaRepositoryImpl;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/** Reglas de la guia de entrega: emision y conformidad del cliente. */
public class GuiaService {

    private static final Pattern DOCUMENTO = Pattern.compile("^[A-Za-z0-9]{8,12}$");

    private final GuiaRepository repositorio = new GuiaRepositoryImpl();
    private final AlquilerRepository alquileres = new AlquilerRepositoryImpl();
    private final ClienteRepository clientes = new ClienteRepositoryImpl();

    public static String etiquetaEstado(String estado) {
        if ("EMITIDA".equals(estado)) return "Emitida";
        if ("ENTREGADA".equals(estado)) return "Entregada";
        if ("ANULADA".equals(estado)) return "Anulada";
        return estado;
    }

    public static String claseEstado(String estado) {
        if ("EMITIDA".equals(estado)) return "text-bg-warning";
        if ("ENTREGADA".equals(estado)) return "text-bg-success";
        return "text-bg-secondary";
    }

    // ---------- Consultas ----------
    /** Una pagina del listado, con busqueda opcional. */
    public com.rentatools.model.Pagina<GuiaEntrega> listarPagina(String q, int pagina) throws SQLException {
        return repositorio.listarPagina(q, pagina, com.rentatools.util.Paginacion.TAMANO);
    }

    /** Cantidad de registros por estado, para los indicadores de la pantalla. */
    public java.util.Map<String, Integer> contarPorEstado() throws SQLException {
        return repositorio.contarPorEstado();
    }

    public List<GuiaEntrega> listar() throws SQLException {
        return repositorio.listarTodos();
    }

    public GuiaEntrega obtener(int id) throws SQLException {
        return repositorio.buscarPorId(id);
    }

    public GuiaEntrega obtenerPorAlquiler(int alquilerId) throws SQLException {
        return repositorio.buscarPorAlquiler(alquilerId);
    }

    // ---------- Emision ----------
    /** Emite la guia del alquiler. Si ya existe, la devuelve sin crear otra. */
    public GuiaEntrega emitirPara(int alquilerId) throws ValidacionException, SQLException {
        Alquiler a = alquileres.buscarPorId(alquilerId);
        if (a == null) {
            throw new ValidacionException("El alquiler no existe");
        }
        if ("ANULADO".equals(a.getEstado())) {
            throw new ValidacionException("No se puede emitir una guía para un alquiler anulado");
        }

        GuiaEntrega existente = repositorio.buscarPorAlquiler(alquilerId);
        if (existente != null) {
            return existente;
        }

        Cliente cliente = clientes.buscarPorId(a.getClienteId());
        if (cliente == null) {
            throw new ValidacionException("El cliente del alquiler no existe");
        }

        GuiaEntrega g = new GuiaEntrega();
        g.setAlquilerId(alquilerId);
        g.setFechaEmision(AlquilerService.hoy());
        g.setDireccionEntrega(cliente.getDireccion());

        try {
            repositorio.emitir(g);
        } catch (SQLException e) {
            if ("23505".equals(e.getSQLState())) {
                GuiaEntrega ya = repositorio.buscarPorAlquiler(alquilerId);
                if (ya != null) {
                    return ya;
                }
            }
            throw e;
        }
        return g;
    }

    // ---------- Conformidad de entrega ----------
    public void registrarConformidad(int id, String recibidoPor, String documento)
            throws ValidacionException, SQLException {

        GuiaEntrega g = repositorio.buscarPorId(id);
        if (g == null) {
            throw new ValidacionException("La guía no existe");
        }
        if (!"EMITIDA".equals(g.getEstado())) {
            throw new ValidacionException("Solo se puede registrar la conformidad de una guía emitida");
        }

        String nombre = recibidoPor == null ? "" : recibidoPor.trim();
        String doc = documento == null ? "" : documento.trim();

        List<String> errores = new ArrayList<>();
        if (nombre.length() < 3 || nombre.length() > 100) {
            errores.add("Ingrese el nombre completo de quien recibe (de 3 a 100 caracteres)");
        }
        if (!DOCUMENTO.matcher(doc).matches()) {
            errores.add("Ingrese el documento de quien recibe (de 8 a 12 letras o números)");
        }
        if (!errores.isEmpty()) {
            throw new ValidacionException(errores);
        }

        if (!repositorio.registrarConformidad(id, nombre, doc.toUpperCase())) {
            throw new ValidacionException("La guía ya no está pendiente de entrega");
        }
    }
}
