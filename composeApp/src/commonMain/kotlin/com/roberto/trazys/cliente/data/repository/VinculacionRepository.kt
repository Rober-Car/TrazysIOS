package com.roberto.trazys.cliente.data.repository

import com.roberto.trazys.cliente.data.error.ErrorAuth
import com.roberto.trazys.cliente.data.error.ErrorRemoto
import com.roberto.trazys.cliente.data.error.ErrorVinculacion
import com.roberto.trazys.cliente.data.error.ResultadoVinculacion
import com.roberto.trazys.cliente.data.firebase.FuenteAutenticacion
import com.roberto.trazys.cliente.data.firebase.FuenteClientesVinculacion
import com.roberto.trazys.cliente.data.firebase.FuenteCodigos
import com.roberto.trazys.cliente.data.firebase.FuenteIndices
import com.roberto.trazys.cliente.data.firebase.FuenteNegocios
import com.roberto.trazys.cliente.data.firebase.FuenteNotificacionesAdmin
import com.roberto.trazys.cliente.data.firebase.FuentePerfiles
import com.roberto.trazys.cliente.data.firebase.ResultadoCreacionFicha
import com.roberto.trazys.cliente.data.firebase.ResultadoFichaVinculada
import com.roberto.trazys.cliente.util.normalizarDni

/**
 * Vinculación del Cliente mediante código maestro + DNI.
 *
 * - **VÍA 1:** el centro ya creó la ficha; se localiza por
 *   `indices_clientes/{negocioId}_{dni}` y se escribe el UID sobre ella.
 * - **VÍA 2:** no hay ficha y el perfil pendiente está completo; se crea la
 *   ficha, su índice y el vínculo en una única transacción.
 *
 * El perfil pendiente solo se borra al completar la vinculación.
 */
class VinculacionRepository(
    private val auth: FuenteAutenticacion,
    private val codigos: FuenteCodigos,
    private val negocios: FuenteNegocios,
    private val perfiles: FuentePerfiles,
    private val indices: FuenteIndices,
    private val clientes: FuenteClientesVinculacion,
    private val notificaciones: FuenteNotificacionesAdmin
) {

    suspend fun vincularConCodigoYDNI(
        codigoMaestro: String,
        dni: String
    ): ResultadoVinculacion {
        val uid = auth.uid ?: return ResultadoVinculacion.fallo(ErrorVinculacion.SIN_SESION)
        val codigo = codigoMaestro.trim()
        if (codigo.isBlank()) {
            return ResultadoVinculacion.fallo(ErrorVinculacion.CAMPOS_INCOMPLETOS)
        }
        val dniNorm = normalizarDni(dni)
        if (dniNorm.isBlank()) {
            return ResultadoVinculacion.fallo(ErrorVinculacion.CAMPOS_INCOMPLETOS)
        }

        return try {
            val negocioId = codigos.negocioIdDe(codigo)
                ?: return ResultadoVinculacion.fallo(ErrorVinculacion.CODIGO_INVALIDO)

            val negocio = negocios.leerPublico(negocioId)
            if (negocio == null || negocio.codigoMaestro != codigo) {
                return ResultadoVinculacion.fallo(ErrorVinculacion.CENTRO_INCOHERENTE)
            }

            perfiles.guardarDeclaracion(uid, dniNorm, negocioId)

            val clienteId = indices.clienteIdDe(negocioId, dniNorm)
            if (clienteId != null) {
                when (clientes.vincularFicha(clienteId, uid, negocioId)) {
                    ResultadoFichaVinculada.Vinculada ->
                        completar(uid, clienteId, negocioId)

                    ResultadoFichaVinculada.YaVinculada ->
                        ResultadoVinculacion.fallo(ErrorVinculacion.DNI_YA_VINCULADO)

                    ResultadoFichaVinculada.NoExiste ->
                        ResultadoVinculacion.fallo(ErrorVinculacion.FICHA_NO_EXISTE)
                }
            } else {
                val perfil = perfiles.leer(uid)
                if (perfil == null || !perfil.completo) {
                    return ResultadoVinculacion.fallo(
                        ErrorVinculacion.REQUIERE_PERFIL,
                        requiereCompletarPerfil = true
                    )
                }
                when (val creada = clientes.crearFicha(uid, negocioId, dniNorm, perfil)) {
                    is ResultadoCreacionFicha.Creada ->
                        completar(uid, creada.clienteId, negocioId)

                    ResultadoCreacionFicha.IndiceOcupado ->
                        ResultadoVinculacion.fallo(ErrorVinculacion.DNI_YA_VINCULADO)

                    ResultadoCreacionFicha.SinIdDisponible,
                    ResultadoCreacionFicha.IdOcupado ->
                        ResultadoVinculacion.fallo(ErrorVinculacion.ID_NO_DISPONIBLE)
                }
            }
        } catch (e: ErrorRemoto) {
            ResultadoVinculacion.fallo(traducir(e.error))
        } catch (_: Exception) {
            ResultadoVinculacion.fallo(ErrorVinculacion.DESCONOCIDO)
        }
    }

    private suspend fun completar(
        uid: String,
        clienteId: Int,
        negocioId: String
    ): ResultadoVinculacion {
        try {
            perfiles.borrar(uid)
        } catch (_: Exception) {
            // El perfil se reintentará borrar; la vinculación ya está hecha.
        }
        try {
            notificaciones.notificarVinculacion(negocioId, clienteId)
        } catch (_: Exception) {
            // Aviso best-effort: nunca condiciona el resultado.
        }
        return ResultadoVinculacion.correcto(clienteId, negocioId)
    }

    private fun traducir(error: ErrorAuth): ErrorVinculacion = when (error) {
        ErrorAuth.SIN_CONEXION -> ErrorVinculacion.SIN_CONEXION
        ErrorAuth.SIN_PERMISOS -> ErrorVinculacion.SIN_PERMISOS
        ErrorAuth.SIN_SESION -> ErrorVinculacion.SIN_SESION
        else -> ErrorVinculacion.DESCONOCIDO
    }
}
