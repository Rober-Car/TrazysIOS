package com.roberto.trazys.cliente.data.firebase

import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.roberto.trazys.cliente.data.error.ErrorAuth
import com.roberto.trazys.cliente.data.error.ErrorRemoto
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.EmailAuthProvider
import dev.gitlive.firebase.auth.FirebaseAuth
import dev.gitlive.firebase.auth.auth

/**
 * Operaciones de Firebase Authentication que necesita el Cliente. La interfaz
 * permite probar el repositorio con dobles en JVM sin Firebase real.
 */
interface FuenteAutenticacion {
    val uid: String?
    val email: String?
    suspend fun registrar(email: String, contrasena: String): String
    suspend fun iniciarSesion(email: String, contrasena: String): String
    suspend fun cerrarSesion()
    suspend fun enviarRecuperacion(email: String)
    suspend fun reautenticar(contrasena: String)
    suspend fun cambiarContrasena(nueva: String)
    suspend fun borrarCuenta()
}

/**
 * Traduce las excepciones de Firebase a [ErrorAuth]. Los códigos concretos de
 * GitLive son los mismos `ERROR_*` en Android e iOS.
 */
internal fun errorAuthDe(e: Throwable): ErrorAuth = when (e) {
    is FirebaseAuthWeakPasswordException -> ErrorAuth.CONTRASENA_CORTA
    is FirebaseAuthUserCollisionException -> ErrorAuth.EMAIL_YA_REGISTRADO
    is FirebaseAuthInvalidUserException -> ErrorAuth.CUENTA_NO_EXISTE
    is FirebaseAuthRecentLoginRequiredException -> ErrorAuth.SESION_ANTIGUA
    is FirebaseAuthInvalidCredentialsException ->
        if (e.errorCode == "ERROR_INVALID_EMAIL") ErrorAuth.EMAIL_INVALIDO
        else ErrorAuth.CREDENCIALES_INVALIDAS

    else -> {
        val texto = e.message.orEmpty().lowercase()
        when {
            texto.contains("network") || texto.contains("offline") ||
                texto.contains("timeout") || texto.contains("unreachable") ->
                ErrorAuth.SIN_CONEXION

            texto.contains("password") -> ErrorAuth.CREDENCIALES_INVALIDAS
            else -> ErrorAuth.DESCONOCIDO
        }
    }
}

/** Implementación real sobre GitLive Firebase (auth nativo). */
class FuenteAutenticacionGitLive(
    private val auth: FirebaseAuth = Firebase.auth
) : FuenteAutenticacion {

    override val uid: String? get() = auth.currentUser?.uid
    override val email: String? get() = auth.currentUser?.email

    override suspend fun registrar(email: String, contrasena: String): String {
        val usuario = try {
            auth.createUserWithEmailAndPassword(email.trim(), contrasena).user
        } catch (e: Exception) {
            throw ErrorRemoto(errorAuthDe(e), e)
        }
        return usuario?.uid ?: throw ErrorRemoto(ErrorAuth.DESCONOCIDO)
    }

    override suspend fun iniciarSesion(email: String, contrasena: String): String {
        val usuario = try {
            auth.signInWithEmailAndPassword(email.trim(), contrasena).user
        } catch (e: Exception) {
            throw ErrorRemoto(errorAuthDe(e), e)
        }
        return usuario?.uid ?: throw ErrorRemoto(ErrorAuth.DESCONOCIDO)
    }

    override suspend fun cerrarSesion() {
        try {
            auth.signOut()
        } catch (e: Exception) {
            throw ErrorRemoto(errorAuthDe(e), e)
        }
    }

    override suspend fun enviarRecuperacion(email: String) {
        try {
            auth.sendPasswordResetEmail(email.trim())
        } catch (e: Exception) {
            throw ErrorRemoto(errorAuthDe(e), e)
        }
    }

    override suspend fun reautenticar(contrasena: String) {
        val usuario = auth.currentUser ?: throw ErrorRemoto(ErrorAuth.SIN_SESION)
        val correo = usuario.email ?: throw ErrorRemoto(ErrorAuth.SIN_PERFIL)
        try {
            usuario.reauthenticate(EmailAuthProvider.credential(correo, contrasena))
        } catch (e: Exception) {
            throw ErrorRemoto(errorAuthDe(e), e)
        }
    }

    override suspend fun cambiarContrasena(nueva: String) {
        val usuario = auth.currentUser ?: throw ErrorRemoto(ErrorAuth.SIN_SESION)
        try {
            usuario.updatePassword(nueva)
        } catch (e: Exception) {
            throw ErrorRemoto(errorAuthDe(e), e)
        }
    }

    override suspend fun borrarCuenta() {
        val usuario = auth.currentUser ?: throw ErrorRemoto(ErrorAuth.SIN_SESION)
        try {
            usuario.delete()
        } catch (e: Exception) {
            throw ErrorRemoto(errorAuthDe(e), e)
        }
    }
}
