package com.roberto.trazys.cliente.model

import kotlin.test.Test
import kotlin.test.assertEquals

class ModelosTest {

    @Test
    fun estadoReservaPriorizaReservadaYLuegoCompleta() {
        assertEquals(EstadoReserva.RESERVADA, EstadoReserva.de(true, 0))
        assertEquals(EstadoReserva.COMPLETA, EstadoReserva.de(false, 0))
        assertEquals(EstadoReserva.RESERVAR, EstadoReserva.de(false, 3))
    }

    @Test
    fun losEstadosCoincidenConElContratoRemoto() {
        assertEquals(
            listOf("ACTIVO", "MOROSO", "BAJA", "ARCHIVADO", "REGISTRADO"),
            EstadoCliente.entries.map { it.name }
        )
        assertEquals(
            listOf("PENDIENTE", "ACEPTADA", "RECHAZADA"),
            EstadoSolicitud.entries.map { it.name }
        )
    }
}
