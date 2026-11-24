package com.roberto.trazys.cliente.data.repository

import com.roberto.trazys.cliente.data.error.ErrorAuth
import com.roberto.trazys.cliente.data.error.ErrorConsulta
import com.roberto.trazys.cliente.data.error.ErrorCuenta
import com.roberto.trazys.cliente.data.error.ErrorRemoto
import com.roberto.trazys.cliente.data.firebase.FuenteAutenticacion
import com.roberto.trazys.cliente.data.firebase.FuenteSolicitudes
import com.roberto.trazys.cliente.model.EstadoSolicitud
import com.roberto.trazys.cliente.model.SolicitudBaja
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class SolicitudBajaRepositoryTest {

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

    private class FakeSolicitudes(
        var lista: List<SolicitudBaja> = emptyList(),
        var error: Exception? = null
    ) : FuenteSolicitudes {
        var creada: SolicitudBaja? = null
        var motivoRecibido: String? = null
        var vecesCreada = 0

        override suspend fun solicitudesDe(
            clienteId: Int,
            negocioId: String
        ): List<SolicitudBaja> {
            error?.let { throw it }
            return lista
        }

        override suspend fun crearSolicitud(
            idSolicitud: String,
            clienteId: Int,
            negocioId: String,
            firebaseUid: String,
            fechaSolicitud: Long,
            motivo: String?
        ) {
            error?.let { throw it }
            vecesCreada++
            motivoRecibido = motivo
            creada = SolicitudBaja(
                idSolicitud = idSolicitud,
                negocioId = negocioId,
                idCliente = clienteId,
                firebaseUid = firebaseUid,
                fechaSolicitud = fechaSolicitud,
                estado = EstadoSolicitud.PENDIENTE,
                motivo = motivo
            )
        }
    }

    private fun solicitud(estado: EstadoSolicitud) = SolicitudBaja(
        idSolicitud = "baja_42_1000",
        negocioId = "negocio-1",
        idCliente = 42,
        firebaseUid = "uid-1",
        fechaSolicitud = 1_000L,
        estado = estado
    )

    @Test
    fun cargarSinSesionNoConsulta() = runTest {
        val fuente = FakeSolicitudes()
        val repo = SolicitudBajaRepository(FakeAuth(uid = null), fuente)

        val resultado = repo.cargar(42, "negocio-1")

        assertEquals(ErrorConsulta.SIN_SESION, resultado.error)
    }

    @Test
    fun cargarConParametrosInvalidosDevuelveError() = runTest {
        val repo = SolicitudBajaRepository(FakeAuth(), FakeSolicitudes())

        val resultado = repo.cargar(0, "")

        assertEquals(ErrorConsulta.DESCONOCIDO, resultado.error)
    }

    @Test
    fun cargarDevuelveLasSolicitudes() = runTest {
        val fuente = FakeSolicitudes(lista = listOf(solicitud(EstadoSolicitud.RECHAZADA)))
        val repo = SolicitudBajaRepository(FakeAuth(), fuente)

        val resultado = repo.cargar(42, "negocio-1")

        assertNull(resultado.error)
        assertEquals(1, resultado.solicitudes.size)
        assertTrue(resultado.exito)
    }

    @Test
    fun errorRemotoAlCargarSeTraduce() = runTest {
        val fuente = FakeSolicitudes(error = ErrorRemoto(ErrorAuth.SIN_CONEXION))
        val repo = SolicitudBajaRepository(FakeAuth(), fuente)

        val resultado = repo.cargar(42, "negocio-1")

        assertEquals(ErrorConsulta.SIN_CONEXION, resultado.error)
    }

    @Test
    fun solicitarSinSesionNoCrea() = runTest {
        val fuente = FakeSolicitudes()
        val repo = SolicitudBajaRepository(FakeAuth(uid = null), fuente)

        val resultado = repo.solicitarBaja(42, "negocio-1", null)

        assertFalse(resultado.exito)
        assertEquals(ErrorCuenta.SIN_SESION, resultado.error)
        assertEquals(0, fuente.vecesCreada)
    }

    @Test
    fun solicitarConPendienteNoDuplica() = runTest {
        val fuente = FakeSolicitudes(lista = listOf(solicitud(EstadoSolicitud.PENDIENTE)))
        val repo = SolicitudBajaRepository(FakeAuth(), fuente)

        val resultado = repo.solicitarBaja(42, "negocio-1", null)

        assertFalse(resultado.exito)
        assertEquals(ErrorCuenta.BAJA_PENDIENTE, resultado.error)
        assertEquals(0, fuente.vecesCreada)
    }

    @Test
    fun solicitarCreaConIdDeterministaYMotivo() = runTest {
        val fuente = FakeSolicitudes(
            lista = listOf(solicitud(EstadoSolicitud.RECHAZADA))
        )
        val repo = SolicitudBajaRepository(FakeAuth(), fuente) { 5_000L }

        val resultado = repo.solicitarBaja(42, "negocio-1", "Me mudo")

        assertTrue(resultado.exito)
        assertEquals(1, fuente.vecesCreada)
        assertEquals("baja_42_5000", fuente.creada?.idSolicitud)
        assertEquals("uid-1", fuente.creada?.firebaseUid)
        assertEquals("Me mudo", fuente.motivoRecibido)
    }

    @Test
    fun errorDeLaFuenteAlCrearSeTraduce() = runTest {
        val fuente = FakeSolicitudes(error = ErrorRemoto(ErrorAuth.SIN_PERMISOS))
        val repo = SolicitudBajaRepository(FakeAuth(), fuente) { 5_000L }

        val resultado = repo.solicitarBaja(42, "negocio-1", null)

        assertFalse(resultado.exito)
        assertEquals(ErrorCuenta.SIN_PERMISOS, resultado.error)
    }
}
