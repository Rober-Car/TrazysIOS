package com.roberto.trazys.cliente.data.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences

/**
 * Crea el DataStore de Preferences del cliente.
 *
 * Cada plataforma decide la ruta:
 * - JVM: directorio temporal (target usado solo para pruebas).
 * - iOS: Application Support, dentro de la carpeta datastore.
 */
expect fun obtenerDataStore(): DataStore<Preferences>
