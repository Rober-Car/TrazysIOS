package com.roberto.trazys.cliente.model

import kotlinx.serialization.Serializable

/**
 * Servicio del centro leído desde Firestore (`servicios/{idServicio}`).
 * El cliente solo puede leer servicios activos de su propio negocio.
 *
 * [permiteCombinarDia]: si es `false`, no puede haber más de una reserva el
 * mismo día que implique esta actividad. Por defecto `true` si el campo falta.
 */
@Serializable
data class Servicio(
    val idServicio: Int,
    val negocioId: String,
    val nombre: String,
    val descripcion: String,
    val activo: Boolean,
    val permiteCombinarDia: Boolean = true
)
