package com.roberto.trazys.cliente.data.repository

import com.roberto.trazys.cliente.data.error.ErrorAuth
import com.roberto.trazys.cliente.data.error.ErrorConsulta
import com.roberto.trazys.cliente.data.error.ErrorRemoto
import com.roberto.trazys.cliente.data.firebase.FuenteNotificaciones
import com.roberto.trazys.cliente.model.Notificacion
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class NotificacionRepositoryTest {

    private fun notificacion(id: String) = Notificacion(
        id = id,
        notificacionId = "n-$id",
        titulo = "Aviso",
        mensaje = "Mensaje",
        tipo = "MANUAL",
        origen = "MANUAL",
        fechaEnvio = 1_000L,
        leida = false
    )

    private class FakeNotificaciones(
        var lista: List<Notificacion> = emptyList(),
        var errorLeer: Exception? = null,
        var resultadoEscritura: Boolean = true
    ) : FuenteNotificaciones {
        var marcada: String? = null
        var eliminada: String? = null

        override suspend fun notificacionesDe(
            clienteId: Int,
            negocioId: String
        ): List<Notificacion> {
            errorLeer?.let { throw it }
            return lista
        }

        override suspend fun marcarLeida(docId: String): Boolean {
            marcada = docId
            return resultadoEscritura
        }

        override suspend fun eliminar(docId: String): Boolean {
            eliminada = docId
            return resultadoEscritura
        }
    }

    @Test
    fun cargarDevuelveLasNotificacionesDeLaFuente() = runTest {
        val fuente = FakeNotificaciones(lista = listOf(notificacion("1"), notificacion("2")))
        val repo = NotificacionRepository(fuente)

        val resultado = repo.cargar(42, "negocio-1")

        assertNull(resultado.error)
        assertEquals(2, resultado.notificaciones.size)
    }

    @Test
    fun errorDePermisosAlCargarSeTraduce() = runTest {
        val fuente = FakeNotificaciones(
            errorLeer = ErrorRemoto(ErrorAuth.SIN_PERMISOS)
        )
        val repo = NotificacionRepository(fuente)

        val resultado = repo.cargar(42, "negocio-1")

        assertEquals(ErrorConsulta.SIN_PERMISOS, resultado.error)
        assertTrue(resultado.notificaciones.isEmpty())
    }

    @Test
    fun errorDesconocidoAlCargarSeTraduce() = runTest {
        val fuente = FakeNotificaciones(errorLeer = IllegalStateException("boom"))
        val repo = NotificacionRepository(fuente)

        val resultado = repo.cargar(42, "negocio-1")

        assertEquals(ErrorConsulta.DESCONOCIDO, resultado.error)
    }

    @Test
    fun marcarLeidaDelegaEnLaFuente() = runTest {
        val fuente = FakeNotificaciones()
        val repo = NotificacionRepository(fuente)

        val ok = repo.marcarLeida("doc-1")

        assertTrue(ok)
        assertEquals("doc-1", fuente.marcada)
    }

    @Test
    fun marcarLeidaConIdVacioNoLlamaALaFuente() = runTest {
        val fuente = FakeNotificaciones()
        val repo = NotificacionRepository(fuente)

        val ok = repo.marcarLeida("  ")

        assertFalse(ok)
        assertNull(fuente.marcada)
    }

    @Test
    fun eliminarDelegaEnLaFuente() = runTest {
        val fuente = FakeNotificaciones()
        val repo = NotificacionRepository(fuente)

        val ok = repo.eliminar("doc-1")

        assertTrue(ok)
        assertEquals("doc-1", fuente.eliminada)
    }

    @Test
    fun eliminarConIdVacioNoLlamaALaFuente() = runTest {
        val fuente = FakeNotificaciones()
        val repo = NotificacionRepository(fuente)

        val ok = repo.eliminar("")

        assertFalse(ok)
        assertNull(fuente.eliminada)
    }
}
