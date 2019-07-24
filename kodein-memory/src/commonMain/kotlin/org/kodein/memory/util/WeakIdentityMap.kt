package org.kodein.memory.util

expect class WeakIdentityMap<in K : Any, V : Any>() {

    operator fun get(key: K): V?

    operator fun set(key: K, value: V)

    fun delete(key: K): Boolean

    fun has(key: K): Boolean

}

operator fun <K : Any> WeakIdentityMap<K, *>.minusAssign(key: K) { delete(key) }
operator fun <K : Any> WeakIdentityMap<K, *>.contains(key: K) = has(key)

expect fun WeakIdentityMap<*, *>.cleanUp()

expect fun <K : Any, V : Any> WeakIdentityMap<K, V>.getOrSet(key: K, creator: () -> V): V
