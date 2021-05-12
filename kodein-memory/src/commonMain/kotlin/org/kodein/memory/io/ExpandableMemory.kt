package org.kodein.memory.io

import org.kodein.memory.Closeable
import kotlin.math.ceil


public interface ExpandableMemory {
    /**
     * On requireCanWrite: checks that the needed bytes are available after position.
     * If not, expands the buffer.
     * Attention: When expanding the buffer, only the content BEFORE position is copied.
     * This means that calling `requireCanWrite` as first call in `slice` is guaranteed without copy.
     *
     * @return A ReadMemory that is only valid until the next call to `slice`,
     */
    public fun slice(write: CursorWriteable.() -> Unit): ReadMemory

    public val bytesCopied: Int

    public companion object {
        public operator fun invoke(initialCapacity: Int, alloc: (Int) -> Memory): ExpandableMemory = ExpandableMemoryImpl(initialCapacity, alloc)
        public fun array(initialCapacity: Int): ExpandableMemory = ExpandableMemory(initialCapacity, Memory::array)
        public fun array(initialCapacity: Int, write: Writeable.() -> Unit): ReadMemory = ExpandableMemory(initialCapacity, Memory::array).slice(write)
    }
}

public interface ExpandableAllocation : ExpandableMemory, Closeable {
    public companion object {
        public operator fun invoke(initialCapacity: Int, alloc: (Int) -> Allocation): ExpandableAllocation = ExpandableAllocationImpl(initialCapacity, alloc)
        public fun native(initialCapacity: Int): ExpandableAllocation = ExpandableAllocationImpl(initialCapacity, Allocation::native)
        public fun native(initialCapacity: Int, write: Writeable.() -> Unit): ReadAllocation {
            val alloc = ExpandableAllocationImpl(initialCapacity, Allocation::native)
            val memory = alloc.slice(write)
            return ReadMemoryAllocation(memory) { alloc.close() }
        }
    }
}


private open class ExpandableMemoryImpl<M : Memory>(private val initialCapacity: Int, private val alloc: (Int) -> M): ExpandableMemory {

    var memory = alloc(initialCapacity)

    override var bytesCopied = 0

    final override fun slice(write: CursorWriteable.() -> Unit): ReadMemory {
        val w = W()
        w.write()
        return memory.slice(0, w.position)
    }

    open fun close(memory: M) {}

    inner class W : CursorWriteable {
        override var position: Int = 0

        override val remaining: Int = Int.MAX_VALUE

        override fun requestCanWrite(needed: Int) {
            val totalNeeded = position + needed

            if (totalNeeded <= memory.size) return

            val factor = ceil(totalNeeded.toDouble() / initialCapacity.toDouble()).toInt()

            val previousMemory = memory
            memory = alloc(factor * initialCapacity)

            try {
                if (position > 0) {
                    memory.putBytes(0, previousMemory.slice(0, position))
                    bytesCopied += position
                }
            } finally {
                close(previousMemory)
            }
        }

        private inline fun <T> writeValue(size: Int, value: T, putValue: Memory.(Int, T) -> Unit) {
            requestCanWrite(size)
            memory.putValue(position, value)
            position += size
        }
        override fun writeByte(value: Byte) = writeValue(1, value, Memory::putByte)
        override fun writeShort(value: Short) = writeValue(2, value, Memory::putShort)
        override fun writeInt(value: Int) = writeValue(4, value, Memory::putInt)
        override fun writeLong(value: Long) = writeValue(8, value, Memory::putLong)

        override fun writeBytes(src: ByteArray, srcOffset: Int, length: Int) {
            requestCanWrite(length)
            memory.putBytes(position, src, srcOffset, length)
            position += length
        }

        override fun writeBytes(src: ReadMemory) {
            requestCanWrite(src.size)
            memory.putBytes(position, src)
            position += src.size
        }

        override fun writeBytes(src: Readable, length: Int) {
            requestCanWrite(length)
            memory.putBytes(position, src, length)
            position += length
        }

        override fun flush() {}

        override fun skip(count: Int) {
            require(count >= 0) { "count: $count < 0" }
            requestCanWrite(count)
            position += count
        }
    }
}

private class ExpandableAllocationImpl(initialCapacity: Int, alloc: (Int) -> Allocation) : ExpandableMemoryImpl<Allocation>(initialCapacity, alloc), ExpandableAllocation {
    override fun close(memory: Allocation) { memory.close() }
    override fun close() { memory.close() }
}
