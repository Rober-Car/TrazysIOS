package com.roberto.trazys.cliente.data.firebase

import com.google.firebase.functions.FirebaseFunctionsException
import com.roberto.trazys.cliente.data.error.ErrorRemoto
import com.roberto.trazys.cliente.data.error.ErrorReserva
import com.roberto.trazys.cliente.data.error.ResultadoReserva
import com.roberto.trazys.cliente.model.Reserva
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.firestore.FirebaseFirestore
import dev.gitlive.firebase.firestore.firestore
import dev.gitlive.firebase.functions.functions

/**
 * Reservas del Cliente: escritura mediante las callables `reservar` y
 * `cancelarReserva` (la identidad la deriva el backend desde Auth) y lectura
 * local de las reservas propias.
 */
interface FuenteReservas {
    suspend fun reservar(sesionId: Int): ResultadoReserva
    suspend fun cancelar(sesionId: Int): ResultadoReserva
    suspend fun reservasDe(clienteId: Int, negocioId: String): List<Reserva>
}

/** Implementación real sobre GitLive Functions + Firestore. */
class FuenteReservasGitLive(
    private val db: FirebaseFirestore = Firebase.firestore
) : FuenteReservas {

    override suspend fun reservar(sesionId: Int): ResultadoReserva =
        invocar(FUNCION_RESERVAR, sesionId)

    override suspend fun cancelar(sesionId: Int): ResultadoReserva =
        invocar(FUNCION_CANCELAR, sesionId)

    private suspend fun invocar(nombre: String, sesionId: Int): ResultadoReserva {
        return try {
            Firebase.functions(REGION)
                .httpsCallable(nombre)
                .invoke(mapOf("sesionId" to sesionId))
            ResultadoReserva.correcto()
        } catch (e: FirebaseFunctionsException) {
            ResultadoReserva.fallo(errorReservaDe(e.message, e.code))
        } catch (e: Exception) {
            ResultadoReserva.fallo(errorReservaDeExcepcion(e))
        }
    }

    override suspend fun reservasDe(clienteId: Int, negocioId: String): List<Reserva> {
        return try {
            db.collection(COLECCION_RESERVAS)
                .where { "clienteId" equalTo clienteId }
                .where { "negocioId" equalTo negocioId }
                .get()
                .documents
                .mapNotNull { documento ->
                    val sesionId = documento.entero("sesionId") ?: return@mapNotNull null
                    Reserva(
                        idReserva = documento.texto("idReserva").ifBlank { documento.id },
                        negocioId = documento.texto("negocioId").ifBlank { negocioId },
                        sesionId = sesionId,
                        clienteId = documento.entero("clienteId") ?: clienteId,
                        fechaReserva = documento.fechaMilis("fechaReserva") ?: 0L
                    )
                }
        } catch (e: Exception) {
            throw ErrorRemoto(errorFirestoreDe(e), e)
        }
    }

    private companion object {
        const val REGION = "europe-west1"
        const val FUNCION_RESERVAR = "reservar"
        const val FUNCION_CANCELAR = "cancelarReserva"
        const val COLECCION_RESERVAS = "reservas"
    }
}

/**
 * Traduce un `HttpsError` de las callables de reserva. Los mensajes de negocio
 * del backend se reconocen por contenido; los códigos cubren el resto.
 */
internal fun errorReservaDe(
    mensaje: String?,
    codigo: FirebaseFunctionsException.Code
): ErrorReserva = errorReservaDeMensaje(mensaje) ?: when (codigo) {
    FirebaseFunctionsException.Code.UNAUTHENTICATED -> ErrorReserva.SIN_SESION
    FirebaseFunctionsException.Code.PERMISSION_DENIED -> ErrorReserva.SIN_PERMISOS
    FirebaseFunctionsException.Code.INVALID_ARGUMENT -> ErrorReserva.SESION_INVALIDA
    FirebaseFunctionsException.Code.UNAVAILABLE,
    FirebaseFunctionsException.Code.DEADLINE_EXCEEDED,
    FirebaseFunctionsException.Code.ABORTED,
    FirebaseFunctionsException.Code.RESOURCE_EXHAUSTED -> ErrorReserva.SIN_CONEXION
    else -> ErrorReserva.DESCONOCIDO
}

internal fun errorReservaDeMensaje(mensaje: String?): ErrorReserva? {
    val texto = mensaje.orEmpty().lowercase()
    return when {
        texto.contains("vinculado") -> ErrorReserva.NO_VINCULADO
        texto.contains("sesión indicada no es válida") ||
            texto.contains("sesion indicada no es valida") -> ErrorReserva.SESION_INVALIDA
        texto.contains("no permite reservar") ||
            texto.contains("dado de baja") ||
            texto.contains("no está activa") ||
            texto.contains("no esta activa") -> ErrorReserva.CUENTA_NO_ACTIVA
        texto.contains("sesión no existe") ||
            texto.contains("sesion no existe") -> ErrorReserva.SESION_NO_EXISTE
        texto.contains("sesión ha cambiado") ||
            texto.contains("sesion ha cambiado") -> ErrorReserva.SESION_CAMBIO
        texto.contains("no hay plazas") ||
            texto.contains("sin plazas") ||
            texto.contains("no tiene plazas") -> ErrorReserva.SIN_PLAZAS
        texto.contains("abren a las") ||
            texto.contains("se abren a las") -> ErrorReserva.FUERA_DE_APERTURA
        texto.contains("combinar") -> ErrorReserva.NO_COMBINABLE
        texto.contains("contratado") -> ErrorReserva.NO_CONTRATADO
        texto.contains("no pertenece a tu cuenta") -> ErrorReserva.SIN_PERMISOS
        else -> null
    }
}

internal fun errorReservaDeExcepcion(e: Throwable): ErrorReserva {
    val texto = e.message.orEmpty().lowercase()
    return when {
        texto.contains("network") ||
            texto.contains("offline") ||
            texto.contains("timeout") ||
            texto.contains("unreachable") -> ErrorReserva.SIN_CONEXION
        else -> ErrorReserva.DESCONOCIDO
    }
}
