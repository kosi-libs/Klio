package org.kodein.memory.util

@JsName("WeakMap")
public actual external class WeakIdentityMap<in K : Any, V : Any> actual constructor() {

    public actual operator fun get(key: K): V?

    public actual operator fun set(key: K, value: V)

    public actual fun delete(key: K): Boolean

    public actual fun has(key: K): Boolean

}

public actual fun WeakIdentityMap<*, *>.cleanUp() {}
