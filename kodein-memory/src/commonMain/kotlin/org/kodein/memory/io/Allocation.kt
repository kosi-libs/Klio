package org.kodein.memory.io

import org.kodein.memory.Closeable

interface ReadAllocation : ReadBuffer, Closeable

interface Allocation : KBuffer, ReadAllocation, Closeable {
    companion object Allocations
}

internal abstract class AbstractReadAllocation<B: ReadBuffer>(protected val buffer: B): ReadAllocation {

    protected var isClosed: Boolean = false ; private set

    protected inline fun <T> delegate(block: B.() -> T): T {
        check(!isClosed) { "Allocation is closed" }
        return buffer.block()
    }

    final override val remaining: Int get() = delegate { remaining }
    override val limit: Int
        get() = delegate { limit }
    final override var position: Int
        get() = delegate { position }
        set(value) { delegate { position = value } }

    override fun duplicate() = delegate { duplicate() }
    override fun slice() = delegate { slice() }
    override fun slice(index: Int, length: Int) = delegate { slice(index, length) }
    override fun internalBuffer() = delegate { internalBuffer() }

    final override fun get(index: Int) = delegate { get(index) }
    final override fun getChar(index: Int) = delegate { getChar(index) }
    final override fun getShort(index: Int) = delegate { getShort(index) }
    final override fun getInt(index: Int) = delegate { getInt(index) }
    final override fun getLong(index: Int) = delegate { getLong(index) }
    final override fun getFloat(index: Int) = delegate { getFloat(index) }
    final override fun getDouble(index: Int) = delegate { getDouble(index) }
    final override fun getBytes(index: Int, dst: ByteArray, dstOffset: Int, length: Int) = delegate { getBytes(index, dst, dstOffset, length) }
    final override fun peek() = delegate { peek() }
    final override fun read() = delegate { read() }
    final override fun readChar() = delegate { readChar() }
    final override fun readShort() = delegate { readShort() }
    final override fun readInt() = delegate { readInt() }
    final override fun readLong() = delegate { readLong() }
    final override fun readFloat() = delegate { readFloat() }
    final override fun readDouble() = delegate { readDouble() }
    final override fun readBytes(dst: ByteArray, dstOffset: Int, length: Int) = delegate { readBytes(dst, dstOffset, length) }
    final override fun skip(count: Int) = delegate { skip(count) }

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

internal abstract class AbstractAllocation(buffer: KBuffer): AbstractReadAllocation<KBuffer>(buffer), Allocation {

    final override val capacity: Int get() = delegate { capacity }
    final override var limit: Int
        get() = delegate { limit }
        set(value) { delegate { limit = value } }

    final override val offset: Int get() = delegate { offset }

    override fun offset(newOffset: Int) = delegate { offset(newOffset) }
    final override fun duplicate() = delegate { duplicate() }
    final override fun slice() = delegate { slice() }
    final override fun slice(index: Int, length: Int) = delegate { slice(index, length) }
    final override fun internalBuffer() = delegate { internalBuffer() }

    final override fun reset() = delegate { reset() }
    final override fun flip() = delegate { flip() }
    final override fun set(index: Int, value: Byte) = delegate { set(index, value) }
    final override fun setChar(index: Int, value: Char) = delegate { setChar(index, value) }
    final override fun setShort(index: Int, value: Short) = delegate { setShort(index, value) }
    final override fun setInt(index: Int, value: Int) = delegate { setInt(index, value) }
    final override fun setLong(index: Int, value: Long) = delegate { setLong(index, value) }
    final override fun setFloat(index: Int, value: Float) = delegate { setFloat(index, value) }
    final override fun setDouble(index: Int, value: Double) = delegate { setDouble(index, value) }
    final override fun setBytes(index: Int, src: ByteArray, srcOffset: Int, length: Int) = delegate { setBytes(index, src, srcOffset, length) }
    final override fun setBytes(index: Int, src: ReadMemory, srcOffset: Int, length: Int) = delegate { setBytes(index, src, srcOffset, length) }
    final override fun put(value: Byte) = delegate { put(value) }
    final override fun putChar(value: Char) = delegate { putChar(value) }
    final override fun putShort(value: Short) = delegate { putShort(value) }
    final override fun putInt(value: Int) = delegate { putInt(value) }
    final override fun putLong(value: Long) = delegate { putLong(value) }
    final override fun putFloat(value: Float) = delegate { putFloat(value) }
    final override fun putDouble(value: Double) = delegate { putDouble(value) }
    final override fun putBytes(src: ByteArray, srcOffset: Int, length: Int) = delegate { putBytes(src, srcOffset, length) }
    final override fun putBytes(src: Readable, length: Int) = delegate { putBytes(src, length) }
    final override fun backingArray() = delegate { backingArray() }
}

internal class NativeAllocation(buffer: KBuffer, private val closeFun: () -> Unit) : AbstractAllocation(buffer) {
    override fun closeOnce() { closeFun() }
}

internal class ManagedAllocation(buffer: KBuffer) : AbstractAllocation(buffer) {
    override fun closeOnce() {}
}

internal class ManagedReadAllocation(buffer: ReadBuffer) : AbstractReadAllocation<ReadBuffer>(buffer) {
    override fun closeOnce() {}
}

expect fun Allocation.Allocations.native(capacity: Int): Allocation

fun Allocation.Allocations.nativeCopy(src: ReadMemory, srcOffset: Int = 0, length: Int = src.limit - srcOffset) = native(length).apply { setBytes(0, src, srcOffset, length) }

fun KBuffer.asManagedAllocation(): Allocation = ManagedAllocation(this)

fun ReadBuffer.asManagedReadAllocation(): ReadAllocation = ManagedReadAllocation(this)

inline fun Allocation.Allocations.native(capacity: Int, block: KBuffer.() -> Unit): Allocation {
    val alloc = Allocation.native(capacity)
    alloc.block()
    alloc.flip()
    return alloc
}
