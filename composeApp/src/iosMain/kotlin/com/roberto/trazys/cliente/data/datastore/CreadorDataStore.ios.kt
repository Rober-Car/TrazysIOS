@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package com.roberto.trazys.cliente.data.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.core.okio.OkioStorage
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.PreferencesSerializer
import okio.FileSystem
import okio.Path.Companion.toPath
import platform.Foundation.NSApplicationSupportDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSUserDomainMask

/**
 * DataStore en Application Support, no en Documents: son preferencias
 * internas y no deben ser visibles para el usuario desde Files.
 */
actual fun obtenerDataStore(): DataStore<Preferences> =
    DataStoreFactory.create(
        storage = OkioStorage(
            fileSystem = FileSystem.SYSTEM,
            serializer = PreferencesSerializer,
            producePath = {
                val manager = NSFileManager.defaultManager
                val url = manager.URLForDirectory(
                    NSApplicationSupportDirectory,
                    NSUserDomainMask,
                    null,
                    true,
                    null
                )
                val base = requireNotNull(url?.path) {
                    "No se pudo resolver Application Support para DataStore"
                }
                val directorio = "$base/datastore"
                manager.createDirectoryAtPath(directorio, true, null, null)
                "$directorio/$DATASTORE_FILE".toPath()
            }
        )
    )
