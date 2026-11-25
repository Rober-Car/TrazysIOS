package com.roberto.trazys.cliente

import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.core.okio.OkioStorage
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.PreferencesSerializer
import com.roberto.trazys.cliente.data.repository.PreferencesRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okio.FileSystem
import java.io.File
import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Verifica el round-trip de PreferencesRepository sobre un DataStore JVM real.
 * Cubre también la ruta absoluta con File.toOkioPath(), que en Windows es el
 * punto donde fallaba toPath().
 */
class PreferencesRepositoryJvmTest {

    private fun crearDataStore(dir: File): DataStore<Preferences> =
        DataStoreFactory.create(
            storage = OkioStorage(
                fileSystem = FileSystem.SYSTEM,
                serializer = PreferencesSerializer,
                producePath = {
                    with(okio.Path.Companion) {
                        File(dir, "prefs.preferences_pb").toOkioPath()
                    }
                }
            )
        )

    @Test
    fun clavesTerminosYLimpiarDatosPersonales() = runBlocking {
        val dir = Files.createTempDirectory("trazys-prefs").toFile()
        val repo = PreferencesRepository(crearDataStore(dir))

        assertEquals("sistema", repo.themeMode.first())
        assertEquals("es", repo.idioma.first())
        assertEquals("", repo.nombreNegocio.first())
        assertTrue(repo.notificacionesActivadas.first())
        assertNull(repo.idCliente.first())

        repo.setThemeMode("oscuro")
        repo.setIdioma("en")
        repo.setIdCliente(42)
        repo.setNegocioId("negocio-1")
        repo.setDniPendiente("12345678z")
        repo.setNombreNegocio("  Trazys  ")
        repo.setLogoNegocio("https://ejemplo/logo.png")
        repo.setNotificacionesActivadas(false)
        repo.guardarAceptacionTerminos("uid-1")

        assertEquals("oscuro", repo.themeMode.first())
        assertEquals("en", repo.idioma.first())
        assertEquals(42, repo.idCliente.first())
        assertEquals("negocio-1", repo.negocioId.first())
        assertEquals("12345678Z", repo.dniPendiente.first())
        assertEquals("Trazys", repo.nombreNegocio.first())
        assertEquals("https://ejemplo/logo.png", repo.logoNegocio.first())
        assertFalse(repo.notificacionesActivadas.first())
        assertTrue(repo.terminosAceptados("uid-1"))
        assertFalse(repo.terminosAceptados("uid-2"))

        repo.limpiarDatosPersonales()

        assertNull(repo.idCliente.first())
        assertNull(repo.negocioId.first())
        assertNull(repo.dniPendiente.first())
        assertEquals("", repo.nombreNegocio.first())
        assertEquals("", repo.logoNegocio.first())
        assertTrue(repo.notificacionesActivadas.first())
        assertFalse(repo.terminosAceptados("uid-1"))
        assertEquals("oscuro", repo.themeMode.first())
        assertEquals("en", repo.idioma.first())
    }

    @Test
    fun limpiarSesionConservaTerminosYAjustesDelDispositivo() = runBlocking {
        val dir = Files.createTempDirectory("trazys-prefs-sesion").toFile()
        val repo = PreferencesRepository(crearDataStore(dir))

        repo.setThemeMode("oscuro")
        repo.setIdioma("en")
        repo.setIdCliente(42)
        repo.setNegocioId("negocio-1")
        repo.setDniPendiente("12345678z")
        repo.setNombreNegocio("Trazys")
        repo.setLogoNegocio("https://ejemplo/logo.png")
        repo.setNotificacionesActivadas(false)
        repo.guardarAceptacionTerminos("uid-1")

        repo.limpiarSesion()

        assertNull(repo.idCliente.first())
        assertNull(repo.negocioId.first())
        assertNull(repo.dniPendiente.first())
        assertEquals("", repo.nombreNegocio.first())
        assertEquals("", repo.logoNegocio.first())
        assertTrue(repo.terminosAceptados("uid-1"))
        assertFalse(repo.notificacionesActivadas.first())
        assertEquals("oscuro", repo.themeMode.first())
        assertEquals("en", repo.idioma.first())
    }
}
