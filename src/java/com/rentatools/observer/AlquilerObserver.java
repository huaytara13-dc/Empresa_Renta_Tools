package com.rentatools.observer;

import com.rentatools.model.Alquiler;

/**
 * Patron OBSERVER - interfaz del observador.
 * Quien quiera enterarse de que se registro un alquiler implementa esta interfaz y se suscribe
 * en AlquilerEventos. El alquiler no sabe quien lo escucha ni que hace cada uno.
 */
public interface AlquilerObserver {

    /** Nombre para el registro de eventos (log). */
    String nombre();

    /** Se llama despues de que el alquiler quedo guardado. */
    void alquilerRegistrado(Alquiler alquiler) throws Exception;
}
