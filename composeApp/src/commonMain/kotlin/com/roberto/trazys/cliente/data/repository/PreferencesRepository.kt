package com.roberto.trazys.cliente.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import com.roberto.trazys.cliente.data.datastore.DNI_PENDIENTE_KEY
import com.roberto.trazys.cliente.data.datastore.IDIOMA_KEY
import com.roberto.trazys.cliente.data.datastore.ID_CLIENTE_KEY
import com.roberto.trazys.cliente.data.datastore.LOGO_NEGOCIO_KEY
import com.roberto.trazys.cliente.data.datastore.NEGOCIO_ID_KEY
import com.roberto.trazys.cliente.data.datastore.NOMBRE_NEGOCIO_KEY
import com.roberto.trazys.cliente.data.datastore.NOTIFICACIONES_ACTIVADAS_KEY
import com.roberto.trazys.cliente.data.datastore.TERMINOS_FECHA_KEY
import com.roberto.trazys.cliente.data.datastore.TERMINOS_UID_KEY
import com.roberto.trazys.cliente.data.datastore.TERMINOS_VERSION_KEY
import com.roberto.trazys.cliente.data.datastore.THEME_MODE_KEY
import com.roberto.trazys.cliente.time.horaActualMilis
import com.roberto.trazys.cliente.util.TerminosDeUso
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

/**
 * Preferencias locales del CLIENTE: tema, idioma, ficha vinculada, negocio,
 * DNI pendiente, avisos y aceptación de términos.
 *
 * Mantiene las mismas claves que la app Android para facilitar la paridad
 * funcional y una futura unificación.
 */
class PreferencesRepository(
    private val dataStore: DataStore<Preferences>
) {

    companion object {
        const val THEME_CLARO = "claro"
        const val THEME_OSCURO = "oscuro"
        const val THEME_SISTEMA = "sistema"

        const val IDIOMA_ES = "es"
        const val IDIOMA_EN = "en"
        const val IDIOMA_DEFECTO = IDIOMA_ES
    }

    val themeMode: Flow<String> = dataStore.data.map { preferences ->
        preferences[THEME_MODE_KEY] ?: THEME_SISTEMA
    }

    suspend fun setThemeMode(mode: String) {
        dataStore.edit { preferences ->
            preferences[THEME_MODE_KEY] = mode
        }
    }

    val idioma: Flow<String> = dataStore.data.map { preferences ->
        preferences[IDIOMA_KEY] ?: IDIOMA_DEFECTO
    }

    suspend fun setIdioma(idioma: String) {
        dataStore.edit { preferences ->
            preferences[IDIOMA_KEY] = idioma
        }
    }

    suspend fun obtenerIdioma(): String =
        dataStore.data.first()[IDIOMA_KEY] ?: IDIOMA_DEFECTO

    val idCliente: Flow<Int?> = dataStore.data.map { preferences ->
        preferences[ID_CLIENTE_KEY]
    }

    suspend fun setIdCliente(id: Int) {
        dataStore.edit { preferences ->
            preferences[ID_CLIENTE_KEY] = id
        }
    }

    suspend fun borrarIdCliente() {
        dataStore.edit { preferences ->
            preferences.remove(ID_CLIENTE_KEY)
        }
    }

    val negocioId: Flow<String?> = dataStore.data.map { preferences ->
        preferences[NEGOCIO_ID_KEY]
    }

    suspend fun setNegocioId(negocioId: String) {
        dataStore.edit { preferences ->
            preferences[NEGOCIO_ID_KEY] = negocioId
        }
    }

    suspend fun borrarNegocioId() {
        dataStore.edit { preferences ->
            preferences.remove(NEGOCIO_ID_KEY)
        }
    }

    val dniPendiente: Flow<String?> = dataStore.data.map { preferences ->
        preferences[DNI_PENDIENTE_KEY]
    }

    suspend fun setDniPendiente(dni: String) {
        dataStore.edit { preferences ->
            preferences[DNI_PENDIENTE_KEY] = dni.uppercase()
        }
    }

    suspend fun borrarDniPendiente() {
        dataStore.edit { preferences ->
            preferences.remove(DNI_PENDIENTE_KEY)
        }
    }

    val nombreNegocio: Flow<String> = dataStore.data.map { preferences ->
        preferences[NOMBRE_NEGOCIO_KEY] ?: ""
    }

    suspend fun setNombreNegocio(nombre: String) {
        dataStore.edit { preferences ->
            preferences[NOMBRE_NEGOCIO_KEY] = nombre.trim()
        }
    }

    val logoNegocio: Flow<String> = dataStore.data.map { preferences ->
        preferences[LOGO_NEGOCIO_KEY] ?: ""
    }

    suspend fun setLogoNegocio(url: String) {
        dataStore.edit { preferences ->
            preferences[LOGO_NEGOCIO_KEY] = url.trim()
        }
    }

    suspend fun terminosAceptados(uid: String): Boolean {
        val prefs = dataStore.data.first()
        return TerminosDeUso.aceptado(
            uidGuardado = prefs[TERMINOS_UID_KEY],
            versionGuardada = prefs[TERMINOS_VERSION_KEY],
            uidActual = uid
        )
    }

    suspend fun guardarAceptacionTerminos(uid: String) {
        dataStore.edit { prefs ->
            prefs[TERMINOS_UID_KEY] = uid
            prefs[TERMINOS_VERSION_KEY] = TerminosDeUso.VERSION
            prefs[TERMINOS_FECHA_KEY] = horaActualMilis().toString()
        }
    }

    val notificacionesActivadas: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[NOTIFICACIONES_ACTIVADAS_KEY] ?: true
    }

    suspend fun setNotificacionesActivadas(activas: Boolean) {
        dataStore.edit { preferences ->
            preferences[NOTIFICACIONES_ACTIVADAS_KEY] = activas
        }
    }

    /**
     * Limpia los datos de la ficha y del negocio al cerrar sesión. Conserva
     * tema, idioma, avisos y aceptación de términos (van ligados al dispositivo
     * o al UID, no a la sesión).
     */
    suspend fun limpiarSesion() {
        dataStore.edit { preferences ->
            preferences.remove(ID_CLIENTE_KEY)
            preferences.remove(NEGOCIO_ID_KEY)
            preferences.remove(DNI_PENDIENTE_KEY)
            preferences.remove(NOMBRE_NEGOCIO_KEY)
            preferences.remove(LOGO_NEGOCIO_KEY)
        }
    }

    /**
     * Limpia datos personales tras eliminar la cuenta. Conserva tema e idioma
     * porque son preferencias del dispositivo, no de la cuenta.
     */
    suspend fun limpiarDatosPersonales() {
        dataStore.edit { preferences ->
            preferences.remove(ID_CLIENTE_KEY)
            preferences.remove(NEGOCIO_ID_KEY)
            preferences.remove(DNI_PENDIENTE_KEY)
            preferences.remove(NOMBRE_NEGOCIO_KEY)
            preferences.remove(LOGO_NEGOCIO_KEY)
            preferences.remove(NOTIFICACIONES_ACTIVADAS_KEY)
            preferences.remove(TERMINOS_UID_KEY)
            preferences.remove(TERMINOS_VERSION_KEY)
            preferences.remove(TERMINOS_FECHA_KEY)
        }
    }
}
