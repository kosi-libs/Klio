package org.kodein.memory.util

import java.lang.ref.WeakReference
import java.util.concurrent.ConcurrentHashMap


actual class WeakIdentityMap<in K : Any, V : Any> actual constructor() {

    private interface Identity<T> {
        fun get(): T?
        fun isEqualTo(other: Any?): Boolean {
            if (other !is Identity<*>) return false
            val value = get()
            return value != null && value === other.get()
        }
    }

    private class IdentityWrapper(val wrapped: Any) : Identity<Any> {
        private val hashCode = System.identityHashCode(wrapped)
        override fun get() = wrapped
        override fun hashCode(): Int = hashCode
        override fun equals(other: Any?) = isEqualTo(other)
    }

    private class IdentityReference<T : Any>(referred: T) : WeakReference<T>(referred), Identity<T> {
        private val hashCode = System.identityHashCode(referred)
        override fun hashCode() = hashCode
        override fun equals(other: Any?) = isEqualTo(other)
    }

    private val map = ConcurrentHashMap<Any, V>()

    internal fun cleanUp() {
        map.keys.removeAll { (it as IdentityReference<*>).get() == null }
    }

    internal fun getOrSet(key: K, creator: () -> V): V =
            map[IdentityWrapper(key)] ?: creator().let { map.putIfAbsent(IdentityReference(key), it) ?: it }

    actual operator fun get(key: K): V? {
        cleanUp()
        return map[IdentityWrapper(key)]
    }

    actual operator fun set(key: K, value: V) {
        cleanUp()
        map[IdentityReference(key)] = value
    }

    actual fun delete(key: K): Boolean {
        val prev = map.remove(IdentityWrapper(key))
        cleanUp()
        return prev != null
    }

    actual fun has(key: K): Boolean {
        cleanUp()
        return map.containsKey(IdentityWrapper(key))
    }

}

actual fun WeakIdentityMap<*, *>.cleanUp() = cleanUp()

actual fun <K : Any, V : Any> WeakIdentityMap<K, V>.getOrSet(key: K, creator: () -> V): V = getOrSet(key, creator)
