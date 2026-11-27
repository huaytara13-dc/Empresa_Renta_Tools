package com.rentatools.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Patron creacional SINGLETON.
 * Una unica instancia de ConexionBD y una unica conexion compartida para las consultas simples.
 * Para operaciones con TRANSACCION se usa nuevaConexion(): una conexion propia, que no se
 * comparte, para que otras peticiones no queden dentro de la misma transaccion.
 *
 * Credenciales (no van en el codigo), en este orden:
 *   1) Variables de entorno: DB_URL, DB_USER, DB_PASS
 *   2) Archivo C:\Users\TU_USUARIO\rentatools.properties
 */
public final class ConexionBD {

    private static ConexionBD instancia;

    private Connection conexion;

    /** Solo se pregunta a la BD si la conexion sigue viva cuando estuvo inactiva mas de este tiempo
     *  (isValid hace un viaje a la red; hacerlo en cada consulta duplicaba la latencia). */
    private static final long INTERVALO_VALIDACION_MS = 30_000;
    private long ultimoUso = 0;

    private final String url;
    private final String user;
    private final String pass;

    private ConexionBD() {
        Properties archivo = cargarArchivo();
        this.url = valor("DB_URL", archivo);
        this.user = valor("DB_USER", archivo);
        this.pass = valor("DB_PASS", archivo);
    }

    public static synchronized ConexionBD getInstancia() {
        if (instancia == null) {
            instancia = new ConexionBD();
        }
        return instancia;
    }

    /** Conexion compartida (Singleton). No la cierre quien la usa. */
    public synchronized Connection getConexion() throws SQLException {
        long ahora = System.currentTimeMillis();
        boolean revisar = conexion == null || conexion.isClosed()
                || (ahora - ultimoUso > INTERVALO_VALIDACION_MS && !conexion.isValid(2));
        if (revisar) {
            conexion = abrirConexion();
        }
        ultimoUso = ahora;
        return conexion;
    }

    /** Conexion independiente para transacciones. Quien la pide debe cerrarla (try-with-resources). */
    public Connection nuevaConexion() throws SQLException {
        return abrirConexion();
    }

    public synchronized void cerrar() {
        try {
            if (conexion != null && !conexion.isClosed()) {
                conexion.close();
            }
        } catch (SQLException ignorada) {
        }
        conexion = null;
    }

    private Connection abrirConexion() throws SQLException {
        if (url == null || user == null || pass == null) {
            throw new SQLException("Faltan DB_URL, DB_USER o DB_PASS "
                    + "(variables de entorno o archivo rentatools.properties)");
        }
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            throw new SQLException("No se encontro el driver (postgresql-42.x.jar)", e);
        }
        return DriverManager.getConnection(url, user, pass);
    }

    // ---------- Lectura de credenciales ----------
    private static Properties cargarArchivo() {
        Properties p = new Properties();
        File f = new File(System.getProperty("user.home"), "rentatools.properties");
        if (f.exists()) {
            try (FileInputStream in = new FileInputStream(f)) {
                p.load(in);
            } catch (IOException e) {
                System.out.println("No se pudo leer " + f + ": " + e.getMessage());
            }
        }
        return p;
    }

    private static String valor(String clave, Properties archivo) {
        String v = System.getenv(clave);
        return (v == null || v.isEmpty()) ? archivo.getProperty(clave) : v;
    }

    // Prueba rapida: clic derecho -> Run File
    public static void main(String[] args) {
        ConexionBD a = ConexionBD.getInstancia();
        ConexionBD b = ConexionBD.getInstancia();
        System.out.println("Misma instancia?: " + (a == b));
        try {
            Connection c1 = a.getConexion();
            Connection c2 = b.getConexion();
            System.out.println("Misma conexion?: " + (c1 == c2));
            System.out.println("Conexion exitosa a: " + c1.getCatalog());
        } catch (SQLException e) {
            System.out.println("Error de conexion: " + e.getMessage());
            Throwable c = e.getCause();
            while (c != null) {
                System.out.println("  Causa: " + c);
                c = c.getCause();
            }
        }
    }
}