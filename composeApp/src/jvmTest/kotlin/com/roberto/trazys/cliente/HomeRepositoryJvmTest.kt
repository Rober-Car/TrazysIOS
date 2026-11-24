package com.roberto.trazys.cliente

import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.core.okio.OkioStorage
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.PreferencesSerializer
import com.roberto.trazys.cliente.data.firebase.FuenteClientes
import com.roberto.trazys.cliente.data.repository.HomeRepository
import com.roberto.trazys.cliente.data.repository.PreferencesRepository
import com.roberto.trazys.cliente.model.Cliente
import com.roberto.trazys.cliente.model.EstadoCliente
import com.roberto.trazys.cliente.model.EstadoIndicadorCliente
import kotlinx.coroutines.runBlocking
import okio.FileSystem
import java.io.File
import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class HomeRepositoryJvmTest {

    private val ficha = Cliente(
        idCliente = 42,
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
        fechaBaja = null,
        estado = EstadoCliente.ACTIVO,
        serviciosContratados = emptyList(),
        fechaInicioActual = null,
        fechaFinActual = 2_000L
    )

    private class FakeClientes(
        private val ficha: Cliente?,
        private val error: Exception? = null
    ) : FuenteClientes {
        override suspend fun leerFicha(idCliente: Int): Cliente? {
            error?.let { throw it }
            return ficha
        }
    }

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

    private fun preferencias(): PreferencesRepository {
        val dir = Files.createTempDirectory("trazys-home").toFile()
        return PreferencesRepository(crearDataStore(dir))
    }

    @Test
    fun sinClienteVinculadoNoDevuelveEstado() = runBlocking {
        val repo = HomeRepository(preferencias(), FakeClientes(ficha), ahora = { 1_000L })

        val estado = repo.cargarEstado()

        assertNull(estado.estado)
        assertNull(estado.error)
    }

    @Test
    fun conFichaVigenteDevuelveActivo() = runBlocking {
        val prefs = preferencias()
        prefs.setIdCliente(42)
        val repo = HomeRepository(prefs, FakeClientes(ficha), ahora = { 1_000L })

        val estado = repo.cargarEstado()

        assertEquals(EstadoIndicadorCliente.ACTIVO, estado.estado)
        assertEquals(2_000L, estado.fechaRelevante)
        assertNull(estado.error)
    }

    @Test
    fun conFichaVencidaDevuelvePagoVencido() = runBlocking {
        val prefs = preferencias()
        prefs.setIdCliente(42)
        val repo = HomeRepository(prefs, FakeClientes(ficha), ahora = { 3_000L })

        val estado = repo.cargarEstado()

        assertEquals(EstadoIndicadorCliente.PAGO_VENCIDO, estado.estado)
    }

    @Test
    fun fichaAusenteDevuelveError() = runBlocking {
        val prefs = preferencias()
        prefs.setIdCliente(42)
        val repo = HomeRepository(prefs, FakeClientes(ficha = null), ahora = { 1_000L })

        val estado = repo.cargarEstado()

        assertNull(estado.estado)
        assertEquals("No se encontró la ficha del cliente", estado.error)
    }
}
