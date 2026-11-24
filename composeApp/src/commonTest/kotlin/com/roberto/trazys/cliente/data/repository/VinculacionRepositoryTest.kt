package com.roberto.trazys.cliente.data.repository

import com.roberto.trazys.cliente.data.error.ErrorAuth
import com.roberto.trazys.cliente.data.error.ErrorRemoto
import com.roberto.trazys.cliente.data.error.ErrorVinculacion
import com.roberto.trazys.cliente.data.firebase.FuenteAutenticacion
import com.roberto.trazys.cliente.data.firebase.FuenteClientesVinculacion
import com.roberto.trazys.cliente.data.firebase.FuenteCodigos
import com.roberto.trazys.cliente.data.firebase.FuenteIndices
import com.roberto.trazys.cliente.data.firebase.FuenteNegocios
import com.roberto.trazys.cliente.data.firebase.FuenteNotificacionesAdmin
import com.roberto.trazys.cliente.data.firebase.FuentePerfiles
import com.roberto.trazys.cliente.data.firebase.ResultadoCreacionFicha
import com.roberto.trazys.cliente.data.firebase.ResultadoFichaVinculada
import com.roberto.trazys.cliente.model.NegocioPublico
import com.roberto.trazys.cliente.model.PerfilPendiente
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class VinculacionRepositoryTest {

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

    private class FakeCodigos(
        val porCodigo: MutableMap<String, String> = mutableMapOf("CODIGO1" to "negocio-1"),
        var error: ErrorRemoto? = null
    ) : FuenteCodigos {
        override suspend fun negocioIdDe(codigo: String): String? {
            error?.let { throw it }
            return porCodigo[codigo]
        }
    }

    private class FakeNegocios(
        val publicos: MutableMap<String, NegocioPublico> = mutableMapOf(
            "negocio-1" to NegocioPublico("Centro Uno", "CODIGO1", "")
        )
    ) : FuenteNegocios {
        override suspend fun leerPublico(negocioId: String): NegocioPublico? = publicos[negocioId]
    }

    private class FakePerfiles(
        var perfil: PerfilPendiente? = PerfilPendiente(
            nombre = "Ana",
            apellidos = "García",
            dni = "12345678Z",
            telefono = "600000000"
        )
    ) : FuentePerfiles {
        var declaracion: Triple<String, String, String>? = null
        var vecesBorrado = 0
        var errorLeer: ErrorRemoto? = null

        override suspend fun guardar(uid: String, perfil: PerfilPendiente) {
            this.perfil = perfil
        }

        override suspend fun guardarDeclaracion(uid: String, dni: String, negocioId: String) {
            declaracion = Triple(uid, dni, negocioId)
        }

        override suspend fun leer(uid: String): PerfilPendiente? {
            errorLeer?.let { throw it }
            return perfil
        }

        override suspend fun borrar(uid: String) {
            vecesBorrado++
            perfil = null
        }
    }

    private class FakeIndices(
        val indices: MutableMap<String, Int> = mutableMapOf(),
        var error: ErrorRemoto? = null
    ) : FuenteIndices {
        override suspend fun clienteIdDe(negocioId: String, dni: String): Int? {
            error?.let { throw it }
            return indices["${negocioId}_$dni"]
        }
    }

    private class FakeClientesVinculacion : FuenteClientesVinculacion {
        var resultadoVincular: ResultadoFichaVinculada = ResultadoFichaVinculada.Vinculada
        var resultadoCrear: ResultadoCreacionFicha = ResultadoCreacionFicha.Creada(42)
        var invocadoVincular = false
        var invocadoCrear = false
        var perfilRecibido: PerfilPendiente? = null

        override suspend fun vincularFicha(
            clienteId: Int,
            uid: String,
            negocioId: String
        ): ResultadoFichaVinculada {
            invocadoVincular = true
            return resultadoVincular
        }

        override suspend fun crearFicha(
            uid: String,
            negocioId: String,
            dni: String,
            perfil: PerfilPendiente
        ): ResultadoCreacionFicha {
            invocadoCrear = true
            perfilRecibido = perfil
            return resultadoCrear
        }
    }

    private class FakeNotificaciones : FuenteNotificacionesAdmin {
        val avisos = mutableListOf<Pair<String, Int>>()
        var error: ErrorRemoto? = null

        override suspend fun notificarVinculacion(negocioId: String, clienteId: Int) {
            error?.let { throw it }
            avisos += negocioId to clienteId
        }
    }

    private class Doble(
        val auth: FakeAuth = FakeAuth(),
        val codigos: FakeCodigos = FakeCodigos(),
        val negocios: FakeNegocios = FakeNegocios(),
        val perfiles: FakePerfiles = FakePerfiles(),
        val indices: FakeIndices = FakeIndices(),
        val clientes: FakeClientesVinculacion = FakeClientesVinculacion(),
        val notificaciones: FakeNotificaciones = FakeNotificaciones()
    ) {
        val repo = VinculacionRepository(
            auth, codigos, negocios, perfiles, indices, clientes, notificaciones
        )
    }

    @Test
    fun sinSesionNoConsultaNada() = runTest {
        val doble = Doble(auth = FakeAuth(uid = null))

        val resultado = doble.repo.vincularConCodigoYDNI("CODIGO1", "12345678Z")

        assertFalse(resultado.exito)
        assertEquals(ErrorVinculacion.SIN_SESION, resultado.error)
        assertNull(doble.perfiles.declaracion)
    }

    @Test
    fun codigoInexistenteNoContinua() = runTest {
        val doble = Doble(codigos = FakeCodigos(porCodigo = mutableMapOf()))

        val resultado = doble.repo.vincularConCodigoYDNI("NOEXISTE", "12345678Z")

        assertEquals(ErrorVinculacion.CODIGO_INVALIDO, resultado.error)
        assertNull(doble.perfiles.declaracion)
    }

    @Test
    fun centroIncoherenteSeRechaza() = runTest {
        val doble = Doble()
        doble.negocios.publicos["negocio-1"] = NegocioPublico("Centro", "OTRO", "")

        val resultado = doble.repo.vincularConCodigoYDNI("CODIGO1", "12345678Z")

        assertEquals(ErrorVinculacion.CENTRO_INCOHERENTE, resultado.error)
    }

    @Test
    fun via1VinculaFichaExistenteYCompleta() = runTest {
        val doble = Doble(indices = FakeIndices(mutableMapOf("negocio-1_12345678Z" to 42)))

        val resultado = doble.repo.vincularConCodigoYDNI(" CODIGO1 ", "12345678z")

        assertTrue(resultado.exito)
        assertEquals(42, resultado.clienteId)
        assertEquals("negocio-1", resultado.negocioId)
        assertEquals(Triple("uid-1", "12345678Z", "negocio-1"), doble.perfiles.declaracion)
        assertEquals(1, doble.perfiles.vecesBorrado)
        assertEquals(listOf("negocio-1" to 42), doble.notificaciones.avisos)
        assertFalse(doble.clientes.invocadoCrear)
    }

    @Test
    fun via1ConDniYaVinculadoNoBorraElPerfil() = runTest {
        val doble = Doble(indices = FakeIndices(mutableMapOf("negocio-1_12345678Z" to 42)))
        doble.clientes.resultadoVincular = ResultadoFichaVinculada.YaVinculada

        val resultado = doble.repo.vincularConCodigoYDNI("CODIGO1", "12345678Z")

        assertEquals(ErrorVinculacion.DNI_YA_VINCULADO, resultado.error)
        assertEquals(0, doble.perfiles.vecesBorrado)
        assertTrue(doble.notificaciones.avisos.isEmpty())
    }

    @Test
    fun via1ConFichaInexistenteSeInforma() = runTest {
        val doble = Doble(indices = FakeIndices(mutableMapOf("negocio-1_12345678Z" to 42)))
        doble.clientes.resultadoVincular = ResultadoFichaVinculada.NoExiste

        val resultado = doble.repo.vincularConCodigoYDNI("CODIGO1", "12345678Z")

        assertEquals(ErrorVinculacion.FICHA_NO_EXISTE, resultado.error)
        assertEquals(0, doble.perfiles.vecesBorrado)
    }

    @Test
    fun via2SinPerfilCompletoSolicitaCompletarlo() = runTest {
        val doble = Doble(
            perfiles = FakePerfiles(perfil = PerfilPendiente(nombre = "Ana"))
        )

        val resultado = doble.repo.vincularConCodigoYDNI("CODIGO1", "12345678Z")

        assertFalse(resultado.exito)
        assertTrue(resultado.requiereCompletarPerfil)
        assertEquals(ErrorVinculacion.REQUIERE_PERFIL, resultado.error)
        assertFalse(doble.clientes.invocadoCrear)
    }

    @Test
    fun via2CreaFichaConElPerfilPendiente() = runTest {
        val doble = Doble()

        val resultado = doble.repo.vincularConCodigoYDNI("CODIGO1", "12345678Z")

        assertTrue(resultado.exito)
        assertEquals(42, resultado.clienteId)
        assertTrue(doble.clientes.invocadoCrear)
        assertEquals("Ana", doble.clientes.perfilRecibido?.nombre)
        assertEquals(1, doble.perfiles.vecesBorrado)
        assertEquals(listOf("negocio-1" to 42), doble.notificaciones.avisos)
    }

    @Test
    fun via2ConIndiceOcupadoSeTraduceADniVinculado() = runTest {
        val doble = Doble()
        doble.clientes.resultadoCrear = ResultadoCreacionFicha.IndiceOcupado

        val resultado = doble.repo.vincularConCodigoYDNI("CODIGO1", "12345678Z")

        assertEquals(ErrorVinculacion.DNI_YA_VINCULADO, resultado.error)
        assertEquals(0, doble.perfiles.vecesBorrado)
    }

    @Test
    fun via2SinIdDisponibleSeInforma() = runTest {
        val doble = Doble()
        doble.clientes.resultadoCrear = ResultadoCreacionFicha.SinIdDisponible

        val resultado = doble.repo.vincularConCodigoYDNI("CODIGO1", "12345678Z")

        assertEquals(ErrorVinculacion.ID_NO_DISPONIBLE, resultado.error)
    }

    @Test
    fun errorDeRedSeTraduceSinConexion() = runTest {
        val doble = Doble()
        doble.codigos.error = ErrorRemoto(ErrorAuth.SIN_CONEXION)

        val resultado = doble.repo.vincularConCodigoYDNI("CODIGO1", "12345678Z")

        assertEquals(ErrorVinculacion.SIN_CONEXION, resultado.error)
    }

    @Test
    fun falloAlNotificarNoRompeLaVinculacion() = runTest {
        val doble = Doble()
        doble.notificaciones.error = ErrorRemoto(ErrorAuth.SIN_PERMISOS)

        val resultado = doble.repo.vincularConCodigoYDNI("CODIGO1", "12345678Z")

        assertTrue(resultado.exito)
        assertEquals(1, doble.perfiles.vecesBorrado)
    }
}
