package com.roberto.trazys.cliente.model

import kotlinx.serialization.Serializable

/**
 * Sesión de un servicio leída desde Firestore (`sesiones/{idSesion}`).
 *
 * - [fecha]: epoch millis de la medianoche del día en zona local.
 * - [hora]: formato "HH:mm" (24 h).
 * - [horaDesdeReserva]: hora local ("HH:mm") a partir de la cual el cliente
 *   puede reservar; `null` = reservas abiertas desde el inicio del día.
 * - [asistentes]: mapa `{ clienteId: nombre }` mantenido por la Cloud
 *   Function de reservas; solo contiene nombres.
 */
@Serializable
data class Sesion(
    val idSesion: Int,
    val negocioId: String,
    val idServicio: Int,
    val fecha: Long,
    val hora: String,
    val duracionMinutos: Int,
    val capacidad: Int,
    val plazasDisponibles: Int,
    val horaDesdeReserva: String? = null,
    val asistentes: Map<String, String> = emptyMap()
)
