package com.roberto.trazys.cliente.data.firebase

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.auth
import dev.gitlive.firebase.firestore.firestore
import dev.gitlive.firebase.functions.functions
import dev.gitlive.firebase.storage.storage
import dev.gitlive.firebase.storage.toData
import kotlinx.serialization.Serializable

@Serializable
data class NegocioSpike(
    val nombre: String = "",
    val logo: String = ""
)

@Serializable
data class SolicitudReservaSpike(
    val negocioId: String,
    val fecha: String
)

@Serializable
data class ReservaSpike(
    val id: String = "",
    val estado: String = ""
)

/**
 * Spike de la Fase 1A: ejercita las operaciones reales de GitLive que usará
 * el Cliente (auth, lectura Firestore, transacción de vinculación, callable
 * y Storage). Solo compila en Windows; la ejecución real requiere Mac,
 * `GoogleService-Info.plist` y el backend desplegado.
 */
object FirebaseSpike {

    suspend fun iniciarSesionAnonima(): String? =
        Firebase.auth.signInAnonymously().user?.uid

    suspend fun leerNegocio(negocioId: String): NegocioSpike? {
        val documento = Firebase.firestore
            .collection("negocios")
            .document(negocioId)
            .get()
        return if (documento.exists) documento.data<NegocioSpike>() else null
    }

    /** Transacción de vinculación: solo escribe si el negocio existe. */
    suspend fun vincularCliente(negocioId: String, uid: String) {
        val referencia = Firebase.firestore.collection("negocios").document(negocioId)
        Firebase.firestore.runTransaction {
            val documento = get(referencia)
            check(documento.exists) { "El negocio no existe" }
            update(referencia, "clienteUid" to uid)
        }
    }

    suspend fun reservar(solicitud: SolicitudReservaSpike): ReservaSpike =
        Firebase.functions
            .httpsCallable("reservar")
            .invoke(solicitud)
            .data()

    suspend fun subirFoto(ruta: String, bytes: ByteArray): String {
        val referencia = Firebase.storage.reference.child(ruta)
        referencia.putData(bytes.toData())
        return referencia.getDownloadUrl()
    }
}
