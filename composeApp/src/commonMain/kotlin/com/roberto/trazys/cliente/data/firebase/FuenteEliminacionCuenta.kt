package com.roberto.trazys.cliente.data.firebase

import com.google.firebase.functions.FirebaseFunctionsException
import com.roberto.trazys.cliente.data.error.ErrorCuenta
import com.roberto.trazys.cliente.data.error.ResultadoCuenta
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.functions.functions

/**
 * Eliminación completa de la cuenta mediante la callable `eliminarMiCuenta`
 * (Admin SDK). La identidad la deriva el backend desde Auth; la app nunca
 * envía uid, clienteId ni negocioId.
 */
interface FuenteEliminacionCuenta {
    suspend fun eliminarCuenta(): ResultadoCuenta
}

/** Implementación real sobre GitLive Functions. */
class FuenteEliminacionCuentaGitLive : FuenteEliminacionCuenta {

    override suspend fun eliminarCuenta(): ResultadoCuenta {
        return try {
            Firebase.functions(REGION).httpsCallable(FUNCION).invoke()
            ResultadoCuenta.correcto()
        } catch (e: FirebaseFunctionsException) {
            ResultadoCuenta.fallo(errorCuentaDe(e.message, e.code))
        } catch (e: Exception) {
            ResultadoCuenta.fallo(errorCuentaDeExcepcion(e))
        }
    }

    private companion object {
        const val REGION = "europe-west1"
        const val FUNCION = "eliminarMiCuenta"
    }
}

/** Traduce un `HttpsError` de `eliminarMiCuenta`. */
internal fun errorCuentaDe(
    mensaje: String?,
    codigo: FirebaseFunctionsException.Code
): ErrorCuenta = errorCuentaDeMensaje(mensaje) ?: when (codigo) {
    FirebaseFunctionsException.Code.UNAUTHENTICATED -> ErrorCuenta.SIN_SESION
    FirebaseFunctionsException.Code.PERMISSION_DENIED -> ErrorCuenta.SIN_PERMISOS
    FirebaseFunctionsException.Code.UNAVAILABLE,
    FirebaseFunctionsException.Code.DEADLINE_EXCEEDED,
    FirebaseFunctionsException.Code.ABORTED,
    FirebaseFunctionsException.Code.RESOURCE_EXHAUSTED -> ErrorCuenta.SIN_CONEXION
    else -> ErrorCuenta.DESCONOCIDO
}

internal fun errorCuentaDeMensaje(mensaje: String?): ErrorCuenta? {
    val texto = mensaje.orEmpty().lowercase()
    return when {
        texto.contains("iniciar sesi") || texto.contains("sesión") ||
            texto.contains("sesion") -> ErrorCuenta.SIN_SESION
        texto.contains("no pertenece") || texto.contains("permission") ->
            ErrorCuenta.SIN_PERMISOS
        else -> null
    }
}

internal fun errorCuentaDeExcepcion(e: Throwable): ErrorCuenta {
    val texto = e.message.orEmpty().lowercase()
    return when {
        texto.contains("network") ||
            texto.contains("offline") ||
            texto.contains("timeout") ||
            texto.contains("unreachable") -> ErrorCuenta.SIN_CONEXION
        else -> ErrorCuenta.DESCONOCIDO
    }
}
