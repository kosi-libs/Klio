package org.kodein.memory

public expect interface Closeable {
    public fun close()
}

public fun Closeable(onClose: () -> Unit): Closeable = object : Closeable {
    override fun close() = onClose()
}

public inline fun <C : Closeable, R> C.use(block: (C) -> R): R {
    var closed = false

    return try {
        block(this)
    } catch (first: Throwable) {
        try {
            closed = true
            close()
        } catch (second: Throwable) {
            first.addSuppressed(second)
        }

        throw first
    } finally {
        if (!closed) {
            close()
        }
    }
}

public inline fun <C : Closeable, R> C.transfer(block: (C) -> R): R {
    return try {
        block(this)
    } catch (first: Throwable) {
        try {
            close()
        } catch (second: Throwable) {
            first.addSuppressed(second)
        }

        throw first
    }
}

public fun Iterable<Closeable>.closeAll() {
    var exception: Throwable? = null
    forEach {
        try {
            it.close()
        } catch (thrown: Throwable) {
            if (exception == null) exception = thrown
            else exception!!.addSuppressed(thrown)
        }
    }
    if (exception != null)
        throw exception!!
}

public inline fun <R> Iterable<Closeable>.useAll(block: (Iterable<Closeable>) -> R): R =
    try {
        block(this)
    } finally {
        closeAll()
    }

public inline fun <C : Closeable?, R> C.useOrNull(block: (C?) -> R): R =
    if (this != null) use(block)
    else block(null)


//public class UseScope : Closeable {
//    private
//}
