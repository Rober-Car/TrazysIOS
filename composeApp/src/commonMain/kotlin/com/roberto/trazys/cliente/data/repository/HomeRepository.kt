package com.roberto.trazys.cliente.data.repository

import com.roberto.trazys.cliente.data.error.ErrorAuth
import com.roberto.trazys.cliente.data.error.ErrorRemoto
import com.roberto.trazys.cliente.data.firebase.FuenteClientes
import com.roberto.trazys.cliente.model.EstadoHomeCliente
import com.roberto.trazys.cliente.util.estadoHomeDe
import kotlinx.coroutines.flow.first

/**
 * Estado del Home: lee la ficha vinculada y deriva el indicador (activo, pago
 * vencido, baja, registrado o archivado). El reloj se inyecta para poder
 * probar los períodos vencidos sin depender del equipo.
 */
class HomeRepository(
    private val preferences: PreferencesRepository,
    private val clientes: FuenteClientes,
    private val ahora: () -> Long
) {

    suspend fun cargarEstado(): EstadoHomeCliente {
        val idCliente = preferences.idCliente.first()
            ?: return EstadoHomeCliente()

        return try {
            val ficha = clientes.leerFicha(idCliente)
            if (ficha == null) {
                EstadoHomeCliente(error = MENSAJE_SIN_FICHA)
            } else {
                estadoHomeDe(ficha, ahora())
            }
        } catch (e: ErrorRemoto) {
            EstadoHomeCliente(error = mensajeDe(e.error))
        } catch (_: Exception) {
            EstadoHomeCliente(error = MENSAJE_ERROR)
        }
    }

    private fun mensajeDe(error: ErrorAuth): String = when (error) {
        ErrorAuth.SIN_CONEXION -> MENSAJE_SIN_CONEXION
        ErrorAuth.SIN_PERMISOS, ErrorAuth.SIN_SESION -> MENSAJE_SIN_PERMISOS
        else -> MENSAJE_ERROR
    }

    private companion object {
        const val MENSAJE_SIN_FICHA = "No se encontró la ficha del cliente"
        const val MENSAJE_SIN_CONEXION = "Sin conexión"
        const val MENSAJE_SIN_PERMISOS = "No se pudo acceder a la ficha"
        const val MENSAJE_ERROR = "No se pudo cargar la ficha"
    }
}
