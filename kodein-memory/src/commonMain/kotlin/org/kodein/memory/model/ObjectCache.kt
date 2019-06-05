package org.kodein.memory.model

import org.kodein.memory.concurent.AtomicInteger
import org.kodein.memory.concurent.RWLock
import org.kodein.memory.concurent.read
import org.kodein.memory.concurent.write
import kotlin.jvm.Synchronized

class ObjectCache<K : Any, V : Any>(val maxSize: Int) {

    private val map = LinkedHashMap<K, Entry<V>>(0, 0.75f)

    val lock = RWLock()

    var size: Int = 0
        private set

    private val atomicHitCount = AtomicInteger(0)
    private val atomicMissCount = AtomicInteger(0)

    var putCount = 0
        private set
    var deleteCount = 0
        private set
    var evictionCount = 0
        private set

    sealed class Entry<V> {
        abstract val value: V?
        abstract val size: Int

        class Cached<M>(override val value: M, override val size: Int) : Entry<M>(), Sized<M>
        object Deleted : Entry<Nothing>() { override val value = null ; override val size: Int = 8 }
        object NotInCache : Entry<Nothing>() { override val value = null ; override val size: Int = 0 }
    }

    init {
        require(maxSize > 0) { "maxSize <= 0" }
    }

    fun getEntry(key: K): Entry<V> {
        val model = lock.read { map[key] }
        if (model != null) atomicHitCount.incrementAndGet()
        else atomicMissCount.incrementAndGet()
        @Suppress("UNCHECKED_CAST")
        return model ?: Entry.NotInCache as Entry<V>
    }

    operator fun get(key: K): V? = getEntry(key).value

    fun getOrRetrieveEntry(key: K, retrieve: () -> Sized<V>?): Entry<V> {
        val entry = getEntry(key)
        if (entry !is Entry.NotInCache) {
            return entry
        }

        val sized = retrieve()

        @Suppress("UNCHECKED_CAST")
        val newEntry = if (sized != null) Entry.Cached(sized.value, sized.size) else Entry.Deleted as Entry<V>

        lock.write {
            map.put(key, newEntry)
        }

        return newEntry
    }

    fun getOrRetrieve(key: K, retrieve: () -> Sized<V>?): V? = getOrRetrieveEntry(key, retrieve).value

    private fun trimToSize(maxSize: Int) {
        if (size <= maxSize || map.isEmpty())
            return

        val it = map.entries.iterator()
        while (true) {
            if (size < 0) throw IllegalStateException("Cache size is $size")
            if (size != 0 && !it.hasNext()) throw IllegalStateException("Cache is empty but size is $size")

            if (size <= maxSize || !it.hasNext())
                break

            val toEvict = it.next()
            it.remove()
            size -= toEvict.value.size
            evictionCount++
        }
    }

    fun put(key: K, value: V, size: Int) {
        lock.write {
            val previous = map.put(key, Entry.Cached(value, size))

            if (previous != null) {
                this.size -= previous.size
            }

            ++putCount
            this.size += size

            trimToSize(maxSize)
        }
    }

    fun delete(key: K): Entry<V> {
        lock.write {
            @Suppress("UNCHECKED_CAST")
            val previous = map.put(key, Entry.Deleted as Entry<V>)

            this.size += Entry.Deleted.size

            if (previous != null) {
                ++deleteCount
                this.size -= previous.size
            }

            @Suppress("UNCHECKED_CAST")
            return previous ?: Entry.NotInCache as Entry<V>
        }
    }

    fun remove(key: K): Entry<V> {
        lock.write {
            @Suppress("UNCHECKED_CAST")
            val previous = map.remove(key)

            if (previous != null) {
                ++deleteCount
                this.size -= previous.size
            }

            @Suppress("UNCHECKED_CAST")
            return previous ?: Entry.NotInCache as Entry<V>
        }
    }

    @Synchronized
    fun clear() {
        lock.write {
            trimToSize(-1) // -1 will evict 0-sized elements
            atomicHitCount.set(0)
            atomicMissCount.set(0)
            putCount = 0
            deleteCount = 0
            evictionCount = 0
        }
    }

    val hitCount: Int get() = atomicHitCount.get()

    val missCount: Int get() = atomicMissCount.get()

    @Synchronized
    fun copy(copyMaxSize: Int): ObjectCache<K, V> {
        val cache = ObjectCache<K, V>(copyMaxSize)
        lock.read {
            cache.map.putAll(map)
        }
        return cache
    }

    @Synchronized
    override fun toString(): String {
        val useRate = if (size != 0) 100 * size / maxSize else 0
        val hits = atomicHitCount.get()
        val misses = atomicMissCount.get()
        val accesses = hits + misses
        val hitRate = if (accesses != 0) 100 * hits / accesses else 0
        return "ObjectCache[maxSize=$maxSize,size=$size,useRate=$useRate,hits=$hits,misses=$misses,hitRate=$hitRate%]"
    }

}
