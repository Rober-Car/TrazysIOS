package com.roberto.trazys.cliente.data.error

/** Errores de dominio del flujo código maestro + DNI. */
enum class ErrorVinculacion {
    SIN_SESION,
    CAMPOS_INCOMPLETOS,
    CODIGO_INVALIDO,
    CENTRO_INCOHERENTE,
    DNI_YA_VINCULADO,
    FICHA_NO_EXISTE,
    PERFIL_NO_DISPONIBLE,
    ID_NO_DISPONIBLE,
    REQUIERE_PERFIL,
    SIN_PERMISOS,
    SIN_CONEXION,
    DESCONOCIDO
}

/**
 * Resultado de un intento de vinculación.
 *
 * [requiereCompletarPerfil] no es un error: indica que el centro todavía no
 * tiene ficha para ese DNI y que el usuario debe completar sus datos para crear
 * una (VÍA 2).
 */
data class ResultadoVinculacion(
    val exito: Boolean,
    val error: ErrorVinculacion? = null,
    val clienteId: Int? = null,
    val negocioId: String? = null,
    val requiereCompletarPerfil: Boolean = false
) {
    companion object {
        fun correcto(clienteId: Int, negocioId: String) = ResultadoVinculacion(
            exito = true,
            clienteId = clienteId,
            negocioId = negocioId
        )

        fun fallo(
            error: ErrorVinculacion,
            requiereCompletarPerfil: Boolean = false
        ) = ResultadoVinculacion(
            exito = false,
            error = error,
            requiereCompletarPerfil = requiereCompletarPerfil
        )
    }
}
