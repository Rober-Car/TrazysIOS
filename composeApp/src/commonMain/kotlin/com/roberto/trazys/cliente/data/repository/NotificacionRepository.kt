package com.roberto.trazys.cliente.data.repository

import com.roberto.trazys.cliente.data.error.ErrorConsulta
import com.roberto.trazys.cliente.data.error.ErrorRemoto
import com.roberto.trazys.cliente.data.error.comoConsulta
import com.roberto.trazys.cliente.data.firebase.FuenteNotificaciones
import com.roberto.trazys.cliente.model.Notificacion

/** Resultado de cargar el buzón propio de notificaciones. */
data class ResultadoNotificaciones(
    val notificaciones: List<Notificacion> = emptyList(),
    val error: ErrorConsulta? = null
)

/**
 * Buzón del cliente: lectura ordenada por fecha de envío y marcar/eliminar
 * solo documentos del propio buzón. Las operaciones de escritura son
 * tolerantes (devuelven `false` sin lanzar).
 */
class NotificacionRepository(
    private val fuente: FuenteNotificaciones
) {

    suspend fun cargar(clienteId: Int, negocioId: String): ResultadoNotificaciones {
        val lista = try {
            fuente.notificacionesDe(clienteId, negocioId)
        } catch (e: ErrorRemoto) {
            return ResultadoNotificaciones(error = e.error.comoConsulta())
        } catch (_: Exception) {
            return ResultadoNotificaciones(error = ErrorConsulta.DESCONOCIDO)
        }
        return ResultadoNotificaciones(notificaciones = lista)
    }

    suspend fun marcarLeida(id: String): Boolean =
        id.isNotBlank() && fuente.marcarLeida(id)

    suspend fun eliminar(id: String): Boolean =
        id.isNotBlank() && fuente.eliminar(id)
}
