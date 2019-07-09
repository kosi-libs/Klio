package org.kodein.memory

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
            first.addSuppressedInternal(second)
        }

        throw first
    } finally {
        if (!closed) {
            close()
        }
    }
}

@PublishedApi
internal expect fun Throwable.addSuppressedInternal(other: Throwable)
