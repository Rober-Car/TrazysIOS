package com.roberto.trazys.cliente.model

/**
 * Datos personales de un Cliente registrado que aún no pertenece a un centro
 * (`perfiles_pendientes/{uid}`). [fechaNacimiento] es opcional.
 */
data class PerfilPendiente(
    val nombre: String = "",
    val apellidos: String = "",
    val dni: String = "",
    val telefono: String = "",
    val email: String? = null,
    val foto: String = "",
    val fechaNacimiento: Long? = null
) {
    /** VÍA 2 exige nombre, apellidos y teléfono para crear la ficha. */
    val completo: Boolean
        get() = nombre.isNotBlank() && apellidos.isNotBlank() && telefono.isNotBlank()
}
