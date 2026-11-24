package com.roberto.trazys.cliente.data.repository

import com.roberto.trazys.cliente.data.error.ErrorConsulta
import com.roberto.trazys.cliente.data.error.ErrorRemoto
import com.roberto.trazys.cliente.data.error.comoConsulta
import com.roberto.trazys.cliente.data.firebase.FuenteSesiones
import com.roberto.trazys.cliente.model.Servicio
import com.roberto.trazys.cliente.model.Sesion

/** Resultado de cargar el servicio y sus sesiones. */
sealed interface ResultadoAgenda {
    data class Ok(val servicio: Servicio, val sesiones: List<Sesion>) : ResultadoAgenda

    /** El servicio no existe, está inactivo o no es legible por las Rules. */
    data object NoDisponible : ResultadoAgenda

    data class Fallo(val error: ErrorConsulta) : ResultadoAgenda
}

/**
 * Lectura de la agenda de un servicio. El Cliente solo puede ver servicios
 * activos de su negocio; el orden por fecha y hora se aplica aquí para que la
 * UI no dependa del orden remoto.
 */
class SesionRepository(
    private val sesiones: FuenteSesiones
) {

    suspend fun cargarAgenda(idServicio: Int, negocioId: String): ResultadoAgenda {
        if (idServicio <= 0 || negocioId.isBlank()) {
            return ResultadoAgenda.NoDisponible
        }
        return try {
            val servicio = sesiones.servicioActivo(idServicio, negocioId)
                ?: return ResultadoAgenda.NoDisponible
            val lista = sesiones.sesionesDeServicio(idServicio, negocioId)
                .sortedWith(compareBy({ it.fecha }, { it.hora }))
            ResultadoAgenda.Ok(servicio, lista)
        } catch (e: ErrorRemoto) {
            ResultadoAgenda.Fallo(e.error.comoConsulta())
        } catch (_: Exception) {
            ResultadoAgenda.Fallo(ErrorConsulta.DESCONOCIDO)
        }
    }

    /**
     * Sesión concreta para la vista de detalle/asistentes. Devuelve null si no
     * existe, no es legible o no pertenece al negocio.
     */
    suspend fun cargarSesion(idSesion: Int, negocioId: String): Sesion? {
        if (idSesion <= 0 || negocioId.isBlank()) return null
        return try {
            sesiones.sesion(idSesion, negocioId)
        } catch (_: Exception) {
            null
        }
    }
}
