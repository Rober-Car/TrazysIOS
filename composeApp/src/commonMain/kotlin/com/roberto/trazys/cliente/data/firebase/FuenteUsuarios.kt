package com.roberto.trazys.cliente.data.firebase

import com.google.firebase.firestore.FirebaseFirestoreException
import com.roberto.trazys.cliente.data.error.ErrorAuth
import com.roberto.trazys.cliente.data.error.ErrorRemoto
import com.roberto.trazys.cliente.model.Usuario
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.firestore.FirebaseFirestore
import dev.gitlive.firebase.firestore.firestore

/** Acceso al documento `usuarios/{uid}`. */
interface FuenteUsuarios {
    suspend fun leer(uid: String): Usuario?
    suspend fun crearCliente(uid: String)
    suspend fun actualizarVinculo(uid: String, clienteId: Int, negocioId: String)
    suspend fun borrar(uid: String)
}

internal fun errorFirestoreDe(e: Throwable): ErrorAuth = when (e) {
    is FirebaseFirestoreException -> when (e.code) {
        FirebaseFirestoreException.Code.PERMISSION_DENIED,
        FirebaseFirestoreException.Code.UNAUTHENTICATED -> ErrorAuth.SIN_PERMISOS

        FirebaseFirestoreException.Code.UNAVAILABLE,
        FirebaseFirestoreException.Code.DEADLINE_EXCEEDED -> ErrorAuth.SIN_CONEXION

        else -> ErrorAuth.DESCONOCIDO
    }

    else -> {
        val texto = e.message.orEmpty().lowercase()
        when {
            texto.contains("permission") -> ErrorAuth.SIN_PERMISOS
            texto.contains("network") || texto.contains("offline") ||
                texto.contains("timeout") || texto.contains("unreachable") ->
                ErrorAuth.SIN_CONEXION

            else -> ErrorAuth.DESCONOCIDO
        }
    }
}

/** Implementación real sobre GitLive Firestore. */
class FuenteUsuariosGitLive(
    private val db: FirebaseFirestore = Firebase.firestore
) : FuenteUsuarios {

    private fun documento(uid: String) = db.collection(COLECCION).document(uid)

    override suspend fun leer(uid: String): Usuario? {
        val documento = try {
            documento(uid).get()
        } catch (e: Exception) {
            throw ErrorRemoto(errorFirestoreDe(e), e)
        }
        if (!documento.exists) return null
        return Usuario(
            rol = if (documento.contains("rol")) documento.get<String>("rol") else "",
            activo = documento.contains("activo") && documento.get<Boolean>("activo"),
            clienteId = if (documento.contains("clienteId")) {
                documento.get<Long?>("clienteId")?.toInt()
            } else {
                null
            },
            negocioId = if (documento.contains("negocioId")) {
                documento.get<String?>("negocioId")
            } else {
                null
            }
        )
    }

    override suspend fun crearCliente(uid: String) {
        try {
            documento(uid).set(
                mapOf(
                    "rol" to ROL_CLIENTE,
                    "activo" to true,
                    "clienteId" to null,
                    "negocioId" to null
                )
            )
        } catch (e: Exception) {
            throw ErrorRemoto(errorFirestoreDe(e), e)
        }
    }

    override suspend fun actualizarVinculo(uid: String, clienteId: Int, negocioId: String) {
        try {
            documento(uid).updateFields {
                "clienteId" to clienteId
                "negocioId" to negocioId
            }
        } catch (e: Exception) {
            throw ErrorRemoto(errorFirestoreDe(e), e)
        }
    }

    override suspend fun borrar(uid: String) {
        try {
            documento(uid).delete()
        } catch (e: Exception) {
            throw ErrorRemoto(errorFirestoreDe(e), e)
        }
    }

    private companion object {
        const val COLECCION = "usuarios"
        const val ROL_CLIENTE = "CLIENTE"
    }
}
