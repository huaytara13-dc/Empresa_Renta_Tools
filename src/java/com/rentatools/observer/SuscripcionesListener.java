package com.rentatools.observer;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

/**
 * Al iniciar la aplicacion, registra (suscribe) a los observadores del alquiler.
 * Para agregar otra reaccion (por ejemplo, enviar un correo) basta con crear otro AlquilerObserver
 * y suscribirlo aqui, sin tocar AlquilerService.
 */
@WebListener
public class SuscripcionesListener implements ServletContextListener {

    private final AlquilerObserver comprobante = new ComprobanteObserver();
    private final AlquilerObserver guia = new GuiaEntregaObserver();

    @Override
    public void contextInitialized(ServletContextEvent evento) {
        AlquilerEventos eventos = AlquilerEventos.getInstancia();
        eventos.suscribir(comprobante);
        eventos.suscribir(guia);
        System.out.println("Observadores suscritos al registro de alquileres: " + eventos.cantidadSuscriptores());
    }

    @Override
    public void contextDestroyed(ServletContextEvent evento) {
        AlquilerEventos eventos = AlquilerEventos.getInstancia();
        eventos.cancelarSuscripcion(comprobante);
        eventos.cancelarSuscripcion(guia);
    }
}
