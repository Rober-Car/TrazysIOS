package com.roberto.trazys.cliente.data.repository

import com.roberto.trazys.cliente.data.error.ErrorAuth
import com.roberto.trazys.cliente.data.error.ErrorConsulta
import com.roberto.trazys.cliente.data.error.ErrorRemoto
import com.roberto.trazys.cliente.data.firebase.FuenteSesiones
import com.roberto.trazys.cliente.data.firebase.asistentesDe
import com.roberto.trazys.cliente.model.Servicio
import com.roberto.trazys.cliente.model.Sesion
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class SesionRepositoryTest {

    private val servicio = Servicio(
        idServicio = 5,
        negocioId = "negocio-1",
        nombre = "Yoga",
        descripcion = "",
        activo = true
    )

    private class FakeSesiones(
        var servicio: Servicio? = null,
        var sesiones: List<Sesion> = emptyList(),
        var sesion: Sesion? = null,
        var errorServicio: ErrorRemoto? = null,
        var errorSesiones: ErrorRemoto? = null,
        var errorSesion: ErrorRemoto? = null
    ) : FuenteSesiones {
        override suspend fun servicioActivo(idServicio: Int, negocioId: String): Servicio? {
            errorServicio?.let { throw it }
            return servicio
        }

        override suspend fun sesionesDeServicio(idServicio: Int, negocioId: String): List<Sesion> {
            errorSesiones?.let { throw it }
            return sesiones
        }

        override suspend fun sesion(idSesion: Int, negocioId: String): Sesion? {
            errorSesion?.let { throw it }
            return sesion
        }
    }

    private fun sesion(id: Int, fecha: Long, hora: String) = Sesion(
        idSesion = id,
        negocioId = "negocio-1",
        idServicio = 5,
        fecha = fecha,
        hora = hora,
        duracionMinutos = 60,
        capacidad = 10,
        plazasDisponibles = 4
    )

    @Test
    fun cargarAgendaOrdenaPorFechaYHora() = runTest {
        val fuente = FakeSesiones(
            servicio = servicio,
            sesiones = listOf(
                sesion(2, 2_000L, "09:00"),
                sesion(1, 1_000L, "18:00"),
                sesion(3, 1_000L, "08:00")
            )
        )
        val repo = SesionRepository(fuente)

        val resultado = repo.cargarAgenda(5, "negocio-1")

        assertTrue(resultado is ResultadoAgenda.Ok)
        val ok = resultado
        assertEquals(servicio, ok.servicio)
        assertEquals(listOf(3, 1, 2), ok.sesiones.map { it.idSesion })
    }

    @Test
    fun servicioNoDisponibleDevuelveNoDisponible() = runTest {
        val repo = SesionRepository(FakeSesiones(servicio = null))

        val resultado = repo.cargarAgenda(5, "negocio-1")

        assertEquals(ResultadoAgenda.NoDisponible, resultado)
    }

    @Test
    fun parametrosInvalidosDevuelvenNoDisponible() = runTest {
        val repo = SesionRepository(FakeSesiones(servicio = servicio))

        assertEquals(ResultadoAgenda.NoDisponible, repo.cargarAgenda(0, "negocio-1"))
        assertEquals(ResultadoAgenda.NoDisponible, repo.cargarAgenda(5, ""))
    }

    @Test
    fun errorRemotoSeTraduce() = runTest {
        val fuente = FakeSesiones(
            servicio = servicio,
            errorSesiones = ErrorRemoto(ErrorAuth.SIN_PERMISOS)
        )
        val repo = SesionRepository(fuente)

        val resultado = repo.cargarAgenda(5, "negocio-1")

        assertTrue(resultado is ResultadoAgenda.Fallo)
        assertEquals(ErrorConsulta.SIN_PERMISOS, resultado.error)
    }

    @Test
    fun cargarSesionDevuelveNullSiFalla() = runTest {
        val fuente = FakeSesiones(errorSesion = ErrorRemoto(ErrorAuth.SIN_CONEXION))
        val repo = SesionRepository(fuente)

        assertNull(repo.cargarSesion(1, "negocio-1"))
    }

    @Test
    fun cargarSesionDevuelveLaSesion() = runTest {
        val esperada = sesion(1, 1_000L, "08:00")
        val repo = SesionRepository(FakeSesiones(sesion = esperada))

        assertEquals(esperada, repo.cargarSesion(1, "negocio-1"))
    }

    @Test
    fun asistentesDeEsTolerante() {
        assertEquals(
            mapOf("1" to "Ana", "2" to "Luis"),
            asistentesDe(mapOf(1 to "Ana", 2 to "Luis"))
        )
        assertEquals(emptyMap(), asistentesDe(null))
        assertEquals(emptyMap(), asistentesDe("no-es-un-mapa"))
        assertEquals(mapOf("1" to "Ana"), asistentesDe(mapOf(1 to "Ana", 2 to null)))
    }
}
