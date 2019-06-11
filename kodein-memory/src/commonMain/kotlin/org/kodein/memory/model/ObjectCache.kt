package org.kodein.memory.model

interface ObjectCache<K : Any, V  : Any> {

    sealed class Entry<V> {
        abstract val value: V?
        abstract val size: Int

        class Cached<M>(override val value: M, override val size: Int) : Entry<M>(), Sized<M>
        object Deleted : Entry<Nothing>() { override val value = null ; override val size: Int = 8 }
        object NotInCache : Entry<Nothing>() { override val value = null ; override val size: Int = 0 }
    }

    fun getEntry(key: K): Entry<V>

    operator fun get(key: K): V? = getEntry(key).value

    fun getOrRetrieveEntry(key: K, retrieve: () -> Sized<V>?): Entry<V>

    fun getOrRetrieve(key: K, retrieve: () -> Sized<V>?): V? = getOrRetrieveEntry(key, retrieve).value

    fun put(key: K, value: V, size: Int)

    fun put(key: K, sized: Sized<V>) = put(key, sized.value, sized.size)

    fun delete(key: K): Entry<V>

    fun remove(key: K): Entry<V>

    fun batch(block: ObjectCache<K, V>.() -> Unit)

    companion object {
        operator fun <K : Any, V : Any> invoke(maxSize: Int): ObjectCache<K, V> = ObjectCacheImpl(maxSize)
    }
}
