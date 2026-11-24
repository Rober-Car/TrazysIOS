package com.roberto.trazys.cliente.data.repository

import com.roberto.trazys.cliente.data.error.ErrorConsulta
import com.roberto.trazys.cliente.data.error.ErrorCuenta
import com.roberto.trazys.cliente.data.error.ErrorRemoto
import com.roberto.trazys.cliente.data.error.ResultadoCuenta
import com.roberto.trazys.cliente.data.error.comoConsulta
import com.roberto.trazys.cliente.data.error.comoCuenta
import com.roberto.trazys.cliente.data.firebase.FuenteAutenticacion
import com.roberto.trazys.cliente.data.firebase.FuenteSolicitudes
import com.roberto.trazys.cliente.model.EstadoSolicitud
import com.roberto.trazys.cliente.model.SolicitudBaja
import com.roberto.trazys.cliente.time.horaActualMilis

/** Resultado de leer las solicitudes de baja propias. */
data class ResultadoSolicitudes(
    val solicitudes: List<SolicitudBaja> = emptyList(),
    val error: ErrorConsulta? = null
) {
    val exito: Boolean get() = error == null
}

/**
 * Solicitud de baja: la app solo crea una solicitud PENDIENTE con ID
 * determinista; la baja efectiva la resuelve el ADMIN. Nunca se duplica una
 * solicitud ya pendiente.
 */
class SolicitudBajaRepository(
    private val auth: FuenteAutenticacion,
    private val solicitudes: FuenteSolicitudes,
    private val ahora: () -> Long = ::horaActualMilis
) {

    suspend fun cargar(clienteId: Int, negocioId: String): ResultadoSolicitudes {
        if (auth.uid == null) return ResultadoSolicitudes(error = ErrorConsulta.SIN_SESION)
        if (clienteId <= 0 || negocioId.isBlank()) {
            return ResultadoSolicitudes(error = ErrorConsulta.DESCONOCIDO)
        }
        return try {
            ResultadoSolicitudes(
                solicitudes = solicitudes.solicitudesDe(clienteId, negocioId)
            )
        } catch (e: ErrorRemoto) {
            ResultadoSolicitudes(error = e.error.comoConsulta())
        } catch (_: Exception) {
            ResultadoSolicitudes(error = ErrorConsulta.DESCONOCIDO)
        }
    }

    suspend fun solicitarBaja(
        clienteId: Int,
        negocioId: String,
        motivo: String?
    ): ResultadoCuenta {
        val uid = auth.uid ?: return ResultadoCuenta.fallo(ErrorCuenta.SIN_SESION)
        if (clienteId <= 0 || negocioId.isBlank()) {
            return ResultadoCuenta.fallo(ErrorCuenta.DESCONOCIDO)
        }
        return try {
            val existentes = solicitudes.solicitudesDe(clienteId, negocioId)
            if (existentes.any { it.estado == EstadoSolicitud.PENDIENTE }) {
                return ResultadoCuenta.fallo(ErrorCuenta.BAJA_PENDIENTE)
            }
            val fechaSolicitud = ahora()
            solicitudes.crearSolicitud(
                idSolicitud = "baja_${clienteId}_$fechaSolicitud",
                clienteId = clienteId,
                negocioId = negocioId,
                firebaseUid = uid,
                fechaSolicitud = fechaSolicitud,
                motivo = motivo
            )
            ResultadoCuenta.correcto()
        } catch (e: ErrorRemoto) {
            ResultadoCuenta.fallo(e.error.comoCuenta())
        } catch (_: Exception) {
            ResultadoCuenta.fallo(ErrorCuenta.DESCONOCIDO)
        }
    }
}
