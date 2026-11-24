package com.roberto.trazys.cliente.data.error

/** Errores de dominio de autenticación y vinculación. La UI traduce el código. */
enum class ErrorAuth {
    SIN_CONEXION,
    SIN_PERMISOS,
    EMAIL_INVALIDO,
    CONTRASENA_CORTA,
    CREDENCIALES_INVALIDAS,
    CONTRASENA_ACTUAL_INCORRECTA,
    CONTRASENAS_NO_COINCIDEN,
    CAMPOS_INCOMPLETOS,
    CUENTA_NO_EXISTE,
    EMAIL_YA_REGISTRADO,
    CUENTA_DESACTIVADA,
    ROL_INCORRECTO,
    SIN_PERFIL,
    SIN_SESION,
    SESION_ANTIGUA,
    DESCONOCIDO
}

/** Resultado de una operación de sesión. */
sealed interface ResultadoSesion {
    data object Exito : ResultadoSesion
    data class Fallo(val error: ErrorAuth) : ResultadoSesion
}

/**
 * Error tipado que lanzan las fuentes remotas para que los repositorios no
 * dependan de las excepciones concretas de Firebase.
 */
class ErrorRemoto(
    val error: ErrorAuth,
    cause: Throwable? = null
) : Exception(cause)
