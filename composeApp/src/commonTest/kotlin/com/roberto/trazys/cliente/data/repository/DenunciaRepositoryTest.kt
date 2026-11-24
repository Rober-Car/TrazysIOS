package com.roberto.trazys.cliente.data.repository

import com.roberto.trazys.cliente.data.error.ErrorAuth
import com.roberto.trazys.cliente.data.error.ErrorDenuncia
import com.roberto.trazys.cliente.data.error.ErrorRemoto
import com.roberto.trazys.cliente.data.firebase.FuenteAutenticacion
import com.roberto.trazys.cliente.data.firebase.FuenteDenuncias
import com.roberto.trazys.cliente.model.MAX_DESCRIPCION_DENUNCIA
import com.roberto.trazys.cliente.model.MotivosDenuncia
import com.roberto.trazys.cliente.model.TiposContenidoDenunciable
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class DenunciaRepositoryTest {

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

    private class FakeDenuncias(
        private val error: Exception? = null
    ) : FuenteDenuncias {
        var id: String? = null
        var negocioId: String? = null
        var denuncianteUid: String? = null
        var tipo: String? = null
        var referencia: String? = null
        var usuarioDenunciadoUid: String? = null
        var motivo: String? = null
        var descripcion: String? = null
        var fecha: Long? = null

        override suspend fun crear(
            idDenuncia: String,
            negocioId: String,
            denuncianteUid: String,
            tipo: String,
            referencia: String,
            usuarioDenunciadoUid: String?,
            motivo: String,
            descripcion: String?,
            fecha: Long
        ) {
            error?.let { throw it }
            this.id = idDenuncia
            this.negocioId = negocioId
            this.denuncianteUid = denuncianteUid
            this.tipo = tipo
            this.referencia = referencia
            this.usuarioDenunciadoUid = usuarioDenunciadoUid
            this.motivo = motivo
            this.descripcion = descripcion
            this.fecha = fecha
        }
    }

    private fun repo(
        auth: FuenteAutenticacion = FakeAuth(),
        fuente: FuenteDenuncias = FakeDenuncias()
    ) = DenunciaRepository(auth, fuente, ahora = { 1_000L }, sufijo = { 4321 })

    @Test
    fun sinSesionNoDenuncia() = runTest {
        val fuente = FakeDenuncias()
        val resultado = repo(auth = FakeAuth(uid = null), fuente = fuente).crear(
            negocioId = "negocio-1",
            tipo = TiposContenidoDenunciable.NOTIFICACION,
            referencia = "n-1",
            usuarioDenunciadoUid = "negocio-1",
            motivo = MotivosDenuncia.ACOSO,
            descripcion = null
        )

        assertFalse(resultado.exito)
        assertEquals(ErrorDenuncia.SIN_SESION, resultado.error)
        assertNull(fuente.id)
    }

    @Test
    fun tipoInvalidoNoDenuncia() = runTest {
        val fuente = FakeDenuncias()
        val resultado = repo(fuente = fuente).crear(
            negocioId = "negocio-1",
            tipo = "OTRO_TIPO",
            referencia = "n-1",
            usuarioDenunciadoUid = null,
            motivo = MotivosDenuncia.OTRO,
            descripcion = null
        )

        assertEquals(ErrorDenuncia.TIPO_INVALIDO, resultado.error)
        assertNull(fuente.id)
    }

    @Test
    fun motivoInvalidoNoDenuncia() = runTest {
        val fuente = FakeDenuncias()
        val resultado = repo(fuente = fuente).crear(
            negocioId = "negocio-1",
            tipo = TiposContenidoDenunciable.LOGO_NEGOCIO,
            referencia = "logo",
            usuarioDenunciadoUid = null,
            motivo = "RARO",
            descripcion = null
        )

        assertEquals(ErrorDenuncia.MOTIVO_INVALIDO, resultado.error)
        assertNull(fuente.id)
    }

    @Test
    fun negocioOVacioReferenciaNoDenuncia() = runTest {
        val fuente = FakeDenuncias()
        val resultado = repo(fuente = fuente).crear(
            negocioId = " ",
            tipo = TiposContenidoDenunciable.FOTO_CLIENTE,
            referencia = "",
            usuarioDenunciadoUid = null,
            motivo = MotivosDenuncia.INAPROPIADO,
            descripcion = null
        )

        assertEquals(ErrorDenuncia.REFERENCIA_INVALIDA, resultado.error)
        assertNull(fuente.id)
    }

    @Test
    fun creaDenunciaConIdDeterministaYDescripcionLimpia() = runTest {
        val fuente = FakeDenuncias()
        val resultado = repo(fuente = fuente).crear(
            negocioId = "negocio-1",
            tipo = TiposContenidoDenunciable.NOTIFICACION,
            referencia = "n-1",
            usuarioDenunciadoUid = "  uid-admin  ",
            motivo = MotivosDenuncia.ACOSO,
            descripcion = "  texto  "
        )

        assertTrue(resultado.exito)
        assertEquals("denuncia_1000_4321", fuente.id)
        assertEquals("uid-1", fuente.denuncianteUid)
        assertEquals("uid-admin", fuente.usuarioDenunciadoUid)
        assertEquals("texto", fuente.descripcion)
        assertEquals(1_000L, fuente.fecha)
        assertEquals(TiposContenidoDenunciable.NOTIFICACION, fuente.tipo)
        assertEquals(MotivosDenuncia.ACOSO, fuente.motivo)
    }

    @Test
    fun descripcionVaciaSeOmiteYSeRecortaALimite() = runTest {
        val vacia = FakeDenuncias()
        repo(fuente = vacia).crear(
            negocioId = "negocio-1",
            tipo = TiposContenidoDenunciable.FOTO_CLIENTE,
            referencia = "42",
            usuarioDenunciadoUid = null,
            motivo = MotivosDenuncia.OTRO,
            descripcion = "   "
        )
        assertNull(vacia.descripcion)

        val larga = FakeDenuncias()
        repo(fuente = larga).crear(
            negocioId = "negocio-1",
            tipo = TiposContenidoDenunciable.FOTO_CLIENTE,
            referencia = "42",
            usuarioDenunciadoUid = null,
            motivo = MotivosDenuncia.OTRO,
            descripcion = "a".repeat(MAX_DESCRIPCION_DENUNCIA + 50)
        )
        assertEquals(MAX_DESCRIPCION_DENUNCIA, larga.descripcion?.length)
    }

    @Test
    fun errorDePermisosSeTraduce() = runTest {
        val fuente = FakeDenuncias(error = ErrorRemoto(ErrorAuth.SIN_PERMISOS))
        val resultado = repo(fuente = fuente).crear(
            negocioId = "negocio-1",
            tipo = TiposContenidoDenunciable.NOTIFICACION,
            referencia = "n-1",
            usuarioDenunciadoUid = null,
            motivo = MotivosDenuncia.OTRO,
            descripcion = null
        )

        assertFalse(resultado.exito)
        assertEquals(ErrorDenuncia.SIN_PERMISOS, resultado.error)
    }

    @Test
    fun errorDeConexionSeTraduce() = runTest {
        val fuente = FakeDenuncias(error = ErrorRemoto(ErrorAuth.SIN_CONEXION))
        val resultado = repo(fuente = fuente).crear(
            negocioId = "negocio-1",
            tipo = TiposContenidoDenunciable.NOTIFICACION,
            referencia = "n-1",
            usuarioDenunciadoUid = null,
            motivo = MotivosDenuncia.OTRO,
            descripcion = null
        )

        assertEquals(ErrorDenuncia.SIN_CONEXION, resultado.error)
    }

    @Test
    fun errorDesconocidoSeTraduce() = runTest {
        val fuente = FakeDenuncias(error = IllegalStateException("boom"))
        val resultado = repo(fuente = fuente).crear(
            negocioId = "negocio-1",
            tipo = TiposContenidoDenunciable.NOTIFICACION,
            referencia = "n-1",
            usuarioDenunciadoUid = null,
            motivo = MotivosDenuncia.OTRO,
            descripcion = null
        )

        assertEquals(ErrorDenuncia.DESCONOCIDO, resultado.error)
    }
}
