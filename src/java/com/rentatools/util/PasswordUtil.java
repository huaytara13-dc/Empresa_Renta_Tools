package com.rentatools.util;

import org.mindrot.jbcrypt.BCrypt;

/** Cifrado de contraseñas con BCrypt (hash con sal; no se puede "desencriptar"). */
public final class PasswordUtil {

    private PasswordUtil() { }

    public static String hash(String plano) {
        return BCrypt.hashpw(plano, BCrypt.gensalt(10));
    }

    public static boolean verificar(String plano, String hash) {
        try {
            return BCrypt.checkpw(plano, hash);
        } catch (IllegalArgumentException e) {
            return false; // el hash guardado no tiene formato BCrypt
        }
    }

    // Util para generar un hash a mano: clic derecho -> Run File
    public static void main(String[] args) {
        System.out.println(hash("admin123"));
    }
}