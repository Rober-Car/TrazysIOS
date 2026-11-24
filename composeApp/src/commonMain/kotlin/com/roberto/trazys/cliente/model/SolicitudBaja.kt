package com.roberto.trazys.cliente.model

import kotlinx.serialization.Serializable

/** Estado de una solicitud de baja (mismos nombres que el contrato remoto). */
enum class EstadoSolicitud {
    PENDIENTE,
    ACEPTADA,
    RECHAZADA
}

/**
 * Solicitud de baja del cliente leída desde Firestore
 * (`solicitudes/{idSolicitud}`). Mientras está [EstadoSolicitud.PENDIENTE] el
 * cliente permanece ACTIVO; solo pasa a BAJA cuando el ADMIN la acepta.
 */
@Serializable
data class SolicitudBaja(
    val idSolicitud: String,
    val negocioId: String,
    val idCliente: Int,
    val firebaseUid: String?,
    val fechaSolicitud: Long,
    val estado: EstadoSolicitud,
    val fechaResolucion: Long? = null,
    val resueltaPor: String? = null,
    val motivo: String? = null
)
