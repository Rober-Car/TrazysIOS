package com.roberto.trazys.cliente.time

import platform.Foundation.NSDate
import platform.Foundation.timeIntervalSince1970

/**
 * NSDate → milisegundos desde epoch, la misma unidad que
 * System.currentTimeMillis en Android/JVM.
 */
actual fun horaActualMilis(): Long =
    (NSDate().timeIntervalSince1970 * 1000.0).toLong()
