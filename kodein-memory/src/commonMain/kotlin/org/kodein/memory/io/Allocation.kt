package org.kodein.memory.io

import org.kodein.memory.Closeable

public interface ReadAllocation : ReadMemory, Closeable

public interface Allocation : Memory, ReadAllocation {
    public companion object
}

internal abstract class AbstractReadAllocation<M: ReadMemory>: ReadAllocation {
    protected abstract val memory: M

    protected var isClosed: Boolean = false ; private set

    protected inline fun <T> delegate(block: M.() -> T): T {
        check(!isClosed) { "Allocation is closed" }
        return memory.block()
    }

    override val size: Int get() = delegate { size }

    override fun slice(index: Int, length: Int) = delegate { slice(index, length) }

    final override fun getByte(index: Int) = delegate { getByte(index) }
    final override fun getShort(index: Int) = delegate { getShort(index) }
    final override fun getInt(index: Int) = delegate { getInt(index) }
    final override fun getLong(index: Int) = delegate { getLong(index) }
    final override fun getBytes(index: Int, dst: ByteArray, dstOffset: Int, length: Int) = delegate { getBytes(index, dst, dstOffset, length) }

    override fun internalMemory() = delegate { internalMemory() }

    protected abstract fun closeOnce()

    final override fun close() {
        if (!isClosed) {
            isClosed = true
            closeOnce()
        }
    }

    final override fun equals(other: Any?) = delegate { equals(other) }
    final override fun hashCode() = delegate { hashCode() }
}

internal class ManagedReadAllocation(override val memory: ReadMemory) : AbstractReadAllocation<ReadMemory>() {
    override fun closeOnce() {}
}

public fun ReadMemory.asManagedReadAllocation(): ReadAllocation = ManagedReadAllocation(this)

internal abstract class AbstractAllocation(override val memory: Memory): AbstractReadAllocation<Memory>(), Allocation {

    override fun slice(index: Int, length: Int) = delegate { slice(index, length) }

    final override fun setByte(index: Int, value: Byte) = delegate { setByte(index, value) }
    final override fun setShort(index: Int, value: Short) = delegate { setShort(index, value) }
    final override fun setInt(index: Int, value: Int) = delegate { setInt(index, value) }
    final override fun setLong(index: Int, value: Long) = delegate { setLong(index, value) }
    final override fun setBytes(index: Int, src: ByteArray, srcOffset: Int, length: Int) = delegate { setBytes(index, src, srcOffset, length) }
    final override fun setBytes(index: Int, src: ReadMemory, srcOffset: Int, length: Int) = delegate { setBytes(index, src, srcOffset, length) }
    final override fun setBytes(index: Int, src: Readable, length: Int) = delegate { setBytes(index, src, length) }
}

internal class ManagedAllocation(memory: Memory) : AbstractAllocation(memory) {
    override fun closeOnce() {}
}

public fun Memory.asManagedAllocation(): Allocation = ManagedAllocation(this)

internal class NativeAllocation(memory: Memory, private val closeFun: () -> Unit) : AbstractAllocation(memory) {
    override fun closeOnce() { closeFun() }
}

public expect fun Allocation.Companion.native(size: Int): Allocation

public fun Allocation.Companion.nativeCopy(src: ReadMemory, srcOffset: Int = 0, length: Int = src.size - srcOffset): Allocation =
    native(length).apply { setBytes(0, src, srcOffset, length) }

public fun Allocation.Companion.nativeCopy(src: Readable, length: Int): Allocation =
    native(length).apply { setBytes(0, src, length) }

public fun Allocation.Companion.nativeCopy(src: CursorReadable): Allocation =
    native(src.remaining).apply { setBytes(0, src) }

private class ReducedAllocation(private val allocation: Allocation, private val reducedSize: Int) : AbstractAllocation(allocation) {
    override val size: Int get() = delegate { reducedSize }
    override fun closeOnce() { allocation.close() }
}

@PublishedApi
internal fun Allocation.reduced(reducedSize: Int): Allocation {
    require(reducedSize < size)
    return ReducedAllocation(this, reducedSize)
}

public inline fun Allocation.Companion.native(size: Int, write: Writeable.() -> Unit): ReadAllocation {
    val alloc = Allocation.native(size)
    val length = alloc.write { write() }
    return if (size == length) alloc else alloc.reduced(length)
}
