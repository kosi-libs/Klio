package org.kodein.memory.io

import kotlinx.atomicfu.atomic
import org.kodein.memory.Closeable

interface Allocation : KBuffer, Closeable {
    companion object Allocations
}

abstract class AbstractAllocation(protected val buffer: KBuffer): Allocation {

    protected var isClosed: Boolean = false ; private set

    protected inline fun <T> delegate(block: KBuffer.() -> T): T {
        check(!isClosed) { "Allocation is closed" }
        return buffer.block()
    }

    override val capacity: Int get() = delegate { capacity }
    override val remaining: Int get() = delegate { remaining }
    override var limit: Int
        get() = delegate { limit }
        set(value) { delegate { limit = value } }
    override var position: Int
        get() = delegate { position }
        set(value) { delegate { position = value } }

    override fun clear() = delegate { clear() }
    override fun flip() = delegate { flip() }
    override fun rewind() = delegate { rewind() }
    override fun duplicate() = delegate { duplicate() }
    override fun slice() = delegate { slice() }
    override fun slice(index: Int, length: Int) = delegate { slice(index, length) }
    override fun internalBuffer() = delegate { internalBuffer() }
    override fun set(index: Int, value: Byte) = delegate { set(index, value) }
    override fun setChar(index: Int, value: Char) = delegate { setChar(index, value) }
    override fun setShort(index: Int, value: Short) = delegate { setShort(index, value) }
    override fun setInt(index: Int, value: Int) = delegate { setInt(index, value) }
    override fun setLong(index: Int, value: Long) = delegate { setLong(index, value) }
    override fun setFloat(index: Int, value: Float) = delegate { setFloat(index, value) }
    override fun setDouble(index: Int, value: Double) = delegate { setDouble(index, value) }
    override fun setBytes(index: Int, src: ByteArray, srcOffset: Int, length: Int) = delegate { setBytes(index, src, srcOffset, length) }
    override fun setBytes(index: Int, src: ReadBuffer, srcOffset: Int, length: Int) = delegate { setBytes(index, src, srcOffset, length) }
    override fun put(value: Byte) = delegate { put(value) }
    override fun putChar(value: Char) = delegate { putChar(value) }
    override fun putShort(value: Short) = delegate { putShort(value) }
    override fun putInt(value: Int) = delegate { putInt(value) }
    override fun putLong(value: Long) = delegate { putLong(value) }
    override fun putFloat(value: Float) = delegate { putFloat(value) }
    override fun putDouble(value: Double) = delegate { putDouble(value) }
    override fun putBytes(src: ByteArray, srcOffset: Int, length: Int) = delegate { putBytes(src, srcOffset, length) }
    override fun putBytes(src: Readable, length: Int) = delegate { putBytes(src, length) }
    override fun get(index: Int) = delegate { get(index) }
    override fun getChar(index: Int) = delegate { getChar(index) }
    override fun getShort(index: Int) = delegate { getShort(index) }
    override fun getInt(index: Int) = delegate { getInt(index) }
    override fun getLong(index: Int) = delegate { getLong(index) }
    override fun getFloat(index: Int) = delegate { getFloat(index) }
    override fun getDouble(index: Int) = delegate { getDouble(index) }
    override fun getBytes(index: Int, dst: ByteArray, offset: Int, length: Int) = delegate { getBytes(index, dst, offset, length) }
    override fun peek() = delegate { peek() }
    override fun read() = delegate { read() }
    override fun readChar() = delegate { readChar() }
    override fun readShort() = delegate { readShort() }
    override fun readInt() = delegate { readInt() }
    override fun readLong() = delegate { readLong() }
    override fun readFloat() = delegate { readFloat() }
    override fun readDouble() = delegate { readDouble() }
    override fun readBytes(dst: ByteArray, offset: Int, length: Int) = delegate { readBytes(dst, offset, length) }
    override fun skip(count: Int) = delegate { skip(count) }

    protected abstract fun closeOnce()
    final override fun close() {
        if (!isClosed) {
            isClosed = true
            closeOnce()
        }
    }

    override fun equals(other: Any?) = delegate { equals(other) }
    override fun hashCode() = delegate { hashCode() }

}

internal class NativeAllocation(buffer: KBuffer, private val closeFun: () -> Unit) : AbstractAllocation(buffer) {
    override fun closeOnce() { closeFun() }
}

internal class ManagedAllocation(buffer: KBuffer) : AbstractAllocation(buffer) {
    override fun closeOnce() {}
}

expect fun Allocation.Allocations.native(capacity: Int): Allocation

fun Allocation.Allocations.nativeCopy(buffer: ReadBuffer) = native(buffer.remaining) { putBytes(buffer.duplicate()) }

fun KBuffer.asManagedAllocation(): Allocation = ManagedAllocation(this)

inline fun Allocation.Allocations.native(capacity: Int, block: KBuffer.() -> Unit): Allocation {
    val alloc = Allocation.native(capacity)
    alloc.block()
    alloc.flip()
    return alloc
}

class ArcAllocation(val delegate: Allocation) : Allocation by delegate {
    private val rc = atomic(1)

    fun attach(): Allocation = apply { rc.incrementAndGet() }

    override fun close() {
        if (rc.decrementAndGet() == 0)
            delegate.close()
    }
}

fun Allocation.arc() = ArcAllocation(this)
