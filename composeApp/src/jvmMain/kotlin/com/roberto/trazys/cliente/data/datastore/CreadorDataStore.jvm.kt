package com.roberto.trazys.cliente.data.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.core.okio.OkioStorage
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.PreferencesSerializer
import okio.FileSystem
import java.io.File

/**
 * Ruta absoluta para OkioStorage en JVM.
 *
 * File.toOkioPath() evita el problema de toPath() con rutas Windows (C:/...),
 * que OkioStorage interpreta como relativas.
 */
actual fun obtenerDataStore(): DataStore<Preferences> =
    DataStoreFactory.create(
        storage = OkioStorage(
            fileSystem = FileSystem.SYSTEM,
            serializer = PreferencesSerializer,
            producePath = {
                val dir = File(System.getProperty("java.io.tmpdir"), "trazys")
                if (!dir.exists()) dir.mkdirs()
                with(okio.Path.Companion) {
                    File(dir, DATASTORE_FILE).toOkioPath()
                }
            }
        )
    )
