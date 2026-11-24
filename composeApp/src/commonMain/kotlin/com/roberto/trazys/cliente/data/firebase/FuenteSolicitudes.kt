package com.roberto.trazys.cliente.data.firebase

import com.roberto.trazys.cliente.data.error.ErrorRemoto
import com.roberto.trazys.cliente.model.EstadoSolicitud
import com.roberto.trazys.cliente.model.SolicitudBaja
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.firestore.FirebaseFirestore
import dev.gitlive.firebase.firestore.Timestamp
import dev.gitlive.firebase.firestore.firestore
import dev.gitlive.firebase.firestore.fromMilliseconds

/**
 * Solicitudes de baja propias en `solicitudes/{idSolicitud}`. Solo se
 * consultan las del cliente vinculado y se crean con ID determinista
 * `baja_{clienteId}_{fechaSolicitud}`.
 */
interface FuenteSolicitudes {
    suspend fun solicitudesDe(clienteId: Int, negocioId: String): List<SolicitudBaja>
    suspend fun crearSolicitud(
        idSolicitud: String,
        clienteId: Int,
        negocioId: String,
        firebaseUid: String,
        fechaSolicitud: Long,
        motivo: String?
    )
}

/** Implementación real sobre GitLive Firestore. */
class FuenteSolicitudesGitLive(
    private val db: FirebaseFirestore = Firebase.firestore
) : FuenteSolicitudes {

    override suspend fun solicitudesDe(
        clienteId: Int,
        negocioId: String
    ): List<SolicitudBaja> {
        return try {
            db.collection(COLECCION)
                .where { "negocioId" equalTo negocioId }
                .where { "idCliente" equalTo clienteId }
                .get()
                .documents
                .mapNotNull { documento ->
                    val idSolicitud = documento.textoOpcional("idSolicitud")
                        ?: return@mapNotNull null
                    val fechaSolicitud = documento.fechaMilis("fechaSolicitud")
                        ?: return@mapNotNull null
                    SolicitudBaja(
                        idSolicitud = idSolicitud,
                        negocioId = documento.textoOpcional("negocioId") ?: negocioId,
                        idCliente = documento.entero("idCliente") ?: clienteId,
                        firebaseUid = documento.textoOpcional("firebaseUid"),
                        fechaSolicitud = fechaSolicitud,
                        estado = estadoDe(documento.textoOpcional("estado")),
                        fechaResolucion = documento.fechaMilis("fechaResolucion"),
                        resueltaPor = documento.textoOpcional("resueltaPor"),
                        motivo = documento.textoOpcional("motivo")
                    )
                }
                .sortedByDescending { it.fechaSolicitud }
        } catch (e: Exception) {
            throw ErrorRemoto(errorFirestoreDe(e), e)
        }
    }

    override suspend fun crearSolicitud(
        idSolicitud: String,
        clienteId: Int,
        negocioId: String,
        firebaseUid: String,
        fechaSolicitud: Long,
        motivo: String?
    ) {
        val datos = mutableMapOf<String, Any>(
            "idSolicitud" to idSolicitud,
            "negocioId" to negocioId,
            "idCliente" to clienteId,
            "firebaseUid" to firebaseUid,
            "fechaSolicitud" to Timestamp.fromMilliseconds(fechaSolicitud.toDouble()),
            "estado" to ESTADO_PENDIENTE,
            "tipo" to "BAJA"
        )
        motivo?.takeIf { it.isNotBlank() }?.let { datos["motivo"] = it }
        try {
            db.collection(COLECCION).document(idSolicitud).set(datos)
        } catch (e: Exception) {
            throw ErrorRemoto(errorFirestoreDe(e), e)
        }
    }

    private fun estadoDe(valor: String?): EstadoSolicitud = when (valor) {
        "ACEPTADA" -> EstadoSolicitud.ACEPTADA
        "RECHAZADA" -> EstadoSolicitud.RECHAZADA
        else -> EstadoSolicitud.PENDIENTE
    }

    private companion object {
        const val COLECCION = "solicitudes"
        const val ESTADO_PENDIENTE = "PENDIENTE"
    }
}
