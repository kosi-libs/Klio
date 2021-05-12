package org.kodein.memory.io

import org.kodein.memory.Closeable

public interface ReadAllocation : ReadMemory, Closeable {
    public val memory: ReadMemory
    public val isClosed: Boolean
    public companion object
}

public interface Allocation : Memory, ReadAllocation {
    override val memory: Memory
    public companion object
}

public open class ReadMemoryAllocation<M: ReadMemory>(protected val backingMemory: M, internal val closeFun: () -> Unit) : ReadAllocation {

    private var _isClosed = false
    override val isClosed: Boolean get() = _isClosed

    protected inline fun <T> delegate(block: M.() -> T): T {
        check(!isClosed) { "Allocation is closed" }
        return backingMemory.block()
    }

    final override val memory: M get() = delegate { backingMemory }

    final override val size: Int get() = delegate { size }

    override fun slice(index: Int, length: Int): ReadMemory = delegate { slice(index, length) }

    final override fun getByte(index: Int): Byte = delegate { getByte(index) }
    final override fun getShort(index: Int): Short = delegate { getShort(index) }
    final override fun getInt(index: Int): Int = delegate { getInt(index) }
    final override fun getLong(index: Int): Long = delegate { getLong(index) }
    final override fun getBytes(index: Int, dst: ByteArray, dstOffset: Int, length: Int): Unit = delegate { getBytes(index, dst, dstOffset, length) }

    override fun internalMemory(): ReadMemory = delegate { internalMemory() }

    final override fun close() {
        if (!_isClosed) {
            _isClosed = true
            closeFun()
        }
    }

    final override fun equals(other: Any?): Boolean = delegate { equals(other) }
    final override fun hashCode(): Int = delegate { hashCode() }
}

public fun <M : ReadMemory> M.asManagedAllocation(): ReadMemoryAllocation<M> = ReadMemoryAllocation(this) {}

public class MemoryAllocation<M : Memory>(memory: M, closeFun: () -> Unit): ReadMemoryAllocation<M>(memory, closeFun), Allocation {

    override fun slice(index: Int, length: Int): Memory = delegate { slice(index, length) }

    override fun putByte(index: Int, value: Byte): Unit = delegate { putByte(index, value) }
    override fun putShort(index: Int, value: Short): Unit = delegate { putShort(index, value) }
    override fun putInt(index: Int, value: Int): Unit = delegate { putInt(index, value) }
    override fun putLong(index: Int, value: Long): Unit = delegate { putLong(index, value) }
    override fun putBytes(index: Int, src: ByteArray, srcOffset: Int, length: Int): Unit = delegate { putBytes(index, src, srcOffset, length) }
    override fun putBytes(index: Int, src: ReadMemory): Unit = delegate { putBytes(index, src) }
    override fun putBytes(index: Int, src: Readable, length: Int): Unit = delegate { putBytes(index, src, length) }
    override fun fill(byte: Byte): Unit = delegate { fill(byte) }

    override fun internalMemory(): Memory = delegate { internalMemory() }
}

public fun Memory.asManagedAllocation(): Allocation = MemoryAllocation(this) {}

public expect class PlatformNativeMemory : AbstractMemory<PlatformNativeMemory>
public typealias PlatformNativeAllocation = MemoryAllocation<PlatformNativeMemory>

public expect fun Allocation.Companion.native(size: Int): PlatformNativeAllocation

@PublishedApi
internal fun PlatformNativeAllocation.reduced(reducedSize: Int): PlatformNativeAllocation =
    PlatformNativeAllocation(memory.slice(0, reducedSize), closeFun)

public inline fun Allocation.Companion.native(size: Int, write: Writeable.() -> Unit): PlatformNativeAllocation {
    val alloc = Allocation.native(size)
    try {
        val length = alloc.write { write() }
        return if (size == length) alloc else alloc.reduced(length)
    } catch (t: Throwable) {
        alloc.close()
        throw t
    }
}

public fun Allocation.Companion.nativeCopy(src: ReadMemory): PlatformNativeAllocation =
    native(src.size) { writeBytes(src) }

public fun Allocation.Companion.nativeCopy(src: Readable, length: Int): PlatformNativeAllocation =
    native(length) { writeBytes(src, length) }

public fun Allocation.Companion.nativeCopy(src: CursorReadable): PlatformNativeAllocation =
    native(src.remaining) { writeBytes(src) }
