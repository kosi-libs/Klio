package org.kodein.memory.util

private val shadows = WeakIdentityMap<Throwable, MutableList<Throwable>>()

public actual fun Throwable.addShadowed(other: Throwable) {
    shadows.getOrSet(this) { ArrayList() } .add(other)
}

public actual fun Throwable.getShadowed(): List<Throwable> = shadows[this] ?: emptyList()
