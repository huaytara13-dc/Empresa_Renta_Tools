package com.rentatools.observer;

import com.rentatools.model.Alquiler;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Patron OBSERVER - el sujeto (publicador de eventos).
 * Mantiene la lista de suscriptores y les avisa cuando se registra un alquiler.
 * Es un Singleton: una sola lista de suscriptores para toda la aplicacion.
 */
public final class AlquilerEventos {

    private static final AlquilerEventos INSTANCIA = new AlquilerEventos();

    // Lista segura para varios hilos: se puede suscribir mientras otro hilo notifica
    private final List<AlquilerObserver> suscriptores = new CopyOnWriteArrayList<>();

    private AlquilerEventos() { }

    public static AlquilerEventos getInstancia() {
        return INSTANCIA;
    }

    public void suscribir(AlquilerObserver observador) {
        if (observador != null && !suscriptores.contains(observador)) {
            suscriptores.add(observador);
        }
    }

    public void cancelarSuscripcion(AlquilerObserver observador) {
        suscriptores.remove(observador);
    }

    public int cantidadSuscriptores() {
        return suscriptores.size();
    }

    /**
     * Avisa a todos los suscriptores. Si uno falla, los demas igual se ejecutan y el alquiler
     * ya guardado no se pierde: el error queda en el log y el documento puede emitirse a mano
     * desde la pantalla del alquiler.
     */
    public void notificar(Alquiler alquiler) {
        for (AlquilerObserver o : suscriptores) {
            try {
                o.alquilerRegistrado(alquiler);
            } catch (Exception e) {
                System.err.println("[Observer " + o.nombre() + "] no se pudo procesar el alquiler N.º "
                        + alquiler.getId() + ": " + e.getMessage());
            }
        }
    }
}
