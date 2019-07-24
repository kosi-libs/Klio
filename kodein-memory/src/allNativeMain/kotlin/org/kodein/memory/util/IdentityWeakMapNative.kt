package org.kodein.memory.util

import kotlin.native.ref.WeakReference

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
        private val hashCode = wrapped.identityHashCode()
        override fun get() = wrapped
        override fun hashCode(): Int = hashCode
        override fun equals(other: Any?) = isEqualTo(other)
    }

    private class IdentityReference<T : Any>(referred: T) : Identity<T> {
        private val weakReference = WeakReference(referred)
        override fun get(): T? = weakReference.get()
        private val hashCode = referred.identityHashCode()
        override fun hashCode() = hashCode
        override fun equals(other: Any?) = isEqualTo(other)
    }

    private val map = HashMap<Any, V>()

    internal fun cleanUp() {
        map.keys.removeAll { (it as IdentityReference<*>).get() == null }
    }

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

@PublishedApi
@SymbolName("Kotlin_Any_hashCode")
external internal fun Any.identityHashCode(): Int
