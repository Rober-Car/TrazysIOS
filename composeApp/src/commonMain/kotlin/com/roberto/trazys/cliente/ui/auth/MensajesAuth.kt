package com.roberto.trazys.cliente.ui.auth

import com.roberto.trazys.cliente.data.error.ErrorAuth

/** Mensajes visibles de los errores de autenticación. */
fun mensajeErrorAuth(error: ErrorAuth): String = when (error) {
    ErrorAuth.SIN_CONEXION -> "Sin conexión. Comprueba tu red e inténtalo de nuevo."
    ErrorAuth.SIN_PERMISOS -> "No tienes permisos para esta operación."
    ErrorAuth.EMAIL_INVALIDO -> "Introduce un correo electrónico válido."
    ErrorAuth.CONTRASENA_CORTA -> "La contraseña debe tener al menos 6 caracteres."
    ErrorAuth.CREDENCIALES_INVALIDAS,
    ErrorAuth.CUENTA_NO_EXISTE -> "Correo o contraseña incorrectos."
    ErrorAuth.CONTRASENA_ACTUAL_INCORRECTA -> "La contraseña actual no es correcta."
    ErrorAuth.CONTRASENAS_NO_COINCIDEN -> "Las contraseñas no coinciden."
    ErrorAuth.CAMPOS_INCOMPLETOS -> "Completa todos los campos."
    ErrorAuth.EMAIL_YA_REGISTRADO -> "Ya existe una cuenta con este correo."
    ErrorAuth.CUENTA_DESACTIVADA ->
        "Tu cuenta está desactivada. Contacta con el centro."
    ErrorAuth.ROL_INCORRECTO -> "Esta cuenta no pertenece a un cliente."
    ErrorAuth.SIN_PERFIL -> "No se pudo leer tu perfil. Inténtalo de nuevo."
    ErrorAuth.SIN_SESION -> "Tu sesión ha caducado. Vuelve a iniciar sesión."
    ErrorAuth.SESION_ANTIGUA -> "Por seguridad, vuelve a iniciar sesión."
    ErrorAuth.DESCONOCIDO -> "No se pudo completar la operación. Inténtalo de nuevo."
}
