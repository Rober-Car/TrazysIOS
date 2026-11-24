package com.roberto.trazys.cliente.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.core.okio.OkioStorage
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.PreferencesSerializer
import com.roberto.trazys.cliente.data.error.ErrorAuth
import com.roberto.trazys.cliente.data.error.ErrorCuenta
import com.roberto.trazys.cliente.data.error.ErrorRemoto
import com.roberto.trazys.cliente.data.error.ResultadoCuenta
import com.roberto.trazys.cliente.data.firebase.FuenteAutenticacion
import com.roberto.trazys.cliente.data.firebase.FuenteEliminacionCuenta
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okio.FileSystem
import java.io.File
import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
class CuentaRepositoryJvmTest {

    private class FakeAuth(
        override var uid: String? = "uid-1",
        private val errorReautenticar: ErrorRemoto? = null,
        private val errorCerrar: ErrorRemoto? = null
    ) : FuenteAutenticacion {
        override var email: String? = "cliente@centro.es"
        var reautenticado = false
        var cerrada = false

        override suspend fun registrar(email: String, contrasena: String): String = "uid-1"
        override suspend fun iniciarSesion(email: String, contrasena: String): String = "uid-1"

        override suspend fun cerrarSesion() {
            errorCerrar?.let { throw it }
            cerrada = true
            uid = null
        }

        override suspend fun enviarRecuperacion(email: String) = Unit

        override suspend fun reautenticar(contrasena: String) {
            errorReautenticar?.let { throw it }
            reautenticado = true
        }

        override suspend fun cambiarContrasena(nueva: String) = Unit
        override suspend fun borrarCuenta() = Unit
    }

    private class FakeEliminacion(
        private val resultado: ResultadoCuenta = ResultadoCuenta.correcto(),
        private val error: Exception? = null
    ) : FuenteEliminacionCuenta {
        var llamada = false

        override suspend fun eliminarCuenta(): ResultadoCuenta {
            error?.let { throw it }
            llamada = true
            return resultado
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
        val dir = Files.createTempDirectory("trazys-cuenta").toFile()
        return PreferencesRepository(crearDataStore(dir))
    }

    @Test
    fun eliminarCuentaExitosoLimpiaPreferenciasYCierraSesion() = runBlocking {
        val prefs = preferencias()
        prefs.setIdCliente(42)
        prefs.setNegocioId("negocio-1")
        val auth = FakeAuth()
        val eliminacion = FakeEliminacion()
        val repo = CuentaRepository(auth, eliminacion, prefs)

        val resultado = repo.eliminarCuenta("secreta")

        assertTrue(resultado.exito)
        assertTrue(auth.reautenticado)
        assertTrue(auth.cerrada)
        assertTrue(eliminacion.llamada)
        assertNull(prefs.idCliente.first())
        assertNull(prefs.negocioId.first())
    }

    @Test
    fun sinSesionNoReautenticaNiElimina() = runBlocking {
        val auth = FakeAuth(uid = null)
        val eliminacion = FakeEliminacion()
        val repo = CuentaRepository(auth, eliminacion, preferencias())

        val resultado = repo.eliminarCuenta("secreta")

        assertFalse(resultado.exito)
        assertEquals(ErrorCuenta.SIN_SESION, resultado.error)
        assertFalse(auth.reautenticado)
        assertFalse(eliminacion.llamada)
    }

    @Test
    fun contrasenaVaciaNoLlamaAlBackend() = runBlocking {
        val auth = FakeAuth()
        val eliminacion = FakeEliminacion()
        val repo = CuentaRepository(auth, eliminacion, preferencias())

        val resultado = repo.eliminarCuenta("   ")

        assertFalse(resultado.exito)
        assertEquals(ErrorCuenta.CAMPOS_INCOMPLETOS, resultado.error)
        assertFalse(eliminacion.llamada)
    }

    @Test
    fun reautenticacionConCredencialesInvalidasSeTraduce() = runBlocking {
        val auth = FakeAuth(
            errorReautenticar = ErrorRemoto(ErrorAuth.CREDENCIALES_INVALIDAS)
        )
        val eliminacion = FakeEliminacion()
        val repo = CuentaRepository(auth, eliminacion, preferencias())

        val resultado = repo.eliminarCuenta("incorrecta")

        assertFalse(resultado.exito)
        assertEquals(ErrorCuenta.CONTRASENA_INCORRECTA, resultado.error)
        assertFalse(eliminacion.llamada)
    }

    @Test
    fun reautenticacionSinConexionSeTraduce() = runBlocking {
        val auth = FakeAuth(errorReautenticar = ErrorRemoto(ErrorAuth.SIN_CONEXION))
        val repo = CuentaRepository(auth, FakeEliminacion(), preferencias())

        val resultado = repo.eliminarCuenta("secreta")

        assertEquals(ErrorCuenta.SIN_CONEXION, resultado.error)
    }

    @Test
    fun falloRemotoNoCierraSesionNiLimpia() = runBlocking {
        val prefs = preferencias()
        prefs.setIdCliente(42)
        val auth = FakeAuth()
        val eliminacion = FakeEliminacion(
            resultado = ResultadoCuenta.fallo(ErrorCuenta.DESCONOCIDO)
        )
        val repo = CuentaRepository(auth, eliminacion, prefs)

        val resultado = repo.eliminarCuenta("secreta")

        assertFalse(resultado.exito)
        assertEquals(ErrorCuenta.DESCONOCIDO, resultado.error)
        assertFalse(auth.cerrada)
        assertNotNull(prefs.idCliente.first())
        Unit
    }

    @Test
    fun excepcionRemotaSeTraduceSinCerrarSesion() = runBlocking {
        val auth = FakeAuth()
        val eliminacion = FakeEliminacion(error = ErrorRemoto(ErrorAuth.SIN_PERMISOS))
        val repo = CuentaRepository(auth, eliminacion, preferencias())

        val resultado = repo.eliminarCuenta("secreta")

        assertEquals(ErrorCuenta.SIN_PERMISOS, resultado.error)
        assertFalse(auth.cerrada)
    }

    @Test
    fun exitoRemotoConFalloAlCerrarSesionSigueSiendoExito() = runBlocking {
        val auth = FakeAuth(errorCerrar = ErrorRemoto(ErrorAuth.SIN_CONEXION))
        val repo = CuentaRepository(auth, FakeEliminacion(), preferencias())

        val resultado = repo.eliminarCuenta("secreta")

        assertTrue(resultado.exito)
    }
}
