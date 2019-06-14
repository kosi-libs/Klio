package org.kodein.memory.model

import org.kodein.memory.concurent.AtomicInteger
import org.kodein.memory.concurent.RWLock
import org.kodein.memory.concurent.read
import org.kodein.memory.concurent.write
import kotlin.jvm.Volatile

internal class ObjectCacheImpl<K : Any, V : Any> private constructor(private var internals: Internals<K, V>, private val instanceMaxSize: Int) : ObjectCache<K, V> {

    constructor(maxSize: Int) : this(Internals(maxSize), maxSize)

    @Volatile
    private var internalsVersion = 0

    private class Internals<K : Any, V : Any>(var maxSize: Int) {
        init {
            require(maxSize > 0) { "maxSize <= 0" }
        }

        val map = LinkedHashMap<K, ObjectCache.Entry<V>>(0, 0.75f)
        val lock = RWLock()
        var size = 0
        val atomicHitCount = AtomicInteger(0)
        val atomicMissCount = AtomicInteger(0)
        var retrieveCount = 0
        var putCount = 0
        var deleteCount = 0
        var evictionCount = 0

        val refCount = AtomicInteger(1)
    }

    override val size: Int get() = lockRead { internals.size }
    override val maxSize: Int get() = lockRead { internals.maxSize }

    override val hitCount get() = lockRead { internals.atomicHitCount.get() }
    override val missCount get() = lockRead { internals.atomicMissCount.get() }
    override val retrieveCount get() = lockRead { internals.retrieveCount }
    override val putCount get() = lockRead { internals.putCount }
    override val deleteCount get() = lockRead { internals.deleteCount }
    override val evictionCount  get() = lockRead { internals.evictionCount }

    private fun Internals<K, V>.unsafeTrimToSize(maxSize: Int = this.maxSize) {
        if (internals.refCount.get() == 1)
            internals.maxSize = instanceMaxSize

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
            ++evictionCount
        }
    }

    private inline fun <T> lockRead(block: () -> T): T {
        while (true) {
            var trimNeeded = false
            val version = internalsVersion
            internals.lock.read {
                if (internalsVersion == version) {
                    val maxSize = if (internals.refCount.get() == 1) instanceMaxSize else internals.maxSize
                    if (internals.maxSize != maxSize || internals.size > maxSize) {
                        trimNeeded = true
                    }
                    else {
                        return block()
                    }
                }
            }
            if (trimNeeded) {
                lockWrite {
                    internals.unsafeTrimToSize()
                }
                trimNeeded = false
            }
        }
    }

    private inline fun <T> lockWrite(block: () -> T): T {
        while (true) {
            val version = internalsVersion
            internals.lock.write {
                if (internalsVersion == version) {
                    val ret = block()
                    internals.unsafeTrimToSize()
                    return ret
                }
            }
        }
    }

    private fun unsafeGetEntry(key: K) : ObjectCache.Entry<V> {
        val entry = internals.map[key]
        if (entry != null) internals.atomicHitCount.incrementAndGet()
        else internals.atomicMissCount.incrementAndGet()
        @Suppress("UNCHECKED_CAST")
        return entry ?: ObjectCache.Entry.NotInCache as ObjectCache.Entry<V>
    }

    override fun getEntry(key: K): ObjectCache.Entry<V> = lockRead { unsafeGetEntry(key) }

    private fun getOrRetrieveEntry(key: K, retrieve: () -> Sized<V>?, lockWrite: (() -> ObjectCache.Entry<V>) -> ObjectCache.Entry<V>): ObjectCache.Entry<V> {
        val entry1 = lockRead { unsafeGetEntry(key) }
        if (entry1 !is ObjectCache.Entry.NotInCache) return entry1

        return lockWrite {
            val entry2 = unsafeGetEntry(key)
            if (entry2 !is ObjectCache.Entry.NotInCache) {
                entry2
            } else {
                val sized = retrieve()

                @Suppress("UNCHECKED_CAST")
                val newEntry = if (sized != null) ObjectCache.Entry.Cached(sized.value, sized.size) else ObjectCache.Entry.Deleted as ObjectCache.Entry<V>

                internals.map.put(key, newEntry)
                ++internals.retrieveCount

                newEntry
            }
        }
    }

    override fun getOrRetrieveEntry(key: K, retrieve: () -> Sized<V>?): ObjectCache.Entry<V> = getOrRetrieveEntry(key, retrieve, this::lockWrite)

    private fun copyIfNeeded() {
        val count = internals.refCount.get()
        if (count < 1) throw IllegalStateException("refCount < 1")
        if (count == 1) return

        internals.refCount.decrementAndGet()
        val newInternals = Internals<K, V>(instanceMaxSize)
        newInternals.map.putAll(internals.map)
        internals = newInternals
        ++internalsVersion
    }

    private fun unsafePut(key: K, value: V, size: Int) {
        copyIfNeeded()

        val entry = ObjectCache.Entry.Cached(value, size + 8)
        val previous = internals.map.put(key, entry)

        if (previous != null) {
            internals.size -= previous.size
        }

        ++internals.putCount
        internals.size += entry.size
    }

    override fun put(key: K, value: V, size: Int) {
        lockWrite {
            unsafePut(key, value, size)
        }
    }

    private fun unsafeDelete(key: K): ObjectCache.Entry<V> {
        copyIfNeeded()

        @Suppress("UNCHECKED_CAST")
        val previous = internals.map.put(key, ObjectCache.Entry.Deleted as ObjectCache.Entry<V>)

        internals.size += ObjectCache.Entry.Deleted.size

        if (previous != null) {
            ++internals.deleteCount
            internals.size -= previous.size
        }

        @Suppress("UNCHECKED_CAST")
        return previous ?: ObjectCache.Entry.NotInCache as ObjectCache.Entry<V>
    }

    override fun delete(key: K): ObjectCache.Entry<V> {
        lockWrite {
            return unsafeDelete(key)
        }
    }

    private fun unsafeEvict(key: K): ObjectCache.Entry<V> {
        copyIfNeeded()

        @Suppress("UNCHECKED_CAST")
        val previous = internals.map.remove(key)

        if (previous != null) {
            ++internals.evictionCount
            internals.size -= previous.size
        }

        @Suppress("UNCHECKED_CAST")
        return previous ?: ObjectCache.Entry.NotInCache as ObjectCache.Entry<V>
    }

    override fun evict(key: K): ObjectCache.Entry<V> {
        lockWrite {
            return unsafeEvict(key)
        }
    }

    override fun batch(block: ObjectCacheBase<K, V>.() -> Unit) {
        lockWrite {
            var batch: ObjectCacheBase<K, V>? = object : ObjectCacheBase<K, V> {
                override fun getEntry(key: K): ObjectCache.Entry<V> = unsafeGetEntry(key)
                override fun getOrRetrieveEntry(key: K, retrieve: () -> Sized<V>?): ObjectCache.Entry<V> = getOrRetrieveEntry(key, retrieve, ::run)
                override fun put(key: K, value: V, size: Int) = unsafePut(key, value, size)
                override fun delete(key: K): ObjectCache.Entry<V> = unsafeDelete(key)
                override fun evict(key: K): ObjectCache.Entry<V> = unsafeEvict(key)
                override fun batch(block: ObjectCacheBase<K, V>.() -> Unit) = throw IllegalStateException()
            }

            fun getBatch(): ObjectCacheBase<K, V> = batch ?: throw IllegalStateException("Do not use batch this object outside of the batch function")

            val proxy = object : ObjectCacheBase<K, V> {
                override fun getEntry(key: K): ObjectCache.Entry<V> = getBatch().getEntry(key)
                override fun getOrRetrieveEntry(key: K, retrieve: () -> Sized<V>?): ObjectCache.Entry<V> = getBatch().getOrRetrieveEntry(key, retrieve)
                override fun put(key: K, value: V, size: Int) = getBatch().put(key, value, size)
                override fun delete(key: K): ObjectCache.Entry<V> = getBatch().delete(key)
                override fun evict(key: K): ObjectCache.Entry<V> = getBatch().evict(key)
                override fun batch(block: ObjectCacheBase<K, V>.() -> Unit) = this.block()
            }

            proxy.block()

            batch = null
        }
    }

    override fun newCopy(copyMaxSize: Int): ObjectCache<K, V> {
        lockWrite {
            internals.refCount.incrementAndGet()
            return ObjectCacheImpl(internals, copyMaxSize)
        }
    }

    override fun toString(): String {
        lockRead {
            val maxSize = internals.maxSize
            val useRate = if (size != 0) 100 * size / maxSize else 0
            val hits = internals.atomicHitCount.get()
            val misses = internals.atomicMissCount.get()
            val accesses = hits + misses
            val hitRate = if (accesses != 0) 100 * hits / accesses else 0
            return "ObjectCache[maxSize=$maxSize,size=$size,useRate=$useRate,hits=$hits,misses=$misses,hitRate=$hitRate%]"
        }
    }

}
