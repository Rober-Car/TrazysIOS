package com.roberto.trazys.cliente.data.firebase

import com.roberto.trazys.cliente.data.error.ErrorRemoto
import com.roberto.trazys.cliente.model.Cliente
import com.roberto.trazys.cliente.model.EstadoCliente
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.firestore.FirebaseFirestore
import dev.gitlive.firebase.firestore.firestore

/** Lectura de la ficha propia en `clientes/{idCliente}`. */
interface FuenteClientes {
    suspend fun leerFicha(idCliente: Int): Cliente?
}

/** Implementación real sobre GitLive Firestore. */
class FuenteClientesGitLive(
    private val db: FirebaseFirestore = Firebase.firestore
) : FuenteClientes {

    override suspend fun leerFicha(idCliente: Int): Cliente? {
        val documento = try {
            db.collection(COLECCION).document(idCliente.toString()).get()
        } catch (e: Exception) {
            throw ErrorRemoto(errorFirestoreDe(e), e)
        }
        if (!documento.exists) return null
        return Cliente(
            idCliente = documento.entero("idCliente") ?: idCliente,
            negocioId = documento.texto("negocioId"),
            firebaseUid = documento.textoOpcional("firebaseUid"),
            nombre = documento.texto("nombre"),
            apellidos = documento.texto("apellidos"),
            dni = documento.texto("dni"),
            telefono = documento.texto("telefono"),
            email = documento.textoOpcional("email"),
            foto = documento.texto("foto"),
            fechaNacimiento = documento.fechaMilis("fechaNacimiento"),
            fechaRegistro = documento.fechaMilis("fechaRegistro") ?: 0L,
            fechaAlta = documento.fechaMilis("fechaAlta"),
            fechaBaja = documento.fechaMilis("fechaBaja"),
            estado = estadoClienteDe(documento.texto("estado")),
            serviciosContratados = (documento.get("serviciosContratados") as? List<*>)
                ?.mapNotNull { (it as? Number)?.toInt() }
                ?: emptyList(),
            fechaInicioActual = documento.fechaMilis("fechaInicioActual"),
            fechaFinActual = documento.fechaMilis("fechaFinActual")
        )
    }

    private fun estadoClienteDe(valor: String): EstadoCliente = when (valor) {
        "ACTIVO" -> EstadoCliente.ACTIVO
        "MOROSO" -> EstadoCliente.MOROSO
        "BAJA" -> EstadoCliente.BAJA
        "ARCHIVADO" -> EstadoCliente.ARCHIVADO
        "REGISTRADO" -> EstadoCliente.REGISTRADO
        else -> EstadoCliente.REGISTRADO
    }

    private companion object {
        const val COLECCION = "clientes"
    }
}
