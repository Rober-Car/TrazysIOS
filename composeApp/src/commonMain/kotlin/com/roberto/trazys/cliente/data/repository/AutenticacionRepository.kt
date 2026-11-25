package com.roberto.trazys.cliente.data.repository

import com.roberto.trazys.cliente.data.error.ErrorAuth
import com.roberto.trazys.cliente.data.error.ErrorRemoto
import com.roberto.trazys.cliente.data.error.ResultadoSesion
import com.roberto.trazys.cliente.data.firebase.FuenteAutenticacion
import com.roberto.trazys.cliente.data.firebase.FuenteUsuarios
import com.roberto.trazys.cliente.util.emailValido
import com.roberto.trazys.cliente.util.validarCambioContrasena
import com.roberto.trazys.cliente.util.validarCredenciales

/**
 * Reglas de sesión del Cliente: alta con `usuarios/{uid}` coherente, login que
 * exige rol CLIENTE y cuenta activa, recuperación sin revelar cuentas y cambio
 * de contraseña con reautenticación previa.
 */
class AutenticacionRepository(
    private val fuenteAuth: FuenteAutenticacion,
    private val fuenteUsuarios: FuenteUsuarios
) {

    fun haySesionActiva(): Boolean = fuenteAuth.uid != null

    fun uidActual(): String? = fuenteAuth.uid

    suspend fun registrar(email: String, contrasena: String): ResultadoSesion {
        validarCredenciales(email, contrasena)?.let { return ResultadoSesion.Fallo(it) }

        val uid = try {
            fuenteAuth.registrar(email, contrasena)
        } catch (e: ErrorRemoto) {
            return ResultadoSesion.Fallo(e.error)
        }

        return try {
            fuenteUsuarios.crearCliente(uid)
            ResultadoSesion.Exito
        } catch (e: Exception) {
            // Sin documento usuarios no hay cuenta utilizable: se deshace el alta.
            cerrarYSilenciar()
            ResultadoSesion.Fallo((e as? ErrorRemoto)?.error ?: ErrorAuth.DESCONOCIDO)
        }
    }

    suspend fun iniciarSesion(email: String, contrasena: String): ResultadoSesion {
        validarCredenciales(email, contrasena)?.let { return ResultadoSesion.Fallo(it) }

        val uid = try {
            fuenteAuth.iniciarSesion(email, contrasena)
        } catch (e: ErrorRemoto) {
            return ResultadoSesion.Fallo(e.error)
        }

        val usuario = try {
            fuenteUsuarios.leer(uid)
        } catch (e: Exception) {
            cerrarYSilenciar()
            return ResultadoSesion.Fallo((e as? ErrorRemoto)?.error ?: ErrorAuth.DESCONOCIDO)
        }

        if (usuario == null) {
            cerrarYSilenciar()
            return ResultadoSesion.Fallo(ErrorAuth.SIN_PERFIL)
        }
        if (!usuario.activo) {
            cerrarYSilenciar()
            return ResultadoSesion.Fallo(ErrorAuth.CUENTA_DESACTIVADA)
        }
        if (usuario.rol != ROL_CLIENTE) {
            cerrarYSilenciar()
            return ResultadoSesion.Fallo(ErrorAuth.ROL_INCORRECTO)
        }
        return ResultadoSesion.Exito
    }

    /**
     * La recuperación responde igual exista o no la cuenta (no revela datos).
     * Solo un fallo real de red o desconocido se comunica.
     */
    suspend fun recuperarContrasena(email: String): ResultadoSesion {
        if (!emailValido(email)) return ResultadoSesion.Fallo(ErrorAuth.EMAIL_INVALIDO)
        return try {
            fuenteAuth.enviarRecuperacion(email)
            ResultadoSesion.Exito
        } catch (e: ErrorRemoto) {
            when (e.error) {
                ErrorAuth.CUENTA_NO_EXISTE,
                ErrorAuth.EMAIL_INVALIDO,
                ErrorAuth.CREDENCIALES_INVALIDAS -> ResultadoSesion.Exito

                else -> ResultadoSesion.Fallo(e.error)
            }
        }
    }

    suspend fun cambiarContrasena(
        actual: String,
        nueva: String,
        repetida: String
    ): ResultadoSesion {
        validarCambioContrasena(actual, nueva, repetida)?.let {
            return ResultadoSesion.Fallo(it)
        }
        try {
            fuenteAuth.reautenticar(actual)
        } catch (e: ErrorRemoto) {
            val error = if (e.error == ErrorAuth.CREDENCIALES_INVALIDAS ||
                e.error == ErrorAuth.CUENTA_NO_EXISTE
            ) {
                ErrorAuth.CONTRASENA_ACTUAL_INCORRECTA
            } else {
                e.error
            }
            return ResultadoSesion.Fallo(error)
        }
        return try {
            fuenteAuth.cambiarContrasena(nueva)
            ResultadoSesion.Exito
        } catch (e: ErrorRemoto) {
            ResultadoSesion.Fallo(e.error)
        }
    }

    suspend fun cerrarSesion(): ResultadoSesion = try {
        fuenteAuth.cerrarSesion()
        ResultadoSesion.Exito
    } catch (e: ErrorRemoto) {
        ResultadoSesion.Fallo(e.error)
    }

    private suspend fun cerrarYSilenciar() {
        runCatching { fuenteAuth.cerrarSesion() }
    }

    private companion object {
        const val ROL_CLIENTE = "CLIENTE"
    }
}
