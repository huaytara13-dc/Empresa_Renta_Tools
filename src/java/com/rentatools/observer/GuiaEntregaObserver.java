package com.rentatools.observer;

import com.rentatools.model.Alquiler;
import com.rentatools.service.GuiaService;

/** Observador concreto: emite la guia de entrega cuando se registra un alquiler. */
public class GuiaEntregaObserver implements AlquilerObserver {

    private final GuiaService servicio = new GuiaService();

    @Override
    public String nombre() {
        return "Guía de entrega";
    }

    @Override
    public void alquilerRegistrado(Alquiler alquiler) throws Exception {
        servicio.emitirPara(alquiler.getId());
    }
}
