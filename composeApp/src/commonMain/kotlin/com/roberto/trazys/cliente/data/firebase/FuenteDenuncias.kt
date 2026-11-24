package com.roberto.trazys.cliente.data.firebase

import com.roberto.trazys.cliente.data.error.ErrorRemoto
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.firestore.FirebaseFirestore
import dev.gitlive.firebase.firestore.Timestamp
import dev.gitlive.firebase.firestore.firestore
import dev.gitlive.firebase.firestore.fromMilliseconds

/**
 * Escritura de denuncias UGC en `denuncias/{idDenuncia}`. El denunciante lo
 * deriva el repositorio de la sesión; aquí se guarda el documento con estado
 * PENDIENTE para que lo resuelva el ADMIN.
 */
interface FuenteDenuncias {
    suspend fun crear(
        idDenuncia: String,
        negocioId: String,
        denuncianteUid: String,
        tipo: String,
        referencia: String,
        usuarioDenunciadoUid: String?,
        motivo: String,
        descripcion: String?,
        fecha: Long
    )
}

/** Implementación real sobre GitLive Firestore. */
class FuenteDenunciasGitLive(
    private val db: FirebaseFirestore = Firebase.firestore
) : FuenteDenuncias {

    override suspend fun crear(
        idDenuncia: String,
        negocioId: String,
        denuncianteUid: String,
        tipo: String,
        referencia: String,
        usuarioDenunciadoUid: String?,
        motivo: String,
        descripcion: String?,
        fecha: Long
    ) {
        val datos = mutableMapOf<String, Any>(
            "negocioId" to negocioId,
            "denuncianteUid" to denuncianteUid,
            "tipo" to tipo,
            "referencia" to referencia,
            "motivo" to motivo,
            "fecha" to Timestamp.fromMilliseconds(fecha.toDouble()),
            "estado" to ESTADO_PENDIENTE
        )
        usuarioDenunciadoUid?.takeIf { it.isNotBlank() }?.let {
            datos["usuarioDenunciadoUid"] = it
        }
        descripcion?.takeIf { it.isNotBlank() }?.let {
            datos["descripcion"] = it
        }
        try {
            db.collection(COLECCION).document(idDenuncia).set(datos)
        } catch (e: Exception) {
            throw ErrorRemoto(errorFirestoreDe(e), e)
        }
    }

    private companion object {
        const val COLECCION = "denuncias"
        const val ESTADO_PENDIENTE = "PENDIENTE"
    }
}
