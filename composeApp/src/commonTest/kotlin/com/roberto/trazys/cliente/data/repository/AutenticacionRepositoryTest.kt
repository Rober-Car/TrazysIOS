package com.roberto.trazys.cliente.data.repository

import com.roberto.trazys.cliente.data.error.ErrorAuth
import com.roberto.trazys.cliente.data.error.ErrorRemoto
import com.roberto.trazys.cliente.data.error.ResultadoSesion
import com.roberto.trazys.cliente.data.firebase.FuenteAutenticacion
import com.roberto.trazys.cliente.data.firebase.FuenteUsuarios
import com.roberto.trazys.cliente.model.Usuario
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class AutenticacionRepositoryTest {

    private class FakeAuth : FuenteAutenticacion {
        override var uid: String? = null
        override var email: String? = "cliente@centro.es"

        var errorRegistrar: ErrorRemoto? = null
        var errorLogin: ErrorRemoto? = null
        var errorRecuperar: ErrorRemoto? = null
        var errorReautenticar: ErrorRemoto? = null
        var errorCambiar: ErrorRemoto? = null

        var vecesCerrada = 0
        var vecesBorrada = 0
        var vecesCambiada = 0
        var contrasenaUsada: String? = null

        override suspend fun registrar(email: String, contrasena: String): String {
            errorRegistrar?.let { throw it }
            uid = "uid-1"
            return "uid-1"
        }

        override suspend fun iniciarSesion(email: String, contrasena: String): String {
            errorLogin?.let { throw it }
            uid = "uid-1"
            return "uid-1"
        }

        override suspend fun cerrarSesion() {
            vecesCerrada++
            uid = null
        }

        override suspend fun enviarRecuperacion(email: String) {
            errorRecuperar?.let { throw it }
        }

        override suspend fun reautenticar(contrasena: String) {
            errorReautenticar?.let { throw it }
            contrasenaUsada = contrasena
        }

        override suspend fun cambiarContrasena(nueva: String) {
            errorCambiar?.let { throw it }
            vecesCambiada++
        }

        override suspend fun borrarCuenta() {
            vecesBorrada++
        }
    }

    private class FakeUsuarios : FuenteUsuarios {
        val usuarios = mutableMapOf<String, Usuario>()
        var errorCrear: ErrorRemoto? = null
        var errorLeer: ErrorRemoto? = null

        override suspend fun leer(uid: String): Usuario? {
            errorLeer?.let { throw it }
            return usuarios[uid]
        }

        override suspend fun crearCliente(uid: String) {
            errorCrear?.let { throw it }
            usuarios[uid] = Usuario("CLIENTE", activo = true, clienteId = null, negocioId = null)
        }

        override suspend fun actualizarVinculo(uid: String, clienteId: Int, negocioId: String) {
            usuarios[uid] = Usuario("CLIENTE", true, clienteId, negocioId)
        }

        override suspend fun borrar(uid: String) {
            usuarios.remove(uid)
        }
    }

    private class Doble(
        val auth: FakeAuth = FakeAuth(),
        val usuarios: FakeUsuarios = FakeUsuarios()
    ) {
        val repo = AutenticacionRepository(auth, usuarios)
    }

    @Test
    fun registrarCreaClienteYDevuelveExito() = runTest {
        val doble = Doble()

        assertEquals(ResultadoSesion.Exito, doble.repo.registrar("cliente@centro.es", "123456"))
        assertEquals("CLIENTE", doble.usuarios.usuarios["uid-1"]?.rol)
        assertTrue(doble.repo.haySesionActiva())
    }

    @Test
    fun registrarNoLlegaAFirebaseSiElEmailNoEsValido() = runTest {
        val doble = Doble()

        assertEquals(
            ResultadoSesion.Fallo(ErrorAuth.EMAIL_INVALIDO),
            doble.repo.registrar("correo-malo", "123456")
        )
        assertTrue(doble.usuarios.usuarios.isEmpty())
    }

    @Test
    fun registrarDevuelveErrorTipadoYDeshaceElAltaSinDocumento() = runTest {
        val doble = Doble()
        doble.auth.errorRegistrar = ErrorRemoto(ErrorAuth.EMAIL_YA_REGISTRADO)
        assertEquals(
            ResultadoSesion.Fallo(ErrorAuth.EMAIL_YA_REGISTRADO),
            doble.repo.registrar("cliente@centro.es", "123456")
        )

        val doble2 = Doble()
        doble2.usuarios.errorCrear = ErrorRemoto(ErrorAuth.SIN_PERMISOS)
        assertEquals(
            ResultadoSesion.Fallo(ErrorAuth.SIN_PERMISOS),
            doble2.repo.registrar("cliente@centro.es", "123456")
        )
        assertEquals(1, doble2.auth.vecesCerrada)
    }

    @Test
    fun loginExigeDocumentoActivoYRolCliente() = runTest {
        val sinPerfil = Doble()
        assertEquals(
            ResultadoSesion.Fallo(ErrorAuth.SIN_PERFIL),
            sinPerfil.repo.iniciarSesion("cliente@centro.es", "123456")
        )
        assertEquals(1, sinPerfil.auth.vecesCerrada)

        val inactivo = Doble()
        inactivo.usuarios.usuarios["uid-1"] = Usuario("CLIENTE", false, null, null)
        assertEquals(
            ResultadoSesion.Fallo(ErrorAuth.CUENTA_DESACTIVADA),
            inactivo.repo.iniciarSesion("cliente@centro.es", "123456")
        )

        val admin = Doble()
        admin.usuarios.usuarios["uid-1"] = Usuario("ADMIN", true, null, null)
        assertEquals(
            ResultadoSesion.Fallo(ErrorAuth.ROL_INCORRECTO),
            admin.repo.iniciarSesion("cliente@centro.es", "123456")
        )
        assertEquals(1, admin.auth.vecesCerrada)

        val correcto = Doble()
        correcto.usuarios.usuarios["uid-1"] = Usuario("CLIENTE", true, 7, "negocio-1")
        assertEquals(
            ResultadoSesion.Exito,
            correcto.repo.iniciarSesion("cliente@centro.es", "123456")
        )
        assertTrue(correcto.usuarios.usuarios.getValue("uid-1").vinculado)
    }

    @Test
    fun loginConCredencialesInvalidasNoConsultaElPerfil() = runTest {
        val doble = Doble()
        doble.auth.errorLogin = ErrorRemoto(ErrorAuth.CREDENCIALES_INVALIDAS)

        assertEquals(
            ResultadoSesion.Fallo(ErrorAuth.CREDENCIALES_INVALIDAS),
            doble.repo.iniciarSesion("cliente@centro.es", "123456")
        )
        assertNull(doble.auth.uid)
    }

    @Test
    fun recuperacionNoRevelaSiLaCuentaExiste() = runTest {
        val inexistente = Doble()
        inexistente.auth.errorRecuperar = ErrorRemoto(ErrorAuth.CUENTA_NO_EXISTE)
        assertEquals(
            ResultadoSesion.Exito,
            inexistente.repo.recuperarContrasena("cliente@centro.es")
        )

        val sinRed = Doble()
        sinRed.auth.errorRecuperar = ErrorRemoto(ErrorAuth.SIN_CONEXION)
        assertEquals(
            ResultadoSesion.Fallo(ErrorAuth.SIN_CONEXION),
            sinRed.repo.recuperarContrasena("cliente@centro.es")
        )
    }

    @Test
    fun cambioDeContrasenaReautenticaAntesDeCambiar() = runTest {
        val doble = Doble()
        assertEquals(
            ResultadoSesion.Fallo(ErrorAuth.CONTRASENAS_NO_COINCIDEN),
            doble.repo.cambiarContrasena("actual123", "nueva123", "nueva124")
        )
        assertEquals(0, doble.auth.vecesCambiada)

        doble.auth.errorReautenticar = ErrorRemoto(ErrorAuth.CREDENCIALES_INVALIDAS)
        assertEquals(
            ResultadoSesion.Fallo(ErrorAuth.CONTRASENA_ACTUAL_INCORRECTA),
            doble.repo.cambiarContrasena("mala123", "nueva123", "nueva123")
        )
        assertEquals(0, doble.auth.vecesCambiada)

        doble.auth.errorReautenticar = null
        assertEquals(
            ResultadoSesion.Exito,
            doble.repo.cambiarContrasena("actual123", "nueva123", "nueva123")
        )
        assertEquals("actual123", doble.auth.contrasenaUsada)
        assertEquals(1, doble.auth.vecesCambiada)
    }

    @Test
    fun cerrarSesionDelegaEnLaFuente() = runTest {
        val doble = Doble()
        doble.auth.uid = "uid-1"

        assertEquals(ResultadoSesion.Exito, doble.repo.cerrarSesion())
        assertEquals(1, doble.auth.vecesCerrada)
        assertTrue(!doble.repo.haySesionActiva())
    }
}
