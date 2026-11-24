package com.roberto.trazys.cliente.util

import com.roberto.trazys.cliente.data.error.ErrorAuth

private val PATRON_EMAIL = Regex("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")

fun emailValido(email: String): Boolean = PATRON_EMAIL.matches(email.trim())

/**
 * Validación pura de email y contraseña para registro y login. La política de
 * contraseña coincide con Firebase (mínimo 6 caracteres).
 */
fun validarCredenciales(email: String, contrasena: String): ErrorAuth? = when {
    !emailValido(email) -> ErrorAuth.EMAIL_INVALIDO
    contrasena.length < 6 -> ErrorAuth.CONTRASENA_CORTA
    else -> null
}

/** Validación pura del formulario de cambio de contraseña. */
fun validarCambioContrasena(
    actual: String,
    nueva: String,
    repetida: String
): ErrorAuth? = when {
    actual.isBlank() || nueva.isBlank() || repetida.isBlank() -> ErrorAuth.CAMPOS_INCOMPLETOS
    nueva.length < 6 -> ErrorAuth.CONTRASENA_CORTA
    nueva != repetida -> ErrorAuth.CONTRASENAS_NO_COINCIDEN
    else -> null
}
