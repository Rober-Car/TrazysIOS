package com.roberto.trazys.cliente.model

import kotlinx.serialization.Serializable

/**
 * Reserva remota del cliente en una sesión. El documento usa el identificador
 * determinista `{clienteId}_{sesionId}`.
 */
@Serializable
data class Reserva(
    val idReserva: String,
    val negocioId: String,
    val sesionId: Int,
    val clienteId: Int,
    val fechaReserva: Long
)

/** Estados visuales posibles de una sesión en el flujo de reserva. */
enum class EstadoReserva {
    RESERVAR,
    RESERVADA,
    COMPLETA;

    companion object {
        fun de(reservadaPorMi: Boolean, plazasDisponibles: Int): EstadoReserva =
            when {
                reservadaPorMi -> RESERVADA
                plazasDisponibles <= 0 -> COMPLETA
                else -> RESERVAR
            }
    }
}
