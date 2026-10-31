package com.rentatools.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Patron creacional SINGLETON (Etapa 3).
 * Garantiza que exista una unica instancia de ConexionBD en toda la aplicacion
 * y que esa instancia reutilice una sola conexion a la base de datos.
 *
 * Credenciales (no van en el codigo), en este orden:
 *   1) Variables de entorno: DB_URL, DB_USER, DB_PASS
 *   2) Archivo C:\Users\TU_USUARIO\rentatools.properties
 */
public final class ConexionBD {

    // 1) Atributo estatico privado: guarda la unica instancia
    private static ConexionBD instancia;

    // 2) La unica conexion compartida
    private Connection conexion;

    private final String url;
    private final String user;
    private final String pass;

    // 3) Constructor PRIVADO: nadie fuera de la clase puede hacer "new ConexionBD()"
    private ConexionBD() {
        Properties archivo = cargarArchivo();
        this.url = valor("DB_URL", archivo);
        this.user = valor("DB_USER", archivo);
        this.pass = valor("DB_PASS", archivo);
    }

    // 4) Punto de acceso global: crea la instancia solo la primera vez
    public static synchronized ConexionBD getInstancia() {
        if (instancia == null) {
            instancia = new ConexionBD();
        }
        return instancia;
    }

    // 5) Entrega la conexion; si se cerro o caduco (Supabase la corta por inactividad), se reconecta
    public synchronized Connection getConexion() throws SQLException {
        if (url == null || user == null || pass == null) {
            throw new SQLException("Faltan DB_URL, DB_USER o DB_PASS "
                    + "(variables de entorno o archivo rentatools.properties)");
        }
        if (conexion == null || conexion.isClosed() || !conexion.isValid(2)) {
            try {
                Class.forName("org.postgresql.Driver");
            } catch (ClassNotFoundException e) {
                throw new SQLException("No se encontro el driver (postgresql-42.x.jar)", e);
            }
            conexion = DriverManager.getConnection(url, user, pass);
        }
        return conexion;
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

    // Prueba rapida: clic derecho -> Run File. Comprueba tambien que sea una unica instancia.
    public static void main(String[] args) {
        ConexionBD a = ConexionBD.getInstancia();
        ConexionBD b = ConexionBD.getInstancia();
        System.out.println("¿Misma instancia?: " + (a == b));
        try {
            Connection c1 = a.getConexion();
            Connection c2 = b.getConexion();
            System.out.println("¿Misma conexion?: " + (c1 == c2));
            System.out.println("Conexion exitosa a: " + c1.getCatalog());
        } catch (SQLException e) {
            System.out.println("Error de conexion: " + e.getMessage());
        }
    }
}