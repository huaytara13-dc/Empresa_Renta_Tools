package com.rentatools.observer;

import com.rentatools.model.Alquiler;
import com.rentatools.service.ComprobanteService;

/** Observador concreto: emite la boleta o factura cuando se registra un alquiler. */
public class ComprobanteObserver implements AlquilerObserver {

    private final ComprobanteService servicio = new ComprobanteService();

    @Override
    public String nombre() {
        return "Comprobante";
    }

    @Override
    public void alquilerRegistrado(Alquiler alquiler) throws Exception {
        servicio.emitirPara(alquiler.getId());
    }
}
