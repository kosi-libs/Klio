package org.kodein.memory.model

interface ObjectCache<K : Any, V  : Any> : ObjectCacheBase<K, V> {

    sealed class Entry<V> {
        abstract val value: V?
        abstract val size: Int

        class Cached<M>(override val value: M, override val size: Int) : Entry<M>(), Sized<M>
        object Deleted : Entry<Nothing>() { override val value = null ; override val size: Int = 8 }
        object NotInCache : Entry<Nothing>() { override val value = null ; override val size: Int = 0 }
    }

    val size: Int
    val maxSize: Int

    val hitCount: Int
    val missCount: Int
    val retrieveCount: Int
    val putCount: Int
    val deleteCount: Int
    val evictionCount: Int

    fun newCopy(copyMaxSize: Int): ObjectCache<K, V>

    companion object {
        operator fun <K : Any, V : Any> invoke(maxSize: Int): ObjectCache<K, V> = ObjectCacheImpl(maxSize)
    }
}
