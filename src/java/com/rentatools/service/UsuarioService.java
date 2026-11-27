package com.rentatools.service;

import com.rentatools.model.Usuario;
import com.rentatools.repository.UsuarioRepository;
import com.rentatools.repository.UsuarioRepositoryImpl;
import com.rentatools.util.PasswordUtil;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/** Reglas de negocio de usuarios: login, validaciones y proteccion de cuentas. */
public class UsuarioService {

    public static final int MAX_INTENTOS = 3;
    public static final int MINUTOS_BLOQUEO = 15;
    public static final int MIN_PASSWORD = 8;
    public static final int MAX_PASSWORD = 72; // limite de BCrypt

    private static final Pattern CORREO = Pattern.compile("^[\\w.+-]+@[\\w-]+(\\.[\\w-]+)+$");
    private static final Pattern USUARIO = Pattern.compile("^[A-Za-z0-9._-]{3,50}$");

    // El servicio depende de la INTERFAZ, no de la implementacion con JDBC
    private final UsuarioRepository repositorio = new UsuarioRepositoryImpl();

    // =====================  LOGIN  =====================
    public ResultadoLogin autenticar(String nombreUsuario, String passwordPlano) throws SQLException {

        Usuario u = repositorio.buscarPorUsuario(nombreUsuario);
        if (u == null) {
            return ResultadoLogin.incorrecto(0);
        }
        if ("INACTIVO".equals(u.getEstado())) {
            return ResultadoLogin.inactivo();
        }
        if ("BLOQUEADO".equals(u.getEstado())) {
            if (bloqueoVigente(u)) {
                return ResultadoLogin.bloqueado();
            }
            repositorio.reiniciarIntentos(u.getId());
        }
        if (PasswordUtil.verificar(passwordPlano, u.getPasswordHash())) {
            repositorio.reiniciarIntentos(u.getId());
            return ResultadoLogin.ok(u);
        }
        int intentos = repositorio.incrementarIntentos(u.getId());
        if (intentos >= MAX_INTENTOS) {
            repositorio.bloquear(u.getId());
            return ResultadoLogin.bloqueado();
        }
        return ResultadoLogin.incorrecto(MAX_INTENTOS - intentos);
    }

    private boolean bloqueoVigente(Usuario u) {
        if (u.getFechaBloqueo() == null) {
            return true;
        }
        return u.getFechaBloqueo().plusMinutes(MINUTOS_BLOQUEO).isAfter(OffsetDateTime.now());
    }

    // =====================  CONSULTAS  =====================
    /** Una pagina del listado, con busqueda opcional (Etapa de paginacion). */
    public com.rentatools.model.Pagina<Usuario> listarPagina(String q, int pagina) throws SQLException {
        return repositorio.listarPagina(q, pagina, com.rentatools.util.Paginacion.TAMANO);
    }

    /** Cantidad de registros por estado, para los indicadores de la pantalla. */
    public java.util.Map<String, Integer> contarPorEstado() throws SQLException {
        return repositorio.contarPorEstado();
    }

    public List<Usuario> listar() throws SQLException {
        return repositorio.listarTodos();
    }

    public Usuario obtener(int id) throws SQLException {
        return repositorio.buscarPorId(id);
    }

    public int contarTodos() throws SQLException {
        return repositorio.contarTodos();
    }

    public int contarPorEstado(String estado) throws SQLException {
        return repositorio.contarPorEstado(estado);
    }

    // =====================  CRUD  =====================
    public void crear(Usuario u, String password, String confirmacion)
            throws ValidacionException, SQLException {

        normalizar(u);
        List<String> errores = validarDatos(u, 0);
        validarPassword(password, confirmacion, true, errores);
        lanzarSiHay(errores);

        u.setPasswordHash(PasswordUtil.hash(password));
        try {
            repositorio.crear(u);
        } catch (SQLException e) {
            traducirDuplicado(e);
        }
    }

    public void actualizar(Usuario u, String password, String confirmacion, int idSolicitante)
            throws ValidacionException, SQLException {

        Usuario existente = repositorio.buscarPorId(u.getId());
        if (existente == null) {
            throw new ValidacionException("El usuario no existe");
        }

        normalizar(u);
        List<String> errores = validarDatos(u, u.getId());
        validarPassword(password, confirmacion, false, errores);

        // Proteccion: el administrador no puede quitarse a si mismo el acceso
        if (u.getId() == idSolicitante
                && (!"ADMIN".equals(u.getRol()) || !"ACTIVO".equals(u.getEstado()))) {
            errores.add("No puede cambiar su propio rol ni desactivar su propia cuenta");
        }
        lanzarSiHay(errores);

        try {
            repositorio.actualizar(u);
            if (password != null && !password.isEmpty()) {
                repositorio.cambiarPassword(u.getId(), PasswordUtil.hash(password));
            }
            // Si queda ACTIVO, se limpian los intentos y el bloqueo
            if ("ACTIVO".equals(u.getEstado())
                    && ("BLOQUEADO".equals(existente.getEstado()) || existente.getIntentosFallidos() > 0)) {
                repositorio.reiniciarIntentos(u.getId());
            }
        } catch (SQLException e) {
            traducirDuplicado(e);
        }
    }

    public void eliminar(int id, int idSolicitante) throws ValidacionException, SQLException {
        if (id == idSolicitante) {
            throw new ValidacionException("No puede eliminar su propia cuenta");
        }
        if (repositorio.buscarPorId(id) == null) {
            throw new ValidacionException("El usuario no existe");
        }
        repositorio.eliminar(id);
    }

    public void desbloquear(int id) throws ValidacionException, SQLException {
        if (repositorio.buscarPorId(id) == null) {
            throw new ValidacionException("El usuario no existe");
        }
        repositorio.reiniciarIntentos(id);
    }

    // =====================  VALIDACIONES  =====================
    private void normalizar(Usuario u) {
        u.setNombres(recortar(u.getNombres()));
        u.setApellidos(recortar(u.getApellidos()));
        u.setUsuario(recortar(u.getUsuario()));
        u.setCorreo(recortar(u.getCorreo()).toLowerCase());
        u.setRol(recortar(u.getRol()));
        u.setEstado(recortar(u.getEstado()));
    }

    private String recortar(String s) {
        return s == null ? "" : s.trim();
    }

    private List<String> validarDatos(Usuario u, int idExcluir) throws SQLException {
        List<String> errores = new ArrayList<>();

        if (u.getNombres().isEmpty()) {
            errores.add("Los nombres son obligatorios");
        } else if (u.getNombres().length() > 80) {
            errores.add("Los nombres no pueden superar 80 caracteres");
        }

        if (u.getApellidos().isEmpty()) {
            errores.add("Los apellidos son obligatorios");
        } else if (u.getApellidos().length() > 80) {
            errores.add("Los apellidos no pueden superar 80 caracteres");
        }

        if (!CORREO.matcher(u.getCorreo()).matches() || u.getCorreo().length() > 120) {
            errores.add("Ingrese un correo electronico valido");
        } else if (repositorio.existeCorreo(u.getCorreo(), idExcluir)) {
            errores.add("El correo ya esta registrado");
        }

        if (!USUARIO.matcher(u.getUsuario()).matches()) {
            errores.add("El usuario debe tener de 3 a 50 caracteres (letras, numeros, punto, guion o guion bajo)");
        } else if (repositorio.existeUsuario(u.getUsuario(), idExcluir)) {
            errores.add("El nombre de usuario ya esta registrado");
        }

        if (!"ADMIN".equals(u.getRol()) && !"EMPLEADO".equals(u.getRol())) {
            errores.add("El rol seleccionado no es valido");
        }
        if (!"ACTIVO".equals(u.getEstado()) && !"INACTIVO".equals(u.getEstado())
                && !"BLOQUEADO".equals(u.getEstado())) {
            errores.add("El estado seleccionado no es valido");
        }
        return errores;
    }

    private void validarPassword(String password, String confirmacion, boolean obligatoria,
                                 List<String> errores) {
        boolean vacia = password == null || password.isEmpty();
        if (vacia) {
            if (obligatoria) {
                errores.add("La contraseña es obligatoria");
            }
            return;
        }
        if (password.length() < MIN_PASSWORD) {
            errores.add("La contraseña debe tener al menos " + MIN_PASSWORD + " caracteres");
        }
        if (password.length() > MAX_PASSWORD) {
            errores.add("La contraseña no puede superar " + MAX_PASSWORD + " caracteres");
        }
        if (!password.equals(confirmacion)) {
            errores.add("Las contraseñas no coinciden");
        }
    }

    private void lanzarSiHay(List<String> errores) throws ValidacionException {
        if (!errores.isEmpty()) {
            throw new ValidacionException(errores);
        }
    }

    // 23505 = violacion de UNIQUE en PostgreSQL (dos admins guardando lo mismo a la vez)
    private void traducirDuplicado(SQLException e) throws ValidacionException, SQLException {
        if ("23505".equals(e.getSQLState())) {
            throw new ValidacionException("El usuario o el correo ya esta registrado");
        }
        throw e;
    }
}
