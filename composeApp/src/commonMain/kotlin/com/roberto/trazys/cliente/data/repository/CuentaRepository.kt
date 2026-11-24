package com.roberto.trazys.cliente.data.repository

import com.roberto.trazys.cliente.data.error.ErrorAuth
import com.roberto.trazys.cliente.data.error.ErrorCuenta
import com.roberto.trazys.cliente.data.error.ErrorRemoto
import com.roberto.trazys.cliente.data.error.ResultadoCuenta
import com.roberto.trazys.cliente.data.error.comoCuenta
import com.roberto.trazys.cliente.data.firebase.FuenteAutenticacion
import com.roberto.trazys.cliente.data.firebase.FuenteEliminacionCuenta

/**
 * Eliminación completa de la cuenta. Exige reautenticación reciente, delega el
 * borrado en la callable del backend (Admin SDK) y, solo tras el éxito remoto,
 * limpia los datos personales locales y cierra la sesión.
 */
class CuentaRepository(
    private val auth: FuenteAutenticacion,
    private val eliminacion: FuenteEliminacionCuenta,
    private val preferencias: PreferencesRepository
) {

    suspend fun eliminarCuenta(contrasena: String): ResultadoCuenta {
        if (auth.uid == null) return ResultadoCuenta.fallo(ErrorCuenta.SIN_SESION)
        if (contrasena.isBlank()) {
            return ResultadoCuenta.fallo(ErrorCuenta.CAMPOS_INCOMPLETOS)
        }

        try {
            auth.reautenticar(contrasena)
        } catch (e: ErrorRemoto) {
            val error = when (e.error) {
                ErrorAuth.CREDENCIALES_INVALIDAS,
                ErrorAuth.CUENTA_NO_EXISTE -> ErrorCuenta.CONTRASENA_INCORRECTA

                else -> e.error.comoCuenta()
            }
            return ResultadoCuenta.fallo(error)
        } catch (_: Exception) {
            return ResultadoCuenta.fallo(ErrorCuenta.DESCONOCIDO)
        }

        val resultado = try {
            eliminacion.eliminarCuenta()
        } catch (e: ErrorRemoto) {
            ResultadoCuenta.fallo(e.error.comoCuenta())
        } catch (_: Exception) {
            ResultadoCuenta.fallo(ErrorCuenta.DESCONOCIDO)
        }
        if (!resultado.exito) return resultado

        runCatching { preferencias.limpiarDatosPersonales() }
        runCatching { auth.cerrarSesion() }
        return ResultadoCuenta.correcto()
    }
}
