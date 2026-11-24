package com.roberto.trazys.cliente.data.firebase

import com.roberto.trazys.cliente.data.error.ErrorRemoto
import com.roberto.trazys.cliente.model.Notificacion
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.firestore.Direction
import dev.gitlive.firebase.firestore.FirebaseFirestore
import dev.gitlive.firebase.firestore.Timestamp
import dev.gitlive.firebase.firestore.firestore

/**
 * Buzón propio de notificaciones en
 * `notificaciones_por_destinatario/{clienteId}_{notificacionId}`. Solo se
 * consultan las del cliente vinculado (filtros que exigen las Rules).
 */
interface FuenteNotificaciones {
    suspend fun notificacionesDe(clienteId: Int, negocioId: String): List<Notificacion>
    suspend fun marcarLeida(docId: String): Boolean
    suspend fun eliminar(docId: String): Boolean
}

/** Implementación real sobre GitLive Firestore. */
class FuenteNotificacionesGitLive(
    private val db: FirebaseFirestore = Firebase.firestore
) : FuenteNotificaciones {

    override suspend fun notificacionesDe(
        clienteId: Int,
        negocioId: String
    ): List<Notificacion> {
        return try {
            db.collection(COLECCION)
                .where { "clienteId" equalTo clienteId }
                .where { "negocioId" equalTo negocioId }
                .orderBy("fechaEnvio", Direction.DESCENDING)
                .get()
                .documents
                .mapNotNull { documento ->
                    val notificacionId = documento.textoOpcional("notificacionId")
                        ?: return@mapNotNull null
                    val titulo = documento.textoOpcional("titulo")
                        ?: return@mapNotNull null
                    val mensaje = documento.textoOpcional("mensaje")
                        ?: return@mapNotNull null
                    val fechaEnvio = documento.fechaMilis("fechaEnvio")
                        ?: return@mapNotNull null
                    Notificacion(
                        id = documento.id,
                        notificacionId = notificacionId,
                        titulo = titulo,
                        mensaje = mensaje,
                        tipo = documento.textoOpcional("tipo") ?: "MANUAL",
                        origen = documento.textoOpcional("origen") ?: "MANUAL",
                        fechaEnvio = fechaEnvio,
                        leida = documento.booleano("leida") ?: false,
                        fechaLeida = documento.fechaMilis("fechaLeida"),
                        tituloEn = documento.textoOpcional("tituloEn"),
                        mensajeEn = documento.textoOpcional("mensajeEn"),
                        subtipo = documento.textoOpcional("subtipo")
                    )
                }
        } catch (e: Exception) {
            throw ErrorRemoto(errorFirestoreDe(e), e)
        }
    }

    override suspend fun marcarLeida(docId: String): Boolean {
        return try {
            db.collection(COLECCION).document(docId).updateFields {
                "leida" to true
                "fechaLeida" to Timestamp.now()
            }
            true
        } catch (_: Exception) {
            false
        }
    }

    override suspend fun eliminar(docId: String): Boolean {
        return try {
            db.collection(COLECCION).document(docId).delete()
            true
        } catch (_: Exception) {
            false
        }
    }

    private companion object {
        const val COLECCION = "notificaciones_por_destinatario"
    }
}
