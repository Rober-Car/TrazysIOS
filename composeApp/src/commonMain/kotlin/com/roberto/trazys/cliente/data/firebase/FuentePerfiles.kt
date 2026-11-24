package com.roberto.trazys.cliente.data.firebase

import com.roberto.trazys.cliente.data.error.ErrorRemoto
import com.roberto.trazys.cliente.model.PerfilPendiente
import com.roberto.trazys.cliente.util.normalizarDni
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.firestore.FirebaseFirestore
import dev.gitlive.firebase.firestore.firestore

/**
 * Operaciones sobre `perfiles_pendientes/{uid}`. Las excepciones se traducen a
 * [ErrorRemoto] para que el repositorio distinga "no hay perfil" de un fallo de
 * red o permisos.
 */
interface FuentePerfiles {
    suspend fun guardar(uid: String, perfil: PerfilPendiente)
    suspend fun guardarDeclaracion(uid: String, dni: String, negocioId: String)
    suspend fun leer(uid: String): PerfilPendiente?
    suspend fun borrar(uid: String)
}

/** Implementación real sobre GitLive Firestore. */
class FuentePerfilesGitLive(
    private val db: FirebaseFirestore = Firebase.firestore
) : FuentePerfiles {

    private fun documento(uid: String) = db.collection(COLECCION).document(uid)

    override suspend fun guardar(uid: String, perfil: PerfilPendiente) {
        try {
            documento(uid).set(
                mapOf(
                    "nombre" to perfil.nombre,
                    "apellidos" to perfil.apellidos,
                    "dni" to normalizarDni(perfil.dni),
                    "telefono" to perfil.telefono,
                    "email" to perfil.email,
                    "foto" to perfil.foto,
                    "fechaNacimiento" to perfil.fechaNacimiento
                )
            )
        } catch (e: Exception) {
            throw ErrorRemoto(errorFirestoreDe(e), e)
        }
    }

    /** `merge = true` conserva el perfil completo de VÍA 2 al declarar la VÍA 1. */
    override suspend fun guardarDeclaracion(uid: String, dni: String, negocioId: String) {
        try {
            documento(uid).set(
                mapOf(
                    "dni" to normalizarDni(dni),
                    "negocioId" to negocioId
                ),
                merge = true
            )
        } catch (e: Exception) {
            throw ErrorRemoto(errorFirestoreDe(e), e)
        }
    }

    override suspend fun leer(uid: String): PerfilPendiente? {
        val documento = try {
            documento(uid).get()
        } catch (e: Exception) {
            throw ErrorRemoto(errorFirestoreDe(e), e)
        }
        if (!documento.exists) return null
        return PerfilPendiente(
            nombre = documento.campoString("nombre"),
            apellidos = documento.campoString("apellidos"),
            dni = documento.campoString("dni"),
            telefono = documento.campoString("telefono"),
            email = if (documento.contains("email")) documento.get<String?>("email") else null,
            foto = documento.campoString("foto"),
            fechaNacimiento = if (documento.contains("fechaNacimiento")) {
                documento.get<Long?>("fechaNacimiento")
            } else {
                null
            }
        )
    }

    override suspend fun borrar(uid: String) {
        try {
            documento(uid).delete()
        } catch (e: Exception) {
            throw ErrorRemoto(errorFirestoreDe(e), e)
        }
    }

    private companion object {
        const val COLECCION = "perfiles_pendientes"
    }
}

/** Lectura de un campo de texto opcional evitando `getString` (no existe en GitLive). */
internal fun dev.gitlive.firebase.firestore.DocumentSnapshot.campoString(campo: String): String =
    if (contains(campo)) get<String>(campo) else ""
