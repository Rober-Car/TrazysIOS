package com.roberto.trazys.cliente.util

/**
 * Términos de uso del cliente.
 *
 * La aceptación se guarda localmente por UID y versión. Si cambia la versión
 * vigente, el usuario debe volver a aceptarla.
 */
object TerminosDeUso {

    const val VERSION = "1.0"

    const val CLAVE_TERMINOS_UID = "terminos_uid"
    const val CLAVE_TERMINOS_VERSION = "terminos_version"
    const val CLAVE_TERMINOS_FECHA = "terminos_fecha"

    fun aceptado(
        uidGuardado: String?,
        versionGuardada: String?,
        uidActual: String
    ): Boolean = uidGuardado == uidActual && versionGuardada == VERSION
}
