package org.kodein.memory.util


expect fun Throwable.addShadowed(other: Throwable)

expect fun Throwable.getShadowed(): List<Throwable>

inline fun <T> Iterable<T>.forEachCatchTo(catchTo: MaybeThrowable, action: (T) -> Unit) {
    for (element in this) {
        try {
            action(element)
        } catch (ex: Throwable) {
            catchTo.add(ex)
        }
    }
}

inline fun <T> Iterable<T>.forEachCatch(action: (T) -> Unit) = MaybeThrowable().also { forEachCatchTo(it, action) }.throwable

inline fun <T> Iterable<T>.forEachResilient(action: (T) -> Unit) = MaybeThrowable().also { forEachCatchTo(it, action) }.shoot()

class MaybeThrowable {
    var throwable: Throwable? = null
    private set

    fun add(ex: Throwable?) {
        if (ex == null)
            return

        throwable?.addShadowed(ex) ?: run { throwable = ex }
    }

    fun shoot() { throwable?.let { throw it } }
}