package org.kodein.memory.cache

interface ObjectCacheBase<K : Any, V  : Any> {

    fun getEntry(key: K): ObjectCache.Entry<V>

    operator fun get(key: K): V? = getEntry(key).value

    fun getOrRetrieveEntry(key: K, retrieve: () -> Sized<V>?): ObjectCache.Entry<V>

    fun getOrRetrieve(key: K, retrieve: () -> Sized<V>?): V? = getOrRetrieveEntry(key, retrieve).value

    fun put(key: K, value: V, size: Int)

    fun put(key: K, sized: Sized<V>) = put(key, sized.value, sized.size)

    fun delete(key: K): ObjectCache.Entry<V>

    fun evict(key: K): ObjectCache.Entry<V>

    fun clean()

    fun batch(block: ObjectCacheBase<K, V>.() -> Unit)


}
