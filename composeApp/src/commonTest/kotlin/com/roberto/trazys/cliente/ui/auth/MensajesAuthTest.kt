package com.roberto.trazys.cliente.ui.auth

import com.roberto.trazys.cliente.data.error.ErrorAuth
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class MensajesAuthTest {

    @Test
    fun todosLosErroresTienenMensajeVisible() {
        ErrorAuth.entries.forEach { error ->
            assertTrue(
                mensajeErrorAuth(error).isNotBlank(),
                "Falta mensaje para $error"
            )
        }
    }

    @Test
    fun credencialesYCuentaInexistenteCompartenMensajeGenerico() {
        assertEquals(
            mensajeErrorAuth(ErrorAuth.CREDENCIALES_INVALIDAS),
            mensajeErrorAuth(ErrorAuth.CUENTA_NO_EXISTE)
        )
        assertEquals(
            "Correo o contraseña incorrectos.",
            mensajeErrorAuth(ErrorAuth.CREDENCIALES_INVALIDAS)
        )
    }
}
