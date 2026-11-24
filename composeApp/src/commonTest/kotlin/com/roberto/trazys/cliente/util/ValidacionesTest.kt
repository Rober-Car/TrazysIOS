package com.roberto.trazys.cliente.util

import com.roberto.trazys.cliente.data.error.ErrorAuth
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ValidacionesTest {

    @Test
    fun emailValidoAceptaFormatosNormalesYRechazaInvalidos() {
        assertTrue(emailValido("cliente@centro.es"))
        assertTrue(emailValido(" nombre.apellido@sub.dominio.com "))
        assertFalse(emailValido("sin-arroba.es"))
        assertFalse(emailValido("dos@@centro.es"))
        assertFalse(emailValido("sin@punto"))
        assertFalse(emailValido(""))
    }

    @Test
    fun credencialesExigenEmailValidoYContrasenaDeSeis() {
        assertNull(validarCredenciales("cliente@centro.es", "123456"))
        assertEquals(ErrorAuth.EMAIL_INVALIDO, validarCredenciales("cliente", "123456"))
        assertEquals(ErrorAuth.CONTRASENA_CORTA, validarCredenciales("cliente@centro.es", "12345"))
    }

    @Test
    fun cambioDeContrasenaValidaCamposYCoincidencia() {
        assertEquals(
            ErrorAuth.CAMPOS_INCOMPLETOS,
            validarCambioContrasena("", "nueva123", "nueva123")
        )
        assertEquals(
            ErrorAuth.CONTRASENA_CORTA,
            validarCambioContrasena("actual123", "123", "123")
        )
        assertEquals(
            ErrorAuth.CONTRASENAS_NO_COINCIDEN,
            validarCambioContrasena("actual123", "nueva123", "nueva124")
        )
        assertNull(validarCambioContrasena("actual123", "nueva123", "nueva123"))
    }
}
