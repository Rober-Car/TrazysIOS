package com.roberto.trazys.cliente.util

import com.roberto.trazys.cliente.model.Cliente
import com.roberto.trazys.cliente.model.EstadoCliente
import com.roberto.trazys.cliente.model.EstadoIndicadorCliente
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class EstadoHomeTest {

    private fun ficha(
        estado: EstadoCliente,
        fechaFin: Long? = null,
        fechaBaja: Long? = null
    ) = Cliente(
        idCliente = 1,
        negocioId = "negocio-1",
        firebaseUid = "uid-1",
        nombre = "Ana",
        apellidos = "García",
        dni = "12345678Z",
        telefono = "600000000",
        email = null,
        foto = "",
        fechaNacimiento = null,
        fechaRegistro = 0L,
        fechaAlta = null,
        fechaBaja = fechaBaja,
        estado = estado,
        serviciosContratados = emptyList(),
        fechaInicioActual = null,
        fechaFinActual = fechaFin
    )

    @Test
    fun activoVigenteMuestraActivoConFechaFin() {
        val resultado = estadoHomeDe(ficha(EstadoCliente.ACTIVO, fechaFin = 2_000L), ahora = 1_000L)
        assertEquals(EstadoIndicadorCliente.ACTIVO, resultado.estado)
        assertEquals(2_000L, resultado.fechaRelevante)
        assertNull(resultado.error)
    }

    @Test
    fun activoConPeriodoVencidoMuestraPagoVencido() {
        val resultado = estadoHomeDe(ficha(EstadoCliente.ACTIVO, fechaFin = 500L), ahora = 1_000L)
        assertEquals(EstadoIndicadorCliente.PAGO_VENCIDO, resultado.estado)
        assertEquals(500L, resultado.fechaRelevante)
    }

    @Test
    fun activoSinFechaFinSigueActivo() {
        val resultado = estadoHomeDe(ficha(EstadoCliente.ACTIVO), ahora = 1_000L)
        assertEquals(EstadoIndicadorCliente.ACTIVO, resultado.estado)
        assertNull(resultado.fechaRelevante)
    }

    @Test
    fun bajaUsaFechaDeBaja() {
        val resultado = estadoHomeDe(ficha(EstadoCliente.BAJA, fechaBaja = 800L), ahora = 1_000L)
        assertEquals(EstadoIndicadorCliente.BAJA, resultado.estado)
        assertEquals(800L, resultado.fechaRelevante)
    }

    @Test
    fun registradoYArchivadoSeRepresentanSinFecha() {
        assertEquals(
            EstadoIndicadorCliente.REGISTRADO,
            estadoHomeDe(ficha(EstadoCliente.REGISTRADO), ahora = 1_000L).estado
        )
        assertEquals(
            EstadoIndicadorCliente.ARCHIVADO,
            estadoHomeDe(ficha(EstadoCliente.ARCHIVADO), ahora = 1_000L).estado
        )
    }

    @Test
    fun morosoNoSeConvierteYDevuelveError() {
        val resultado = estadoHomeDe(ficha(EstadoCliente.MOROSO), ahora = 1_000L)
        assertNull(resultado.estado)
        assertEquals(MENSAJE_ESTADO_NO_COMPATIBLE, resultado.error)
    }
}
