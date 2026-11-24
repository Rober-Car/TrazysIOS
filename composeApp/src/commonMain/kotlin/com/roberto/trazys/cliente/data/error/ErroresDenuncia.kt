package com.roberto.trazys.cliente.data.error

/** Errores de dominio al crear una denuncia UGC. */
enum class ErrorDenuncia {
    SIN_SESION,
    TIPO_INVALIDO,
    MOTIVO_INVALIDO,
    REFERENCIA_INVALIDA,
    SIN_PERMISOS,
    SIN_CONEXION,
    DESCONOCIDO
}

/** Resultado de crear una denuncia. */
data class ResultadoDenuncia(
    val exito: Boolean,
    val error: ErrorDenuncia? = null
) {
    companion object {
        fun correcto() = ResultadoDenuncia(exito = true)
        fun fallo(error: ErrorDenuncia) = ResultadoDenuncia(exito = false, error = error)
    }
}

internal fun ErrorAuth.comoDenuncia(): ErrorDenuncia = when (this) {
    ErrorAuth.SIN_SESION -> ErrorDenuncia.SIN_SESION
    ErrorAuth.SIN_PERMISOS -> ErrorDenuncia.SIN_PERMISOS
    ErrorAuth.SIN_CONEXION -> ErrorDenuncia.SIN_CONEXION
    else -> ErrorDenuncia.DESCONOCIDO
}
