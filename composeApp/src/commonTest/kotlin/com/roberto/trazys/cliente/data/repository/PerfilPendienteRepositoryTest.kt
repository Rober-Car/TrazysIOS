package com.roberto.trazys.cliente.data.repository

import com.roberto.trazys.cliente.data.error.ErrorAuth
import com.roberto.trazys.cliente.data.error.ResultadoSesion
import com.roberto.trazys.cliente.data.firebase.FuenteAutenticacion
import com.roberto.trazys.cliente.data.firebase.FuentePerfiles
import com.roberto.trazys.cliente.model.PerfilPendiente
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class PerfilPendienteRepositoryTest {

    private class FakeAuth(override var uid: String? = "uid-1") : FuenteAutenticacion {
        override var email: String? = null
        override suspend fun registrar(email: String, contrasena: String): String = "uid-1"
        override suspend fun iniciarSesion(email: String, contrasena: String): String = "uid-1"
        override suspend fun cerrarSesion() = Unit
        override suspend fun enviarRecuperacion(email: String) = Unit
        override suspend fun reautenticar(contrasena: String) = Unit
        override suspend fun cambiarContrasena(nueva: String) = Unit
        override suspend fun borrarCuenta() = Unit
    }

    private class FakePerfiles : FuentePerfiles {
        var guardado: PerfilPendiente? = null

        override suspend fun guardar(uid: String, perfil: PerfilPendiente) {
            guardado = perfil
        }

        override suspend fun guardarDeclaracion(uid: String, dni: String, negocioId: String) = Unit

        override suspend fun leer(uid: String): PerfilPendiente? = guardado

        override suspend fun borrar(uid: String) {
            guardado = null
        }
    }

    private val completo = PerfilPendiente(
        nombre = "Ana",
        apellidos = "García",
        dni = "12345678Z",
        telefono = "600000000"
    )

    @Test
    fun guardarExigeSesionYPerfilCompleto() = runTest {
        val fuente = FakePerfiles()
        val repo = PerfilPendienteRepository(fuente, FakeAuth(uid = null))

        assertEquals(
            ResultadoSesion.Fallo(ErrorAuth.SIN_SESION),
            repo.guardar(completo)
        )

        val repoConSesion = PerfilPendienteRepository(
            fuente,
            FakeAuth()
        )
        assertEquals(
            ResultadoSesion.Fallo(ErrorAuth.CAMPOS_INCOMPLETOS),
            repoConSesion.guardar(completo.copy(telefono = ""))
        )

        assertEquals(ResultadoSesion.Exito, repoConSesion.guardar(completo))
        assertEquals(completo, fuente.guardado)
    }

    @Test
    fun leerSinSesionDevuelveNull() = runTest {
        val repo = PerfilPendienteRepository(FakePerfiles(), FakeAuth(uid = null))
        assertNull(repo.leer())
    }
}
