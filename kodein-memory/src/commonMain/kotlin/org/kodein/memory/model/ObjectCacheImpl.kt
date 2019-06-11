package org.kodein.memory.model

import org.kodein.memory.concurent.AtomicInteger
import org.kodein.memory.concurent.RWLock
import org.kodein.memory.concurent.read
import org.kodein.memory.concurent.write
import kotlin.jvm.Synchronized

internal class ObjectCacheImpl<K : Any, V : Any>(val maxSize: Int) : ObjectCache<K, V> {

    private val map = LinkedHashMap<K, ObjectCache.Entry<V>>(0, 0.75f)

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

    init {
        require(maxSize > 0) { "maxSize <= 0" }
    }

    private fun toEntry(entry: ObjectCache.Entry<V>?) : ObjectCache.Entry<V> {
        if (entry != null) atomicHitCount.incrementAndGet()
        else atomicMissCount.incrementAndGet()
        @Suppress("UNCHECKED_CAST")
        return entry ?: ObjectCache.Entry.NotInCache as ObjectCache.Entry<V>
    }

    override fun getEntry(key: K): ObjectCache.Entry<V> = toEntry(lock.read { map[key] })

    private fun getOrRetrieveEntry(key: K, retrieve: () -> Sized<V>?, lockWrite: (() -> Unit) -> Unit): ObjectCache.Entry<V> {
        val entry = getEntry(key)
        if (entry !is ObjectCache.Entry.NotInCache) {
            return entry
        }

        val sized = retrieve()

        @Suppress("UNCHECKED_CAST")
        val newEntry = if (sized != null) ObjectCache.Entry.Cached(sized.value, sized.size) else ObjectCache.Entry.Deleted as ObjectCache.Entry<V>

        lockWrite {
            map.put(key, newEntry)
        }

        return newEntry
    }

    override fun getOrRetrieveEntry(key: K, retrieve: () -> Sized<V>?): ObjectCache.Entry<V> = getOrRetrieveEntry(key, retrieve, lock::write)

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

    private fun unsafePut(key: K, value: V, size: Int) {
        val previous = map.put(key, ObjectCache.Entry.Cached(value, size))

        if (previous != null) {
            this.size -= previous.size
        }

        ++putCount
        this.size += size

        trimToSize(maxSize)
    }

    override fun put(key: K, value: V, size: Int) {
        lock.write {
            unsafePut(key, value, size)
        }
    }

    private fun unsafeDelete(key: K): ObjectCache.Entry<V> {
        @Suppress("UNCHECKED_CAST")
        val previous = map.put(key, ObjectCache.Entry.Deleted as ObjectCache.Entry<V>)

        this.size += ObjectCache.Entry.Deleted.size

        if (previous != null) {
            ++deleteCount
            this.size -= previous.size
        }

        @Suppress("UNCHECKED_CAST")
        return previous ?: ObjectCache.Entry.NotInCache as ObjectCache.Entry<V>
    }

    override fun delete(key: K): ObjectCache.Entry<V> {
        lock.write {
            return unsafeDelete(key)
        }
    }

    private fun unsafeRemove(key: K): ObjectCache.Entry<V> {
        @Suppress("UNCHECKED_CAST")
        val previous = map.remove(key)

        if (previous != null) {
            ++deleteCount
            this.size -= previous.size
        }

        @Suppress("UNCHECKED_CAST")
        return previous ?: ObjectCache.Entry.NotInCache as ObjectCache.Entry<V>
    }

    override fun remove(key: K): ObjectCache.Entry<V> {
        lock.write {
            return unsafeRemove(key)
        }
    }

    override fun batch(block: ObjectCache<K, V>.() -> Unit) {
        lock.write {
            var batch: ObjectCache<K, V>? = object : ObjectCache<K, V> {
                override fun getEntry(key: K): ObjectCache.Entry<V> = toEntry(map[key])
                override fun getOrRetrieveEntry(key: K, retrieve: () -> Sized<V>?): ObjectCache.Entry<V> = getOrRetrieveEntry(key, retrieve, ::run)
                override fun put(key: K, value: V, size: Int) = unsafePut(key, value, size)
                override fun delete(key: K): ObjectCache.Entry<V> = unsafeDelete(key)
                override fun remove(key: K): ObjectCache.Entry<V> = unsafeRemove(key)
                override fun batch(block: ObjectCache<K, V>.() -> Unit) = throw IllegalStateException()
            }

            fun getBatch(): ObjectCache<K, V> = batch ?: throw IllegalStateException("Do not use batch this object outside of the batch function")

            val proxy = object : ObjectCache<K, V> {
                override fun getEntry(key: K): ObjectCache.Entry<V> = getBatch().getEntry(key)
                override fun getOrRetrieveEntry(key: K, retrieve: () -> Sized<V>?): ObjectCache.Entry<V> = getBatch().getOrRetrieveEntry(key, retrieve)
                override fun put(key: K, value: V, size: Int) = getBatch().put(key, value, size)
                override fun delete(key: K): ObjectCache.Entry<V> = getBatch().delete(key)
                override fun remove(key: K): ObjectCache.Entry<V> = getBatch().remove(key)
                override fun batch(block: ObjectCache<K, V>.() -> Unit) = this.block()
            }

            proxy.block()

            batch = null
        }
    }

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

    fun newCopy(copyMaxSize: Int): ObjectCacheImpl<K, V> {
        val cache = ObjectCacheImpl<K, V>(copyMaxSize)
        lock.read {
            cache.map.putAll(map)
        }
        return cache
    }

    override fun toString(): String {
        val useRate = if (size != 0) 100 * size / maxSize else 0
        val hits = atomicHitCount.get()
        val misses = atomicMissCount.get()
        val accesses = hits + misses
        val hitRate = if (accesses != 0) 100 * hits / accesses else 0
        return "ObjectCache[maxSize=$maxSize,size=$size,useRate=$useRate,hits=$hits,misses=$misses,hitRate=$hitRate%]"
    }

}
