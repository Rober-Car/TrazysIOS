package com.roberto.trazys.cliente.data.error

/** Errores de dominio de las operaciones de cuenta y baja. */
enum class ErrorCuenta {
    SIN_SESION,
    SIN_PERMISOS,
    SIN_CONEXION,
    CAMPOS_INCOMPLETOS,
    CONTRASENA_INCORRECTA,
    BAJA_PENDIENTE,
    DESCONOCIDO
}

/** Resultado de una operación de cuenta (solicitar baja, eliminar cuenta). */
data class ResultadoCuenta(
    val exito: Boolean,
    val error: ErrorCuenta? = null
) {
    companion object {
        fun correcto() = ResultadoCuenta(exito = true)
        fun fallo(error: ErrorCuenta) = ResultadoCuenta(exito = false, error = error)
    }
}

internal fun ErrorAuth.comoCuenta(): ErrorCuenta = when (this) {
    ErrorAuth.SIN_SESION -> ErrorCuenta.SIN_SESION
    ErrorAuth.SIN_PERMISOS -> ErrorCuenta.SIN_PERMISOS
    ErrorAuth.SIN_CONEXION -> ErrorCuenta.SIN_CONEXION
    else -> ErrorCuenta.DESCONOCIDO
}
