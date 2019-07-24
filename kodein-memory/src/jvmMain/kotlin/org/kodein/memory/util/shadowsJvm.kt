package org.kodein.memory.util


actual fun Throwable.addShadowed(other: Throwable) = addSuppressed(other)

actual fun Throwable.getShadowed() = suppressed.asList()
