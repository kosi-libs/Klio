package org.kodein.memory.io

import org.kodein.memory.Closeable

public class SliceBuilder(private val initialCapacity: Int, private val alloc: (Int) -> Allocation) : Closeable {

    private val allocs = ArrayList<Allocation>()

    private var current: Allocation = alloc(initialCapacity).also { allocs += it }
    private var currentPosition: Int = 0

    private var startPosition: Int = 0

    private var hasSubSlice = false

    public var copies: Int = 0
        private set

    private fun requireSize(size: Int) {
        if (size <= current.size - currentPosition) return

        val neededSize = currentPosition - startPosition + size
        val factor =
            if (neededSize < (initialCapacity / 2)) 1
            else ((neededSize / initialCapacity) + 2)

        val previousAllocation = current
        val isOneSlice = startPosition == 0 && !hasSubSlice
        if (isOneSlice) {
            allocs.removeAt(allocs.lastIndex)
        }
        current = alloc(factor * initialCapacity).also { allocs += it }
        val copyLength = currentPosition - startPosition
        current.putBytes(0, previousAllocation.slice(startPosition, copyLength))
        startPosition = 0
        currentPosition = copyLength
        hasSubSlice = false
        ++copies
        if (isOneSlice) {
            previousAllocation.close()
        }
    }

    public inner class BuilderWriteable internal constructor(): Writeable {

        override val position: Int get() = currentPosition - startPosition

        override fun requestCanWrite(needed: Int) {
            requireSize(needed)
        }

        private inline fun <T> writeValue(size: Int, value: T, putValue: Memory.(Int, T) -> Unit) {
            requireSize(size)
            current.putValue(currentPosition, value)
            currentPosition += size
        }

        override fun writeByte(value: Byte): Unit = writeValue(1, value, Memory::putByte)
        override fun writeShort(value: Short): Unit = writeValue(2, value, Memory::putShort)
        override fun writeInt(value: Int): Unit = writeValue(4, value, Memory::putInt)
        override fun writeLong(value: Long): Unit = writeValue(8, value, Memory::putLong)

        override fun writeBytes(src: ByteArray, srcOffset: Int, length: Int) {
            requireSize(length)
            current.putBytes(currentPosition, src, srcOffset, length)
            currentPosition += length
        }

        override fun writeBytes(src: ReadMemory) {
            requireSize(src.size)
            current.putBytes(currentPosition, src)
            currentPosition += src.size
        }

        override fun writeBytes(src: Readable, length: Int) {
            requireSize(length)
            current.putBytes(currentPosition, src, length)
            currentPosition += length
        }

        override fun flush() {}

        public fun subSlice(block: () -> Unit) : ReadMemory {
            val startOffset = position
            block()
            hasSubSlice = true
            val newStart = startPosition + startOffset
            return current.slice(newStart, currentPosition - newStart)
        }
    }

    private val writeable = BuilderWriteable()

    public fun slice(block: BuilderWriteable.() -> Unit): ReadMemory {
        startPosition = currentPosition
        writeable.block()
        return current.slice(startPosition, currentPosition - startPosition)
    }

    public val allocationCount: Int get() = allocs.size

    public val allocationSize: Int get() = allocs.sumBy { it.size }

    override fun close() {
        allocs.forEach { it.close() }
    }

    public companion object {
        public fun native(initialCapacity: Int): SliceBuilder = SliceBuilder(initialCapacity) { Allocation.native(it) }
        public fun array(initialCapacity: Int): SliceBuilder = SliceBuilder(initialCapacity) { Memory.array(it).asManagedAllocation() }
    }
}
