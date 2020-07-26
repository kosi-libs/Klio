package org.kodein.memory.util

public expect class WeakIdentityMap<in K : Any, V : Any>() {

    public operator fun get(key: K): V?

    public operator fun set(key: K, value: V)

    public fun delete(key: K): Boolean

    public fun has(key: K): Boolean

}

public operator fun <K : Any> WeakIdentityMap<K, *>.minusAssign(key: K) { delete(key) }
public operator fun <K : Any> WeakIdentityMap<K, *>.contains(key: K): Boolean = has(key)

public expect fun WeakIdentityMap<*, *>.cleanUp()

public expect fun <K : Any, V : Any> WeakIdentityMap<K, V>.getOrSet(key: K, creator: () -> V): V
