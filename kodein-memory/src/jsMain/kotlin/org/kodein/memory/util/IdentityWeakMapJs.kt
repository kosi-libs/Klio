package org.kodein.memory.util

@JsName("WeakMap")
actual external class WeakIdentityMap<in K : Any, V : Any> actual constructor() {

    actual operator fun get(key: K): V?

    actual operator fun set(key: K, value: V)

    actual fun delete(key: K): Boolean

    actual fun has(key: K): Boolean

}

actual fun WeakIdentityMap<*, *>.cleanUp() {}
