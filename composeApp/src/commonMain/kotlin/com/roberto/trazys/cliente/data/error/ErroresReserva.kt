package com.roberto.trazys.cliente.data.error

/**
 * Errores de dominio de las operaciones de reserva. La UI traduce el código.
 *
 * Los motivos reflejan las causas que puede devolver la callable `reservar`
 * (y `cancelarReserva`) además de los fallos locales previos a la llamada.
 */
enum class ErrorReserva {
    SIN_SESION,
    SESION_INVALIDA,
    NO_VINCULADO,
    CUENTA_NO_ACTIVA,
    SESION_NO_EXISTE,
    SESION_CAMBIO,
    SIN_PLAZAS,
    FUERA_DE_APERTURA,
    NO_COMBINABLE,
    NO_CONTRATADO,
    SIN_PERMISOS,
    SIN_CONEXION,
    DESCONOCIDO
}

/** Resultado de reservar o cancelar una sesión. `exito` no lleva mensaje: la UI decide el texto. */
data class ResultadoReserva(
    val exito: Boolean,
    val error: ErrorReserva? = null
) {
    companion object {
        fun correcto() = ResultadoReserva(exito = true)
        fun fallo(error: ErrorReserva) = ResultadoReserva(exito = false, error = error)
    }
}

/** Errores de las lecturas (servicio, sesiones, reservas, ficha). */
enum class ErrorConsulta {
    SIN_SESION,
    SIN_PERMISOS,
    SIN_CONEXION,
    DESCONOCIDO
}

internal fun ErrorAuth.comoConsulta(): ErrorConsulta = when (this) {
    ErrorAuth.SIN_SESION -> ErrorConsulta.SIN_SESION
    ErrorAuth.SIN_PERMISOS -> ErrorConsulta.SIN_PERMISOS
    ErrorAuth.SIN_CONEXION -> ErrorConsulta.SIN_CONEXION
    else -> ErrorConsulta.DESCONOCIDO
}
