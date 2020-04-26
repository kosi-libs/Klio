package org.kodein.memory.util

import java.lang.IllegalArgumentException
import java.lang.IllegalStateException


private val shadows: WeakIdentityMap<Throwable, MutableList<Throwable>>? by lazy {
    try {
        val ex1 = IllegalStateException()
        val ex2 = IllegalArgumentException()
        ex1.addSuppressed(ex2)
        check(ex1.suppressed[0] === ex2)
        null
    } catch (_: Throwable) {
        WeakIdentityMap<Throwable, MutableList<Throwable>>()
    }
}

actual fun Throwable.addShadowed(other: Throwable) {
    if (shadows == null)
        return addSuppressed(other)

    shadows!!.getOrSet(this) { ArrayList() } .add(other)
}

actual fun Throwable.getShadowed(): List<Throwable> {
    if (shadows == null)
        return suppressed.asList()

    return shadows!![this] ?: emptyList()
}
