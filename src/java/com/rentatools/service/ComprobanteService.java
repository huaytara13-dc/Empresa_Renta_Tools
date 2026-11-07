package com.rentatools.service;

import com.rentatools.model.Alquiler;
import com.rentatools.model.Cliente;
import com.rentatools.model.Comprobante;
import com.rentatools.repository.AlquilerRepository;
import com.rentatools.repository.AlquilerRepositoryImpl;
import com.rentatools.repository.ClienteRepository;
import com.rentatools.repository.ClienteRepositoryImpl;
import com.rentatools.repository.ComprobanteRepository;
import com.rentatools.repository.ComprobanteRepositoryImpl;
import java.sql.SQLException;
import java.util.List;

/**
 * Reglas del comprobante de pago: factura para clientes con RUC y boleta para los demas.
 * Es una simulacion academica: no se envia a SUNAT ni tiene validez tributaria.
 */
public class ComprobanteService {

    private final ComprobanteRepository repositorio = new ComprobanteRepositoryImpl();
    private final AlquilerRepository alquileres = new AlquilerRepositoryImpl();
    private final ClienteRepository clientes = new ClienteRepositoryImpl();

    public static String etiquetaTipo(String tipo) {
        return "FACTURA".equals(tipo) ? "Factura" : "Boleta de venta";
    }

    // ---------- Consultas ----------
    public List<Comprobante> listar() throws SQLException {
        return repositorio.listarTodos();
    }

    public Comprobante obtener(int id) throws SQLException {
        return repositorio.buscarPorId(id);
    }

    public Comprobante obtenerPorAlquiler(int alquilerId) throws SQLException {
        return repositorio.buscarPorAlquiler(alquilerId);
    }

    // ---------- Emision ----------
    /**
     * Emite el comprobante del alquiler. Si ya existe, lo devuelve sin crear otro (se puede llamar
     * varias veces: desde el Observer o desde el boton de la pantalla del alquiler).
     */
    public Comprobante emitirPara(int alquilerId) throws ValidacionException, SQLException {
        Alquiler a = alquileres.buscarPorId(alquilerId);
        if (a == null) {
            throw new ValidacionException("El alquiler no existe");
        }
        if ("ANULADO".equals(a.getEstado())) {
            throw new ValidacionException("No se puede emitir un comprobante para un alquiler anulado");
        }

        Comprobante existente = repositorio.buscarPorAlquiler(alquilerId);
        if (existente != null) {
            return existente;
        }

        Cliente cliente = clientes.buscarPorId(a.getClienteId());
        if (cliente == null) {
            throw new ValidacionException("El cliente del alquiler no existe");
        }

        Comprobante c = new Comprobante();
        c.setAlquilerId(alquilerId);
        c.setTipo("RUC".equals(cliente.getTipoDocumento()) ? "FACTURA" : "BOLETA");
        c.setFechaEmision(AlquilerService.hoy());
        c.setClienteDocumento(cliente.getTipoDocumento() + " " + cliente.getNumeroDocumento());
        c.setClienteNombre((cliente.getNombres() + " " + cliente.getApellidos()).trim());
        c.setClienteDireccion(cliente.getDireccion());
        c.setSubtotal(a.getSubtotal());
        c.setIgv(a.getIgv());
        c.setTotal(a.getTotal());

        try {
            repositorio.emitir(c);
        } catch (SQLException e) {
            // 23505 = otro proceso emitio el comprobante de este alquiler al mismo tiempo
            if ("23505".equals(e.getSQLState())) {
                Comprobante ya = repositorio.buscarPorAlquiler(alquilerId);
                if (ya != null) {
                    return ya;
                }
            }
            throw e;
        }
        return c;
    }
}
