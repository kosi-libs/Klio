package org.kodein.memory.util

private val shadows = WeakIdentityMap<Throwable, MutableList<Throwable>>()

actual fun Throwable.addShadowed(other: Throwable) {
    shadows.getOrSet(this) { ArrayList() } .add(other)
}

actual fun Throwable.getShadowed(): List<Throwable> = shadows[this] ?: emptyList()
