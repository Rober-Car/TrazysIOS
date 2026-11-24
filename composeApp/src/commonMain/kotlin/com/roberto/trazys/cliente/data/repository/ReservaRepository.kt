package com.roberto.trazys.cliente.data.repository

import com.roberto.trazys.cliente.data.error.ErrorAuth
import com.roberto.trazys.cliente.data.error.ErrorConsulta
import com.roberto.trazys.cliente.data.error.ErrorRemoto
import com.roberto.trazys.cliente.data.error.ErrorReserva
import com.roberto.trazys.cliente.data.error.ResultadoReserva
import com.roberto.trazys.cliente.data.error.comoConsulta
import com.roberto.trazys.cliente.data.firebase.FuenteAutenticacion
import com.roberto.trazys.cliente.data.firebase.FuenteReservas
import com.roberto.trazys.cliente.model.Reserva

/** Resultado de leer las reservas propias. */
data class ResultadoReservas(
    val reservas: List<Reserva> = emptyList(),
    val error: ErrorConsulta? = null
) {
    val exito: Boolean get() = error == null
}

/**
 * Reservas del Cliente. Crear y cancelar se delegan en las callables del
 * backend, que derivan la identidad desde Auth; aquí solo se valida la sesión
 * local y se traducen los errores de dominio.
 */
class ReservaRepository(
    private val auth: FuenteAutenticacion,
    private val reservas: FuenteReservas
) {

    suspend fun reservar(sesionId: Int): ResultadoReserva =
        operar(sesionId, esCancelar = false)

    suspend fun cancelar(sesionId: Int): ResultadoReserva =
        operar(sesionId, esCancelar = true)

    private suspend fun operar(sesionId: Int, esCancelar: Boolean): ResultadoReserva {
        if (auth.uid == null) return ResultadoReserva.fallo(ErrorReserva.SIN_SESION)
        if (sesionId <= 0) return ResultadoReserva.fallo(ErrorReserva.SESION_INVALIDA)
        return try {
            if (esCancelar) reservas.cancelar(sesionId) else reservas.reservar(sesionId)
        } catch (e: ErrorRemoto) {
            ResultadoReserva.fallo(e.error.comoReserva())
        } catch (_: Exception) {
            ResultadoReserva.fallo(ErrorReserva.DESCONOCIDO)
        }
    }

    suspend fun cargarReservas(clienteId: Int, negocioId: String): ResultadoReservas {
        if (auth.uid == null) return ResultadoReservas(error = ErrorConsulta.SIN_SESION)
        if (clienteId <= 0 || negocioId.isBlank()) {
            return ResultadoReservas(error = ErrorConsulta.DESCONOCIDO)
        }
        return try {
            ResultadoReservas(reservas = reservas.reservasDe(clienteId, negocioId))
        } catch (e: ErrorRemoto) {
            ResultadoReservas(error = e.error.comoConsulta())
        } catch (_: Exception) {
            ResultadoReservas(error = ErrorConsulta.DESCONOCIDO)
        }
    }

    private fun ErrorAuth.comoReserva(): ErrorReserva = when (this) {
        ErrorAuth.SIN_SESION -> ErrorReserva.SIN_SESION
        ErrorAuth.SIN_PERMISOS -> ErrorReserva.SIN_PERMISOS
        ErrorAuth.SIN_CONEXION -> ErrorReserva.SIN_CONEXION
        else -> ErrorReserva.DESCONOCIDO
    }
}
