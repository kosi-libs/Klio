package org.kodein.memory.util


expect fun Throwable.addShadowed(other: Throwable)

expect fun Throwable.getShadowed(): List<Throwable>

inline fun <T> Iterable<T>.forEachResilient(action: (T) -> Unit) {
    var first: Throwable? = null

    for (element in this) {
        try {
            action(element)
        } catch (ex: Throwable) {
            if (first == null) {
                first = ex
            } else {
                first.addShadowed(ex)
            }
        }
    }

    if (first != null)
        throw first
}
