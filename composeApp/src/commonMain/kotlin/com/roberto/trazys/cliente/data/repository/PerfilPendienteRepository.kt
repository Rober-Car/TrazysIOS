package com.roberto.trazys.cliente.data.repository

import com.roberto.trazys.cliente.data.error.ErrorAuth
import com.roberto.trazys.cliente.data.error.ErrorRemoto
import com.roberto.trazys.cliente.data.error.ResultadoSesion
import com.roberto.trazys.cliente.data.firebase.FuenteAutenticacion
import com.roberto.trazys.cliente.data.firebase.FuentePerfiles
import com.roberto.trazys.cliente.model.PerfilPendiente

/**
 * Perfil temporal (`perfiles_pendientes/{uid}`) del Cliente sin centro. Solo se
 * borra cuando la vinculación se completa con éxito.
 */
class PerfilPendienteRepository(
    private val fuente: FuentePerfiles,
    private val auth: FuenteAutenticacion
) {

    suspend fun guardar(perfil: PerfilPendiente): ResultadoSesion {
        if (!perfil.completo || perfil.dni.isBlank()) {
            return ResultadoSesion.Fallo(ErrorAuth.CAMPOS_INCOMPLETOS)
        }
        val uid = auth.uid ?: return ResultadoSesion.Fallo(ErrorAuth.SIN_SESION)
        return try {
            fuente.guardar(uid, perfil)
            ResultadoSesion.Exito
        } catch (e: ErrorRemoto) {
            ResultadoSesion.Fallo(e.error)
        }
    }

    /** Devuelve `null` si no hay sesión, no hay perfil o la lectura falla. */
    suspend fun leer(): PerfilPendiente? {
        val uid = auth.uid ?: return null
        return try {
            fuente.leer(uid)
        } catch (_: ErrorRemoto) {
            null
        }
    }

    suspend fun borrar(): ResultadoSesion {
        val uid = auth.uid ?: return ResultadoSesion.Fallo(ErrorAuth.SIN_SESION)
        return try {
            fuente.borrar(uid)
            ResultadoSesion.Exito
        } catch (e: ErrorRemoto) {
            ResultadoSesion.Fallo(e.error)
        }
    }
}
