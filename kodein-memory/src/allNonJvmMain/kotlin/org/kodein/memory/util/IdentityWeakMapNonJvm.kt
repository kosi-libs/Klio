package org.kodein.memory.util

public actual fun <K : Any, V : Any> WeakIdentityMap<K, V>.getOrSet(key: K, creator: () -> V): V {
    val res = get(key)
    if (res != null)
        return res
    val value = creator()
    set(key, value)
    return value
}
