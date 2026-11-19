package com.roberto.trazys.cliente.data.datastore

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.roberto.trazys.cliente.util.TerminosDeUso

/** Nombre del fichero DataStore del cliente (Preferences). */
const val DATASTORE_FILE = "preferencias_cliente.preferences_pb"

internal val THEME_MODE_KEY = stringPreferencesKey("theme_mode")
internal val IDIOMA_KEY = stringPreferencesKey("idioma")
internal val ID_CLIENTE_KEY = intPreferencesKey("id_cliente")
internal val NEGOCIO_ID_KEY = stringPreferencesKey("negocio_id")
internal val DNI_PENDIENTE_KEY = stringPreferencesKey("dni_pendiente")
internal val NOMBRE_NEGOCIO_KEY = stringPreferencesKey("nombre_negocio")
internal val LOGO_NEGOCIO_KEY = stringPreferencesKey("logo_negocio")
internal val NOTIFICACIONES_ACTIVADAS_KEY = booleanPreferencesKey("notificaciones_activadas")

internal val TERMINOS_UID_KEY = stringPreferencesKey(TerminosDeUso.CLAVE_TERMINOS_UID)
internal val TERMINOS_VERSION_KEY = stringPreferencesKey(TerminosDeUso.CLAVE_TERMINOS_VERSION)
internal val TERMINOS_FECHA_KEY = stringPreferencesKey(TerminosDeUso.CLAVE_TERMINOS_FECHA)
