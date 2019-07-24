package org.kodein.memory

import org.kodein.memory.util.addShadowed

expect interface Closeable {
    fun close()
}

fun Closeable(onClose: () -> Unit) = object : Closeable {
    override fun close() = onClose()
}

inline fun <C : Closeable, R> C.use(block: (C) -> R): R {
    var closed = false

    return try {
        block(this)
    } catch (first: Throwable) {
        try {
            closed = true
            close()
        } catch (second: Throwable) {
            first.addShadowed(second)
        }

        throw first
    } finally {
        if (!closed) {
            close()
        }
    }
}

