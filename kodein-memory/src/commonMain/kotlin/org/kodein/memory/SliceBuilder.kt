package org.kodein.memory

class SliceBuilder(val initialCapacity: Int, private val alloc: (Int) -> Allocation) : Closeable {

    private val allocs = ArrayList<Allocation>()

    private var current: Allocation = alloc(initialCapacity).also { allocs += it }

    private var startPosition: Int = 0

    private fun checkSize(size: Int) {
        if (current.position + size <= current.limit) return

        val neededSize = current.position - startPosition + size
        val factor =
                if (neededSize < (initialCapacity / 2))
                    1
                else
                    ((neededSize / initialCapacity) + 2)

        val previousAllocation = current
        val previousBuffer = previousAllocation.duplicate()
        previousBuffer.limitHere()
        val isOneSlice = startPosition == 0
        if (isOneSlice) {
            allocs.removeAt(allocs.lastIndex)
        }
        previousBuffer.position = startPosition
        current = alloc(factor * initialCapacity).also { allocs += it }
        startPosition = 0
        current.putBytes(previousBuffer)
        if (isOneSlice) {
            previousAllocation.close()
        }
    }

    private inner class BuilderWriteable : Writeable {
        override val remaining: Int get() = Int.MAX_VALUE

        override fun put(value: Byte) {
            checkSize(1)
            current.put(value)
        }

        override fun putChar(value: Char) {
            checkSize(2)
            current.putChar(value)
        }

        override fun putShort(value: Short) {
            checkSize(2)
            current.putShort(value)
        }

        override fun putInt(value: Int) {
            checkSize(4)
            current.putInt(value)
        }

        override fun putLong(value: Long) {
            checkSize(8)
            current.putLong(value)
        }

        override fun putFloat(value: Float) {
            checkSize(4)
            current.putFloat(value)
        }

        override fun putDouble(value: Double) {
            checkSize(8)
            current.putDouble(value)
        }

        override fun putBytes(src: ByteArray, srcOffset: Int, length: Int) {
            checkSize(length)
            current.putBytes(src, srcOffset, length)
        }

        override fun putBytes(src: Readable, length: Int) {
            checkSize(length)
            current.putBytes(src, length)
        }

        override fun internalBuffer(): Writeable = current.internalBuffer()
    }

    private val writeable = BuilderWriteable()

    fun newSlice(block: Writeable.() -> Unit): KBuffer {
        startPosition = current.position
        writeable.block()
        val dup = current.duplicate()
        dup.limitHere()
        dup.position = startPosition
        return dup.slice()
    }

    val allocationCount get() = allocs.size

    val allocationSize get() = allocs.sumBy { it.capacity }

    override fun close() {
        allocs.forEach { it.close() }
    }
}
