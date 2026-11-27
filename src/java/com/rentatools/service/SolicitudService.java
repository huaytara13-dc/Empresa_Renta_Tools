package com.rentatools.service;

import com.rentatools.model.Cliente;
import com.rentatools.model.DetalleSolicitud;
import com.rentatools.model.Equipo;
import com.rentatools.model.Solicitud;
import com.rentatools.repository.ClienteRepository;
import com.rentatools.repository.ClienteRepositoryImpl;
import com.rentatools.repository.EquipoRepository;
import com.rentatools.repository.EquipoRepositoryImpl;
import com.rentatools.repository.SolicitudRepository;
import com.rentatools.repository.SolicitudRepositoryImpl;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Reglas de negocio de la solicitud de alquiler (SOLPED).
 * Flujo: PENDIENTE -> APROBADA | RECHAZADA; APROBADA -> ATENDIDA (se convierte en alquiler);
 * PENDIENTE o APROBADA -> ANULADA.
 */
public class SolicitudService {

    private final SolicitudRepository repositorio = new SolicitudRepositoryImpl();
    private final ClienteRepository clientes = new ClienteRepositoryImpl();
    private final EquipoRepository equipos = new EquipoRepositoryImpl();
    private final AlquilerService alquilerService = new AlquilerService();

    // ---------- Ayudas para las pantallas ----------
    public static String etiquetaEstado(String estado) {
        if ("PENDIENTE".equals(estado)) return "Pendiente";
        if ("APROBADA".equals(estado)) return "Aprobada";
        if ("RECHAZADA".equals(estado)) return "Rechazada";
        if ("ATENDIDA".equals(estado)) return "Atendida";
        if ("ANULADA".equals(estado)) return "Anulada";
        return estado;
    }

    public static String claseEstado(String estado) {
        if ("PENDIENTE".equals(estado)) return "text-bg-warning";
        if ("APROBADA".equals(estado)) return "text-bg-info";
        if ("ATENDIDA".equals(estado)) return "text-bg-success";
        if ("RECHAZADA".equals(estado)) return "text-bg-danger";
        return "text-bg-secondary";
    }

    // ---------- Consultas ----------
    /** Una pagina del listado, con busqueda opcional. */
    public com.rentatools.model.Pagina<Solicitud> listarPagina(String q, int pagina) throws SQLException {
        return repositorio.listarPagina(q, pagina, com.rentatools.util.Paginacion.TAMANO);
    }

    /** Cantidad de registros por estado, para los indicadores de la pantalla. */
    public java.util.Map<String, Integer> contarPorEstado() throws SQLException {
        return repositorio.contarPorEstado();
    }

    public List<Solicitud> listar() throws SQLException {
        return repositorio.listarTodos();
    }

    public Solicitud obtener(int id) throws SQLException {
        return repositorio.buscarPorId(id);
    }

    public int contarPorEstado(String estado) throws SQLException {
        return repositorio.contarPorEstado(estado);
    }

    // ---------- Registro y edicion ----------
    /** Devuelve el numero de la solicitud creada. */
    public int crear(int clienteId, int usuarioId, int dias, List<Integer> equipoIds, String observaciones)
            throws ValidacionException, SQLException {
        Solicitud s = construir(clienteId, usuarioId, dias, equipoIds, observaciones);
        repositorio.crear(s);
        return s.getId();
    }

    public void actualizar(int id, int clienteId, int dias, List<Integer> equipoIds, String observaciones)
            throws ValidacionException, SQLException {

        Solicitud existente = repositorio.buscarPorId(id);
        if (existente == null) {
            throw new ValidacionException("La solicitud no existe");
        }
        if (!"PENDIENTE".equals(existente.getEstado())) {
            throw new ValidacionException("Solo se pueden editar las solicitudes pendientes");
        }
        Solicitud s = construir(clienteId, existente.getUsuarioId(), dias, equipoIds, observaciones);
        s.setId(id);
        if (!repositorio.actualizar(s)) {
            throw new ValidacionException("La solicitud ya no está pendiente");
        }
    }

    // ---------- Decisiones del administrador ----------
    public void aprobar(int id, int adminId) throws ValidacionException, SQLException {
        if (!repositorio.cambiarEstado(id, "APROBADA", adminId, "PENDIENTE")) {
            throw new ValidacionException("La solicitud no existe o ya no está pendiente");
        }
    }

    public void rechazar(int id, int adminId) throws ValidacionException, SQLException {
        if (!repositorio.cambiarEstado(id, "RECHAZADA", adminId, "PENDIENTE")) {
            throw new ValidacionException("La solicitud no existe o ya no está pendiente");
        }
    }

    public void anular(int id, int adminId) throws ValidacionException, SQLException {
        if (!repositorio.cambiarEstado(id, "ANULADA", adminId, "PENDIENTE", "APROBADA")) {
            throw new ValidacionException("Solo se pueden anular solicitudes pendientes o aprobadas");
        }
    }

    // ---------- Conversion en alquiler (la venta) ----------
    /** Devuelve el numero del alquiler creado. */
    public int convertir(int id, int usuarioId) throws ValidacionException, SQLException {
        Solicitud s = repositorio.buscarPorId(id);
        return alquilerService.registrarDesdeSolicitud(s, usuarioId);
    }

    // ---------- Validacion y calculo ----------
    private Solicitud construir(int clienteId, int usuarioId, int dias, List<Integer> equipoIds, String observaciones)
            throws ValidacionException, SQLException {

        List<String> errores = new ArrayList<>();
        String obs = observaciones == null ? "" : observaciones.trim();

        Cliente cliente = clientes.buscarPorId(clienteId);
        if (cliente == null) {
            errores.add("Seleccione un cliente");
        } else if (!"ACTIVO".equals(cliente.getEstado())) {
            errores.add("El cliente seleccionado está inactivo");
        }

        if (dias < 1 || dias > AlquilerService.DIAS_MAXIMO) {
            errores.add("Ingrese una cantidad de días entre 1 y " + AlquilerService.DIAS_MAXIMO);
        }
        if (obs.length() > 255) {
            errores.add("Las observaciones no pueden superar 255 caracteres");
        }

        List<DetalleSolicitud> detalles = new ArrayList<>();
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
                    DetalleSolicitud d = new DetalleSolicitud();
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

        // Montos estimados (con los precios de hoy)
        BigDecimal subtotal = BigDecimal.ZERO;
        for (DetalleSolicitud d : detalles) {
            d.setDias(dias);
            d.setSubtotal(d.getPrecioDia().multiply(BigDecimal.valueOf(dias)).setScale(2, RoundingMode.HALF_UP));
            subtotal = subtotal.add(d.getSubtotal());
        }
        BigDecimal igv = subtotal.multiply(AlquilerService.IGV).setScale(2, RoundingMode.HALF_UP);

        Solicitud s = new Solicitud();
        s.setClienteId(clienteId);
        s.setUsuarioId(usuarioId);
        s.setDiasSolicitados(dias);
        s.setSubtotal(subtotal);
        s.setIgv(igv);
        s.setTotal(subtotal.add(igv));
        s.setGarantia(subtotal.multiply(AlquilerService.GARANTIA).setScale(2, RoundingMode.HALF_UP));
        s.setObservaciones(obs);
        s.setDetalles(detalles);
        return s;
    }
}
