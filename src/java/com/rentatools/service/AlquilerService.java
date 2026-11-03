package com.rentatools.service;

import com.rentatools.model.Alquiler;
import com.rentatools.model.Cliente;
import com.rentatools.model.DetalleAlquiler;
import com.rentatools.model.Equipo;
import com.rentatools.repository.AlquilerRepository;
import com.rentatools.repository.AlquilerRepositoryImpl;
import com.rentatools.repository.ClienteRepository;
import com.rentatools.repository.ClienteRepositoryImpl;
import com.rentatools.repository.EquipoRepository;
import com.rentatools.repository.EquipoRepositoryImpl;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/** Reglas de negocio del modulo Alquileres. */
public class AlquilerService {

    // ===== Reglas de cobro y plazos (valores propuestos: se pueden ajustar aqui) =====
    public static final BigDecimal IGV = new BigDecimal("0.18");        // 18 % sobre el subtotal
    public static final BigDecimal GARANTIA = new BigDecimal("0.20");   // 20 % del subtotal, reembolsable
    public static final int DIAS_MAXIMO = 90;                           // duracion maxima de un alquiler
    public static final int DIAS_AVISO = 2;                             // "por vencer" = vence en 2 dias o menos
    private static final ZoneId ZONA = ZoneId.of("America/Lima");

    private final AlquilerRepository repositorio = new AlquilerRepositoryImpl();
    private final ClienteRepository clientes = new ClienteRepositoryImpl();
    private final EquipoRepository equipos = new EquipoRepositoryImpl();

    /** Fecha de hoy en Lima (el servidor de Render trabaja en UTC). */
    public static LocalDate hoy() {
        return LocalDate.now(ZONA);
    }

    // ---------- Ayudas para las pantallas ----------
    public static long diasRestantes(Alquiler a, LocalDate hoy) {
        return ChronoUnit.DAYS.between(hoy, a.getFechaDevolucionPrevista());
    }

    public static String etiquetaPlazo(Alquiler a, LocalDate hoy) {
        long d = diasRestantes(a, hoy);
        if (d < 0) return "Vencido hace " + (-d) + (d == -1 ? " día" : " días");
        if (d == 0) return "Vence hoy";
        if (d == 1) return "Vence mañana";
        return "Vence en " + d + " días";
    }

    public static String clasePlazo(Alquiler a, LocalDate hoy) {
        long d = diasRestantes(a, hoy);
        if (d < 0) return "text-bg-danger";
        if (d <= DIAS_AVISO) return "text-bg-warning";
        return "text-bg-success";
    }

    // ---------- Consultas ----------
    public List<Alquiler> listar() throws SQLException {
        return repositorio.listarTodos();
    }

    public Alquiler obtener(int id) throws SQLException {
        return repositorio.buscarPorId(id);
    }

    public List<Alquiler> alertas() throws SQLException {
        return repositorio.listarAlertas(hoy().plusDays(DIAS_AVISO));
    }

    public int contarActivos() throws SQLException {
        return repositorio.contarPorEstado("ACTIVO");
    }

    public int contarVencidos() throws SQLException {
        return repositorio.contarVencidos(hoy());
    }

    public int contarPorVencer() throws SQLException {
        LocalDate hoy = hoy();
        return repositorio.contarPorVencer(hoy, hoy.plusDays(DIAS_AVISO));
    }

    // ---------- Registro ----------
    /** Devuelve el numero del alquiler creado. */
    public int registrar(int clienteId, int usuarioId, LocalDate prevista,
                         List<Integer> equipoIds, String observaciones)
            throws ValidacionException, SQLException {

        List<String> errores = new ArrayList<>();
        LocalDate hoy = hoy();
        String obs = observaciones == null ? "" : observaciones.trim();

        Cliente cliente = clientes.buscarPorId(clienteId);
        if (cliente == null) {
            errores.add("Seleccione un cliente");
        } else if (!"ACTIVO".equals(cliente.getEstado())) {
            errores.add("El cliente seleccionado está inactivo");
        }

        if (prevista == null) {
            errores.add("Ingrese una fecha de devolución válida");
        } else if (prevista.isBefore(hoy)) {
            errores.add("La fecha de devolución no puede ser anterior a hoy");
        } else if (ChronoUnit.DAYS.between(hoy, prevista) > DIAS_MAXIMO) {
            errores.add("Un alquiler no puede superar " + DIAS_MAXIMO + " días");
        }

        if (obs.length() > 255) {
            errores.add("Las observaciones no pueden superar 255 caracteres");
        }

        List<DetalleAlquiler> detalles = new ArrayList<>();
        if (equipoIds == null || equipoIds.isEmpty()) {
            errores.add("Seleccione al menos un equipo");
        } else {
            Set<Integer> sinRepetir = new LinkedHashSet<>(equipoIds);
            for (Integer idEquipo : sinRepetir) {
                Equipo e = equipos.buscarPorId(idEquipo);
                if (e == null) {
                    errores.add("Uno de los equipos seleccionados no existe");
                } else if (!"DISPONIBLE".equals(e.getEstado())) {
                    errores.add("El equipo " + e.getCodigo() + " no está disponible");
                } else {
                    DetalleAlquiler d = new DetalleAlquiler();
                    d.setEquipoId(e.getId());
                    d.setEquipoCodigo(e.getCodigo());
                    d.setEquipoNombre(e.getNombre());
                    d.setPrecioDia(e.getPrecioDia());
                    detalles.add(d);
                }
            }
        }
        if (!errores.isEmpty()) {
            throw new ValidacionException(errores);
        }

        // Calculo de montos (el mismo dia de devolucion se cobra como 1 dia)
        int dias = (int) Math.max(1, ChronoUnit.DAYS.between(hoy, prevista));
        BigDecimal subtotal = BigDecimal.ZERO;
        for (DetalleAlquiler d : detalles) {
            d.setDias(dias);
            d.setSubtotal(d.getPrecioDia().multiply(BigDecimal.valueOf(dias)).setScale(2, RoundingMode.HALF_UP));
            subtotal = subtotal.add(d.getSubtotal());
        }
        BigDecimal igv = subtotal.multiply(IGV).setScale(2, RoundingMode.HALF_UP);

        Alquiler a = new Alquiler();
        a.setClienteId(clienteId);
        a.setUsuarioId(usuarioId);
        a.setFechaInicio(hoy);
        a.setFechaDevolucionPrevista(prevista);
        a.setSubtotal(subtotal);
        a.setIgv(igv);
        a.setTotal(subtotal.add(igv));
        a.setGarantia(subtotal.multiply(GARANTIA).setScale(2, RoundingMode.HALF_UP));
        a.setObservaciones(obs);
        a.setDetalles(detalles);

        List<String> noDisponibles = repositorio.registrar(a);
        if (!noDisponibles.isEmpty()) {
            throw new ValidacionException("Los equipos " + String.join(", ", noDisponibles)
                    + " acaban de ser alquilados por otro usuario");
        }
        return a.getId();
    }

    // ---------- Anulacion ----------
    public void anular(int id) throws ValidacionException, SQLException {
        Alquiler a = repositorio.buscarPorId(id);
        if (a == null) {
            throw new ValidacionException("El alquiler no existe");
        }
        if (!"ACTIVO".equals(a.getEstado())) {
            throw new ValidacionException("Solo se pueden anular alquileres activos");
        }
        if (!repositorio.anular(id)) {
            throw new ValidacionException("El alquiler ya no está activo");
        }
    }
}
