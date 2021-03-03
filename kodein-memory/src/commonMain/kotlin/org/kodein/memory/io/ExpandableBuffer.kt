package org.kodein.memory.io


public interface ExpandableBuffer : ReadAllocation, ResettableWriteable {
    /**
     * Checks that the needed bytes are available after position.
     * If not, expands the buffer.
     * Attention: When expanding the buffer, only the content BEFORE position is copied.
     * This means that using this just after reset is guaranteed without copy.
     */
    override fun requireCanWrite(needed: Int)

    public companion object {
        public operator fun invoke(initialCapacity: Int, alloc: (Int) -> Allocation): ExpandableBuffer = ExpandableBufferImpl(initialCapacity, alloc)

        public fun native(initialCapacity: Int): ExpandableBuffer = ExpandableBuffer(initialCapacity) { Allocation.native(it) }
        public fun array(initialCapacity: Int): ExpandableBuffer = ExpandableBuffer(initialCapacity) { KBuffer.array(it).asManagedAllocation() }
    }
}


/**
 * Attention: the [sliceHere] function DOES expand the buffer, but the [slice] function DOES NOT
 *   (because it is absolute and does not depend on position).
 */
internal class ExpandableBufferImpl(private val initialCapacity: Int, private val alloc: (Int) -> Allocation) : AbstractReadAllocation<KBuffer>(), ExpandableBuffer {

    override var buffer = alloc(initialCapacity)

    private var writeMode = true

    override fun closeOnce() {
        buffer.close()
    }

    override fun requireCanWrite(needed: Int) {
        require(writeMode) { "Buffer is in read mode because you called flip(). Use reset() or resetHere() to put it back to read-write mode." }

        if (needed <= buffer.remaining) return

        val factor =
            if (needed < (initialCapacity / 2)) 1
            else ((needed / initialCapacity) + 2)

        val previousBuffer = buffer
        buffer = alloc(previousBuffer.capacity + factor * initialCapacity)

        try {
            if (previousBuffer.position > 0) {
                buffer.putMemoryBytes(previousBuffer, length = previousBuffer.position)
            }
        } finally {
            previousBuffer.close()
        }
    }

    override fun putByte(value: Byte) {
        requireCanWrite(Byte.SIZE_BYTES)
        buffer.putByte(value)
    }

    override fun putChar(value: Char) {
        requireCanWrite(Char.SIZE_BYTES)
        buffer.putChar(value)
    }

    override fun putShort(value: Short) {
        requireCanWrite(Short.SIZE_BYTES)
        buffer.putShort(value)
    }

    override fun putInt(value: Int) {
        requireCanWrite(Int.SIZE_BYTES)
        buffer.putInt(value)
    }

    override fun putLong(value: Long) {
        requireCanWrite(Long.SIZE_BYTES)
        buffer.putLong(value)
    }

    override fun putFloat(value: Float) {
        requireCanWrite(Int.SIZE_BYTES)
        buffer.putFloat(value)
    }

    override fun putDouble(value: Double) {
        requireCanWrite(Long.SIZE_BYTES)
        buffer.putDouble(value)
    }

    override fun putBytes(src: ByteArray, srcOffset: Int, length: Int) {
        requireCanWrite(length)
        buffer.putBytes(src, srcOffset, length)
    }

    override fun putMemoryBytes(src: ReadMemory, srcOffset: Int, length: Int) {
        requireCanWrite(length)
        buffer.putMemoryBytes(src, srcOffset, length)
    }

    override fun putReadableBytes(src: Readable, length: Int) {
        requireCanWrite(length)
        buffer.putReadableBytes(src, length)
    }

    override fun flush() = buffer.flush()

    override fun sliceHere(length: Int): ReadBuffer {
        requireCanWrite(length)
        return buffer.sliceHere(length)
    }

    override fun reset() {
        writeMode = true
        buffer.reset()
    }

    override fun resetHere() {
        writeMode = true
        buffer.resetHere()
    }

    override fun flip() {
        writeMode = false
        buffer.flip()
    }

    override fun duplicate(): ReadBuffer {
        error("Cannot duplicate an Expandable ReadBuffer")
    }
}
