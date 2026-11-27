package com.rentatools.service;

import com.rentatools.model.Alquiler;
import com.rentatools.model.DetalleAlquiler;
import com.rentatools.model.DetalleDevolucion;
import com.rentatools.model.Devolucion;
import com.rentatools.model.Incidencia;
import com.rentatools.repository.AlquilerRepository;
import com.rentatools.repository.AlquilerRepositoryImpl;
import com.rentatools.repository.DevolucionRepository;
import com.rentatools.repository.DevolucionRepositoryImpl;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Reglas de negocio del modulo Devoluciones: retraso, incidencias, penalidades y garantia. */
public class DevolucionService {

    // ===== Reglas de penalidad (valores propuestos: se pueden ajustar aqui) =====
    /** Recargo por cada dia de retraso y por equipo: 50 % del precio diario. */
    public static final BigDecimal RECARGO_RETRASO = new BigDecimal("0.50");

    public static final List<String> ESTADOS_DEVOLUCION = Collections.unmodifiableList(Arrays.asList(
            "DISPONIBLE", "MANTENIMIENTO", "INCIDENCIA", "BAJA"));

    /** Tipos que el empleado puede registrar (el retraso lo registra el sistema). */
    public static final List<String> TIPOS_MANUALES = Collections.unmodifiableList(Arrays.asList(
            "DANO", "PERDIDA", "ACCESORIOS", "OTRO"));

    private static final BigDecimal MONTO_MAXIMO = new BigDecimal("99999999.99");

    private final DevolucionRepository repositorio = new DevolucionRepositoryImpl();
    private final AlquilerRepository alquileres = new AlquilerRepositoryImpl();

    // ---------- Ayudas para las pantallas ----------
    public static String etiquetaTipo(String tipo) {
        if ("RETRASO".equals(tipo)) return "Retraso";
        if ("DANO".equals(tipo)) return "Daño";
        if ("PERDIDA".equals(tipo)) return "Pérdida";
        if ("ACCESORIOS".equals(tipo)) return "Accesorios faltantes";
        if ("OTRO".equals(tipo)) return "Otro";
        return tipo;
    }

    public static int diasRetraso(Alquiler a, LocalDate hoy) {
        return (int) Math.max(0, ChronoUnit.DAYS.between(a.getFechaDevolucionPrevista(), hoy));
    }

    public static BigDecimal penalidadRetraso(DetalleAlquiler d, int diasRetraso) {
        return d.getPrecioDia().multiply(RECARGO_RETRASO)
                .multiply(BigDecimal.valueOf(diasRetraso)).setScale(2, RoundingMode.HALF_UP);
    }

    // ---------- Consultas ----------
    /** Una pagina del listado, con busqueda opcional. */
    public com.rentatools.model.Pagina<Devolucion> listarPagina(String q, int pagina) throws SQLException {
        return repositorio.listarPagina(q, pagina, com.rentatools.util.Paginacion.TAMANO);
    }

    /** Indicadores de la pantalla calculados sobre toda la tabla. */
    public java.util.Map<String, java.math.BigDecimal> indicadores() throws SQLException {
        return repositorio.indicadores();
    }

    public List<Devolucion> listar() throws SQLException {
        return repositorio.listarTodos();
    }

    public Devolucion obtener(int id) throws SQLException {
        return repositorio.buscarPorId(id);
    }

    public Devolucion obtenerPorAlquiler(int alquilerId) throws SQLException {
        return repositorio.buscarPorAlquiler(alquilerId);
    }

    // ---------- Registro ----------
    /** Devuelve el numero de la devolucion creada. */
    public int registrar(int alquilerId, int usuarioId, List<ItemDevolucion> items, String observaciones)
            throws ValidacionException, SQLException {

        Alquiler alquiler = alquileres.buscarPorId(alquilerId);
        if (alquiler == null) {
            throw new ValidacionException("El alquiler no existe");
        }
        if (!"ACTIVO".equals(alquiler.getEstado())) {
            throw new ValidacionException("El alquiler ya no está activo");
        }

        LocalDate hoy = AlquilerService.hoy();
        int diasRetraso = diasRetraso(alquiler, hoy);
        String obs = observaciones == null ? "" : observaciones.trim();

        Map<Integer, ItemDevolucion> porEquipo = new HashMap<>();
        for (ItemDevolucion it : items) {
            porEquipo.put(it.getEquipoId(), it);
        }

        List<String> errores = new ArrayList<>();
        Devolucion dev = new Devolucion();
        BigDecimal totalRetraso = BigDecimal.ZERO;
        BigDecimal totalIncidencias = BigDecimal.ZERO;

        for (DetalleAlquiler d : alquiler.getDetalles()) {
            String cod = d.getEquipoCodigo();
            ItemDevolucion it = porEquipo.get(d.getEquipoId());
            if (it == null) {
                errores.add("Falta indicar el estado del equipo " + cod);
                continue;
            }

            String estado = limpiar(it.getEstadoEquipo());
            String tipo = limpiar(it.getTipoIncidencia());
            if (!ESTADOS_DEVOLUCION.contains(estado)) {
                errores.add("Seleccione un estado válido para el equipo " + cod);
                continue;
            }
            if (!tipo.isEmpty() && !TIPOS_MANUALES.contains(tipo)) {
                errores.add("El tipo de incidencia del equipo " + cod + " no es válido");
                continue;
            }

            DetalleDevolucion dd = new DetalleDevolucion();
            dd.setEquipoId(d.getEquipoId());
            dd.setEstadoEquipo(estado);
            dev.getDetalles().add(dd);

            // Retraso: lo calcula y registra el sistema
            if (diasRetraso > 0) {
                BigDecimal pen = penalidadRetraso(d, diasRetraso);
                totalRetraso = totalRetraso.add(pen);
                Incidencia inc = new Incidencia();
                inc.setEquipoId(d.getEquipoId());
                inc.setTipo("RETRASO");
                inc.setDescripcion("Devolución con " + diasRetraso + (diasRetraso == 1 ? " día" : " días") + " de retraso");
                inc.setMonto(pen);
                dev.getIncidencias().add(inc);
            }

            // Incidencia ingresada por el empleado
            if (!tipo.isEmpty()) {
                String desc = limpiar(it.getDescripcion());
                BigDecimal monto = parsearMonto(it.getMontoTexto());
                if (desc.isEmpty()) {
                    errores.add("Describa la incidencia del equipo " + cod);
                } else if (desc.length() > 255) {
                    errores.add("La descripción de la incidencia del equipo " + cod + " no puede superar 255 caracteres");
                }
                if (monto == null) {
                    errores.add("Ingrese un monto válido (0 o mayor, máximo 2 decimales) para la incidencia del equipo " + cod);
                }
                if ("PERDIDA".equals(tipo) && !"BAJA".equals(estado)) {
                    errores.add("Un equipo perdido debe quedar De baja (" + cod + ")");
                }
                if ("DANO".equals(tipo) && "DISPONIBLE".equals(estado)) {
                    errores.add("Un equipo dañado no puede quedar Disponible (" + cod + ")");
                }
                if (monto != null) {
                    totalIncidencias = totalIncidencias.add(monto);
                    Incidencia inc = new Incidencia();
                    inc.setEquipoId(d.getEquipoId());
                    inc.setTipo(tipo);
                    inc.setDescripcion(desc);
                    inc.setMonto(monto);
                    dev.getIncidencias().add(inc);
                }
            }
        }
        if (obs.length() > 255) {
            errores.add("Las observaciones no pueden superar 255 caracteres");
        }
        if (!errores.isEmpty()) {
            throw new ValidacionException(errores);
        }

        // Penalidades y garantia
        BigDecimal total = totalRetraso.add(totalIncidencias);
        BigDecimal garantia = alquiler.getGarantia();

        dev.setAlquilerId(alquilerId);
        dev.setUsuarioId(usuarioId);
        dev.setFechaDevolucion(hoy);
        dev.setDiasRetraso(diasRetraso);
        dev.setPenalidadRetraso(totalRetraso);
        dev.setPenalidadIncidencias(totalIncidencias);
        dev.setPenalidadTotal(total);
        dev.setGarantiaDevuelta(garantia.subtract(total).max(BigDecimal.ZERO));
        dev.setMontoAdicional(total.subtract(garantia).max(BigDecimal.ZERO));
        dev.setObservaciones(obs);

        if (!repositorio.registrar(dev)) {
            throw new ValidacionException("El alquiler ya no está activo: otra persona registró su devolución");
        }
        return dev.getId();
    }

    private String limpiar(String s) {
        return s == null ? "" : s.trim();
    }

    /** Vacio = 0. Devuelve null si el monto no es valido. */
    private BigDecimal parsearMonto(String texto) {
        String limpio = limpiar(texto).replace(',', '.');
        if (limpio.isEmpty()) {
            return BigDecimal.ZERO.setScale(2);
        }
        try {
            BigDecimal m = new BigDecimal(limpio);
            if (m.signum() < 0 || m.scale() > 2 || m.compareTo(MONTO_MAXIMO) > 0) {
                return null;
            }
            return m.setScale(2);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
