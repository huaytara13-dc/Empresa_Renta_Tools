package com.rentatools.service;

import com.rentatools.model.Equipo;
import com.rentatools.repository.EquipoRepository;
import com.rentatools.repository.EquipoRepositoryImpl;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

/** Reglas de negocio del modulo Equipos. */
public class EquipoService {

    public static final List<String> CATEGORIAS = Collections.unmodifiableList(Arrays.asList(
            "Taladros", "Tronzadoras", "Sierras", "Esmeriladoras", "Martillos",
            "Herramientas manuales", "Jardinería", "Accesorios", "Otros"));

    public static final List<String> ESTADOS = Collections.unmodifiableList(Arrays.asList(
            "DISPONIBLE", "ALQUILADO", "MANTENIMIENTO", "INCIDENCIA", "BAJA"));

    private static final Pattern CODIGO = Pattern.compile("^[A-Z0-9-]{3,20}$");
    private static final BigDecimal PRECIO_MAXIMO = new BigDecimal("99999999.99");

    private final EquipoRepository repositorio = new EquipoRepositoryImpl();

    /** Texto para mostrar en pantalla. */
    public static String etiquetaEstado(String estado) {
        if ("DISPONIBLE".equals(estado)) return "Disponible";
        if ("ALQUILADO".equals(estado)) return "Alquilado";
        if ("MANTENIMIENTO".equals(estado)) return "En mantenimiento";
        if ("INCIDENCIA".equals(estado)) return "Con incidencia";
        if ("BAJA".equals(estado)) return "De baja";
        return estado;
    }

    // ---------- Consultas ----------
    public List<Equipo> listar() throws SQLException {
        return repositorio.listarTodos();
    }

    public Equipo obtener(int id) throws SQLException {
        return repositorio.buscarPorId(id);
    }

    public int contarTodos() throws SQLException {
        return repositorio.contarTodos();
    }

    public int contarPorEstado(String estado) throws SQLException {
        return repositorio.contarPorEstado(estado);
    }

    // ---------- CRUD ----------
    public void crear(Equipo e, String precioTexto) throws ValidacionException, SQLException {
        normalizar(e);
        List<String> errores = validar(e, 0, precioTexto);
        if ("ALQUILADO".equals(e.getEstado())) {
            errores.add("El estado Alquilado se asigna al registrar un alquiler");
        }
        lanzarSiHay(errores);
        try {
            repositorio.crear(e);
        } catch (SQLException ex) {
            traducirDuplicado(ex);
        }
    }

    public void actualizar(Equipo e, String precioTexto) throws ValidacionException, SQLException {
        Equipo existente = repositorio.buscarPorId(e.getId());
        if (existente == null) {
            throw new ValidacionException("El equipo no existe");
        }
        normalizar(e);
        List<String> errores = validar(e, e.getId(), precioTexto);

        boolean eraAlquilado = "ALQUILADO".equals(existente.getEstado());
        boolean esAlquilado = "ALQUILADO".equals(e.getEstado());
        if (eraAlquilado && !esAlquilado) {
            errores.add("Un equipo alquilado cambia de estado al registrar su devolución");
        }
        if (!eraAlquilado && esAlquilado) {
            errores.add("El estado Alquilado se asigna al registrar un alquiler");
        }
        lanzarSiHay(errores);
        try {
            repositorio.actualizar(e);
        } catch (SQLException ex) {
            traducirDuplicado(ex);
        }
    }

    public void eliminar(int id) throws ValidacionException, SQLException {
        Equipo existente = repositorio.buscarPorId(id);
        if (existente == null) {
            throw new ValidacionException("El equipo no existe");
        }
        if ("ALQUILADO".equals(existente.getEstado())) {
            throw new ValidacionException("No se puede eliminar un equipo que está alquilado");
        }
        try {
            repositorio.eliminar(id);
        } catch (SQLException ex) {
            // 23503 = clave foranea: el equipo ya tiene alquileres en su historial
            if ("23503".equals(ex.getSQLState())) {
                throw new ValidacionException(
                        "El equipo tiene alquileres en su historial y no se puede eliminar. Cámbielo a De baja.");
            }
            throw ex;
        }
    }

    // ---------- Validaciones ----------
    private void normalizar(Equipo e) {
        e.setCodigo(recortar(e.getCodigo()).toUpperCase());
        e.setNombre(recortar(e.getNombre()));
        e.setDescripcion(recortar(e.getDescripcion()));
        e.setCategoria(recortar(e.getCategoria()));
        e.setMarca(recortar(e.getMarca()));
        e.setEstado(recortar(e.getEstado()));
    }

    private String recortar(String s) {
        return s == null ? "" : s.trim();
    }

    private List<String> validar(Equipo e, int idExcluir, String precioTexto) throws SQLException {
        List<String> errores = new ArrayList<>();

        if (!CODIGO.matcher(e.getCodigo()).matches()) {
            errores.add("El código debe tener de 3 a 20 caracteres (letras mayúsculas, números o guion)");
        } else if (repositorio.existeCodigo(e.getCodigo(), idExcluir)) {
            errores.add("Ya existe un equipo con ese código");
        }

        if (e.getNombre().isEmpty()) {
            errores.add("El nombre es obligatorio");
        } else if (e.getNombre().length() > 100) {
            errores.add("El nombre no puede superar 100 caracteres");
        }
        if (e.getDescripcion().length() > 255) {
            errores.add("La descripción no puede superar 255 caracteres");
        }
        if (e.getMarca().length() > 50) {
            errores.add("La marca no puede superar 50 caracteres");
        }
        if (!CATEGORIAS.contains(e.getCategoria())) {
            errores.add("Seleccione una categoría válida");
        }
        if (!ESTADOS.contains(e.getEstado())) {
            errores.add("El estado seleccionado no es válido");
        }

        BigDecimal precio = parsearPrecio(precioTexto);
        if (precio == null) {
            errores.add("Ingrese un precio por día válido, mayor que cero y con máximo 2 decimales");
        } else {
            e.setPrecioDia(precio);
        }
        return errores;
    }

    private BigDecimal parsearPrecio(String texto) {
        if (texto == null) {
            return null;
        }
        String limpio = texto.trim().replace(',', '.');
        if (limpio.isEmpty()) {
            return null;
        }
        try {
            BigDecimal p = new BigDecimal(limpio);
            if (p.signum() <= 0 || p.scale() > 2 || p.compareTo(PRECIO_MAXIMO) > 0) {
                return null;
            }
            return p.setScale(2);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private void lanzarSiHay(List<String> errores) throws ValidacionException {
        if (!errores.isEmpty()) {
            throw new ValidacionException(errores);
        }
    }

    private void traducirDuplicado(SQLException ex) throws ValidacionException, SQLException {
        if ("23505".equals(ex.getSQLState())) {
            throw new ValidacionException("Ya existe un equipo con ese código");
        }
        throw ex;
    }
}
