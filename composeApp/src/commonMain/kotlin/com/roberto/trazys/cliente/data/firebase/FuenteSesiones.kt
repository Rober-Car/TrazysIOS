package com.roberto.trazys.cliente.data.firebase

import com.google.firebase.firestore.FirebaseFirestoreException
import com.roberto.trazys.cliente.data.error.ErrorRemoto
import com.roberto.trazys.cliente.model.Servicio
import com.roberto.trazys.cliente.model.Sesion
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.firestore.DocumentSnapshot
import dev.gitlive.firebase.firestore.FirebaseFirestore
import dev.gitlive.firebase.firestore.firestore

/**
 * Lectura de servicios y sesiones para el Cliente.
 *
 * Las Rules solo permiten leer servicios activos del propio negocio y evalúan
 * las consultas por documento, así que el acceso se hace **por servicio**:
 * primero el servicio y después sus sesiones filtradas por servicio y negocio.
 */
interface FuenteSesiones {
    suspend fun servicioActivo(idServicio: Int, negocioId: String): Servicio?
    suspend fun sesionesDeServicio(idServicio: Int, negocioId: String): List<Sesion>
    suspend fun sesion(idSesion: Int, negocioId: String): Sesion?
}

/** Implementación real sobre GitLive Firestore. */
class FuenteSesionesGitLive(
    private val db: FirebaseFirestore = Firebase.firestore
) : FuenteSesiones {

    override suspend fun servicioActivo(idServicio: Int, negocioId: String): Servicio? {
        val documento = try {
            db.collection(COLECCION_SERVICIOS).document(idServicio.toString()).get()
        } catch (e: Exception) {
            if (esPermiso(e)) return null
            throw ErrorRemoto(errorFirestoreDe(e), e)
        }
        if (!documento.exists) return null
        val servicio = Servicio(
            idServicio = documento.entero("idServicio") ?: idServicio,
            negocioId = documento.texto("negocioId"),
            nombre = documento.texto("nombre"),
            descripcion = documento.texto("descripcion"),
            activo = documento.booleano("activo") == true,
            permiteCombinarDia = documento.booleano("permiteCombinarDia") ?: true
        )
        return servicio.takeIf { it.negocioId == negocioId && it.activo }
    }

    override suspend fun sesionesDeServicio(
        idServicio: Int,
        negocioId: String
    ): List<Sesion> {
        return try {
            db.collection(COLECCION_SESIONES)
                .where { "idServicio" equalTo idServicio }
                .where { "negocioId" equalTo negocioId }
                .get()
                .documents
                .mapNotNull { documento ->
                    sesionDe(documento, idServicio, negocioId)
                }
        } catch (e: Exception) {
            if (esPermiso(e)) emptyList() else throw ErrorRemoto(errorFirestoreDe(e), e)
        }
    }

    override suspend fun sesion(idSesion: Int, negocioId: String): Sesion? {
        val documento = try {
            db.collection(COLECCION_SESIONES).document(idSesion.toString()).get()
        } catch (e: Exception) {
            if (esPermiso(e)) return null
            throw ErrorRemoto(errorFirestoreDe(e), e)
        }
        if (!documento.exists) return null
        return sesionDe(documento, null, negocioId)
    }

    private fun sesionDe(
        documento: DocumentSnapshot,
        idServicioPorDefecto: Int?,
        negocioId: String
    ): Sesion? {
        if (documento.texto("negocioId") != negocioId) return null
        val idSesion = documento.entero("idSesion")
            ?: documento.id.toIntOrNull()
            ?: return null
        val idServicio = documento.entero("idServicio")
            ?: idServicioPorDefecto
            ?: return null
        return Sesion(
            idSesion = idSesion,
            negocioId = negocioId,
            idServicio = idServicio,
            fecha = documento.fechaMilis("fecha") ?: 0L,
            hora = documento.texto("hora"),
            duracionMinutos = documento.entero("duracionMinutos") ?: 0,
            capacidad = documento.entero("capacidad") ?: 0,
            plazasDisponibles = documento.entero("plazasDisponibles") ?: 0,
            horaDesdeReserva = documento.textoOpcional("horaDesdeReserva"),
            asistentes = asistentesDe(documento.get("asistentes"))
        )
    }

    private fun esPermiso(e: Exception): Boolean =
        (e as? FirebaseFirestoreException)?.code ==
            FirebaseFirestoreException.Code.PERMISSION_DENIED ||
            e.message?.contains("permission", ignoreCase = true) == true

    private companion object {
        const val COLECCION_SERVICIOS = "servicios"
        const val COLECCION_SESIONES = "sesiones"
    }
}
