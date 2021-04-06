package org.kodein.memory.io

import kotlin.math.min


public interface BufferedMemoryReadable : Readable {
    public fun tryReadSlice(length: Int): ReadMemory?
}

public abstract class AbstractBufferedMemoryReadable : BufferedMemoryReadable {

    internal var globalPosition = 0

    internal val buffers = ArrayDeque<ReadMemory>()
    internal var buffersRemaining: Int = 0

    private var firstBufferPosition = 0

    override val position: Int
        get() = globalPosition

    internal abstract fun nextBuffer(): ReadMemory?

    private fun pullBuffer(): Boolean {
        val buffer = nextBuffer()?.takeIf { it.size > 0 } ?: return false
        buffers.addLast(buffer)
        buffersRemaining += buffer.size
        return true
    }

    private fun moveForward(size: Int) {
        globalPosition += size
        firstBufferPosition += size
        buffersRemaining -= size
        while (firstBufferPosition > 0 && firstBufferPosition >= buffers.first().size) {
            val removed = buffers.removeFirst()
            firstBufferPosition -= removed.size
        }
    }

    override fun requestCanRead(needed: Int) {
        while (needed > buffersRemaining) {
            if (!pullBuffer()) throw IOException("Needed at least $needed remaining bytes, but has only $buffersRemaining bytes.")
        }
    }

    override fun valid(): Boolean {
        if (buffersRemaining > 0) return true
        return pullBuffer()
    }

    override fun tryReadByte(): Int {
        if (!valid()) return -1
        val b = buffers.first().getByte(firstBufferPosition)
        moveForward(1)
        return b.toInt()
    }

    private inline fun <T> tryReadBytes(dst: T, dstOffset: Int, length: Int, getBytes: ReadMemory.(Int, T, Int, Int) -> Unit): Int {
        if (!valid()) return -1
        var remaining = length
        var offset = 0
        while (remaining > 0) {
            val firstBuffer = buffers.first()
            val chunkSize = min(remaining, firstBuffer.size - firstBufferPosition)
            firstBuffer.getBytes(firstBufferPosition, dst, dstOffset + offset, chunkSize)
            remaining -= chunkSize
            offset += chunkSize
            moveForward(chunkSize)
            if (!valid()) break
        }
        return offset
    }

    override fun tryReadBytes(dst: ByteArray, dstOffset: Int, length: Int): Int = tryReadBytes(dst, dstOffset, length, ReadMemory::getBytes)

    @Suppress("NAME_SHADOWING")
    override fun tryReadBytes(dst: Memory): Int = tryReadBytes(dst, 0, dst.size) { index, dst, dstOffset, length -> dst.putBytes(dstOffset, this.slice(index, length)) }

    override fun readByte(): Byte {
        requestCanRead(1)
        val b = buffers.first().getByte(firstBufferPosition)
        moveForward(1)
        return b
    }

    override fun readBytes(dst: ByteArray, dstOffset: Int, length: Int) {
        val read = tryReadBytes(dst, dstOffset, length)
        if (read < length) throw IOException("Needed to read $length bytes, but could only read $read bytes.")
    }

    override fun tryReadSlice(length: Int): ReadMemory? {
        val firstBuffer = buffers.first()
        if (firstBuffer.size - firstBufferPosition <= length) {
            val slice = firstBuffer.slice(firstBufferPosition, length)
            moveForward(length)
            return slice
        }
        return null
    }

    private inline fun <T> readValue(size: Int, getValue: ReadMemory.(Int) -> T, loadValue: ((Int) -> Byte) -> T): T {
        requestCanRead(size)
        val firstBuffer = buffers.first()
        if (firstBuffer.size - firstBufferPosition >= size) {
            val value = firstBuffer.getValue(firstBufferPosition)
            moveForward(size)
            return value
        }
        return loadValue { readByte() }
    }

    override fun readShort(): Short = readValue(2, ReadMemory::getShort, ::slowLoadShort)

    override fun readInt(): Int = readValue(4, ReadMemory::getInt, ::slowLoadInt)

    override fun readLong(): Long = readValue(8, ReadMemory::getLong, ::slowLoadLong)

    override fun skip(count: Int) {
        val skipped = skipAtMost(count)
        if (skipped < count) throw IOException("Needed to skip $count bytes, but could only skip $skipped bytes.")
    }

    override fun skipAtMost(count: Int): Int {
        if (!valid()) return 0
        var remaining = count
        var skipped = 0
        while (remaining > 0) {
            val firstBuffer = buffers.first()
            val chunkSize = min(remaining, firstBuffer.size - firstBufferPosition)
            remaining -= chunkSize
            skipped += chunkSize
            moveForward(chunkSize)
            if (!valid()) break
        }
        return skipped
    }
}

public class BufferedMemoryPullReadable(sequence: Sequence<ReadMemory>) : AbstractBufferedMemoryReadable() {
    internal var bufferIterator = sequence.iterator()

    override fun nextBuffer(): ReadMemory? =
        if (bufferIterator.hasNext()) bufferIterator.next()
        else null
}

public class BufferedMemoryPushReadable : AbstractBufferedMemoryReadable(), CursorReadable {

    override val remaining: Int get() = buffersRemaining

    override fun nextBuffer(): ReadMemory? = null

    public fun pushBuffer(buffer: ReadMemory) {
        buffers.addLast(buffer)
        buffersRemaining += buffer.size
    }
}
