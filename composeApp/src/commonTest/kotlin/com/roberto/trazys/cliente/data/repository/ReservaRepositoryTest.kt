package com.roberto.trazys.cliente.data.repository

import com.roberto.trazys.cliente.data.error.ErrorAuth
import com.roberto.trazys.cliente.data.error.ErrorRemoto
import com.roberto.trazys.cliente.data.error.ErrorReserva
import com.roberto.trazys.cliente.data.error.ResultadoReserva
import com.roberto.trazys.cliente.data.firebase.FuenteAutenticacion
import com.roberto.trazys.cliente.data.firebase.FuenteReservas
import com.roberto.trazys.cliente.model.Reserva
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ReservaRepositoryTest {

    private class FakeAuth(override var uid: String? = "uid-1") : FuenteAutenticacion {
        override var email: String? = "cliente@centro.es"
        override suspend fun registrar(email: String, contrasena: String): String = "uid-1"
        override suspend fun iniciarSesion(email: String, contrasena: String): String = "uid-1"
        override suspend fun cerrarSesion() {
            uid = null
        }

        override suspend fun enviarRecuperacion(email: String) = Unit
        override suspend fun reautenticar(contrasena: String) = Unit
        override suspend fun cambiarContrasena(nueva: String) = Unit
        override suspend fun borrarCuenta() = Unit
    }

    private class FakeReservas(
        var resultado: ResultadoReserva = ResultadoReserva.correcto(),
        var errorLeer: ErrorRemoto? = null,
        var errorOperar: ErrorRemoto? = null
    ) : FuenteReservas {
        var sesionReservada: Int? = null
        var sesionCancelada: Int? = null
        var reservas: List<Reserva> = emptyList()

        override suspend fun reservar(sesionId: Int): ResultadoReserva {
            errorOperar?.let { throw it }
            sesionReservada = sesionId
            return resultado
        }

        override suspend fun cancelar(sesionId: Int): ResultadoReserva {
            errorOperar?.let { throw it }
            sesionCancelada = sesionId
            return resultado
        }

        override suspend fun reservasDe(clienteId: Int, negocioId: String): List<Reserva> {
            errorLeer?.let { throw it }
            return reservas
        }
    }

    @Test
    fun reservarSinSesionNoLlamaALaFuente() = runTest {
        val fuente = FakeReservas()
        val repo = ReservaRepository(FakeAuth(uid = null), fuente)

        val resultado = repo.reservar(10)

        assertFalse(resultado.exito)
        assertEquals(ErrorReserva.SIN_SESION, resultado.error)
        assertNull(fuente.sesionReservada)
    }

    @Test
    fun reservarConSesionInvalidaNoLlamaALaFuente() = runTest {
        val fuente = FakeReservas()
        val repo = ReservaRepository(FakeAuth(), fuente)

        val resultado = repo.reservar(0)

        assertFalse(resultado.exito)
        assertEquals(ErrorReserva.SESION_INVALIDA, resultado.error)
        assertNull(fuente.sesionReservada)
    }

    @Test
    fun reservarDelegaEnLaFuente() = runTest {
        val fuente = FakeReservas()
        val repo = ReservaRepository(FakeAuth(), fuente)

        val resultado = repo.reservar(10)

        assertTrue(resultado.exito)
        assertEquals(10, fuente.sesionReservada)
    }

    @Test
    fun cancelarDelegaEnLaFuente() = runTest {
        val fuente = FakeReservas()
        val repo = ReservaRepository(FakeAuth(), fuente)

        val resultado = repo.cancelar(20)

        assertTrue(resultado.exito)
        assertEquals(20, fuente.sesionCancelada)
    }

    @Test
    fun errorDeNegocioDeLaFuenteSePropaga() = runTest {
        val fuente = FakeReservas(resultado = ResultadoReserva.fallo(ErrorReserva.NO_VINCULADO))
        val repo = ReservaRepository(FakeAuth(), fuente)

        val resultado = repo.reservar(10)

        assertFalse(resultado.exito)
        assertEquals(ErrorReserva.NO_VINCULADO, resultado.error)
    }

    @Test
    fun errorRemotoSeTraduceASinConexion() = runTest {
        val fuente = FakeReservas(errorOperar = ErrorRemoto(ErrorAuth.SIN_CONEXION))
        val repo = ReservaRepository(FakeAuth(), fuente)

        val resultado = repo.cancelar(20)

        assertFalse(resultado.exito)
        assertEquals(ErrorReserva.SIN_CONEXION, resultado.error)
    }

    @Test
    fun cargarReservasSinSesionDevuelveError() = runTest {
        val repo = ReservaRepository(FakeAuth(uid = null), FakeReservas())

        val resultado = repo.cargarReservas(1, "negocio-1")

        assertFalse(resultado.exito)
        assertEquals(com.roberto.trazys.cliente.data.error.ErrorConsulta.SIN_SESION, resultado.error)
    }

    @Test
    fun cargarReservasDevuelveLasPropias() = runTest {
        val fuente = FakeReservas().apply {
            reservas = listOf(Reserva("1_10", "negocio-1", 10, 1, 0L))
        }
        val repo = ReservaRepository(FakeAuth(), fuente)

        val resultado = repo.cargarReservas(1, "negocio-1")

        assertTrue(resultado.exito)
        assertEquals(1, resultado.reservas.size)
        assertEquals("1_10", resultado.reservas.first().idReserva)
    }

    @Test
    fun cargarReservasTraduceElErrorRemoto() = runTest {
        val fuente = FakeReservas(errorLeer = ErrorRemoto(ErrorAuth.SIN_PERMISOS))
        val repo = ReservaRepository(FakeAuth(), fuente)

        val resultado = repo.cargarReservas(1, "negocio-1")

        assertFalse(resultado.exito)
        assertEquals(com.roberto.trazys.cliente.data.error.ErrorConsulta.SIN_PERMISOS, resultado.error)
    }
}
