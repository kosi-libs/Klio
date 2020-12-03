package org.kodein.memory.util


public inline fun <T> Iterable<T>.forEachCatchTo(catchTo: MaybeThrowable, action: (T) -> Unit) {
    for (element in this) {
        try {
            action(element)
        } catch (ex: Throwable) {
            catchTo.add(ex)
        }
    }
}

public inline fun <T> Iterable<T>.forEachCatch(action: (T) -> Unit): Throwable? = MaybeThrowable().also { forEachCatchTo(it, action) }.throwable

public inline fun <T> Iterable<T>.forEachResilient(action: (T) -> Unit): Unit = MaybeThrowable().also { forEachCatchTo(it, action) }.shoot()

public class MaybeThrowable {
    public var throwable: Throwable? = null
    private set

    public fun add(ex: Throwable?) {
        if (ex == null)
            return

        throwable?.addSuppressed(ex) ?: run { throwable = ex }
    }

    public fun shoot() { throwable?.let { throw it } }
}