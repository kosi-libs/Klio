package org.kodein.memory.io

import kotlin.math.min


internal open class BufferedReadableImpl(sequence: Sequence<Memory>) : Readable {

    internal var globalPosition = 0

    internal val buffers = ArrayDeque<Memory>()
    internal var firstBufferPosition = 0
    internal var buffersRemaining: Int = 0
    internal var isAtEnd = false

    internal var bufferIterator = sequence.iterator()

    private val intermediateBuffer = ByteArray(8)

    override val position: Int
        get() = globalPosition

    init {
        addBuffer()
    }

    private fun addBuffer() {
        val buffer = if (bufferIterator.hasNext()) bufferIterator.next().takeIf { it.size > 0 } else null
        if (buffer == null) {
            isAtEnd = true
            return
        }
        buffers.addLast(buffer)
        buffersRemaining += buffer.size
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
            if (isAtEnd) throw IOException("Needed at least $needed remaining bytes, but has only $buffersRemaining bytes.")
            addBuffer()
        }
    }

    override fun valid(): Boolean {
        if (buffersRemaining > 0) return true
        if (!isAtEnd) addBuffer()
        return buffersRemaining > 0
    }

    override fun tryReadByte(): Int {
        if (!valid()) return -1
        val b = buffers.first().getByte(firstBufferPosition)
        moveForward(1)
        return b.toInt()
    }

    private inline fun <T> tryReadBytes(dst: T, dstOffset: Int, length: Int, getBytes: Memory.(Int, T, Int, Int) -> Unit): Int {
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

    override fun tryReadBytes(dst: ByteArray, dstOffset: Int, length: Int): Int = tryReadBytes(dst, dstOffset, length, Memory::getBytes)

    override fun tryReadBytes(dst: Memory, dstOffset: Int, length: Int): Int = tryReadBytes(dst, dstOffset, length, Memory::getBytes)

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

    private inline fun <T> readValue(size: Int, getValue: Memory.(Int) -> T, loadValue: ((Int) -> Byte) -> T): T {
        requestCanRead(size)
        val firstBuffer = buffers.first()
        if (firstBuffer.size - firstBufferPosition >= size) {
            val value = firstBuffer.getValue(firstBufferPosition)
            moveForward(size)
            return value
        }
        readBytes(intermediateBuffer, 0, size)
        return loadValue { intermediateBuffer[it] }
    }

    override fun readShort(): Short = readValue(2, Memory::getShort, ::slowLoadShort)

    override fun readInt(): Int = readValue(4, Memory::getInt, ::slowLoadInt)

    override fun readLong(): Long = readValue(8, Memory::getLong, ::slowLoadLong)

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

internal class BufferedCursorReadableImpl(private val size: Int, private val getSequence: (Int) -> Sequence<Memory>) : CursorReadable, BufferedReadableImpl(getSequence(0)) {
    override var position: Int
        get() = globalPosition
        set(value) {
            buffers.clear()
            firstBufferPosition = 0
            buffersRemaining = 0
            isAtEnd = false
            bufferIterator = getSequence(value).iterator()
        }
    override val remaining: Int get() = size - position
}

public fun Sequence<Memory>.asBufferedReadable(): Readable = BufferedReadableImpl(this)
public fun bufferedCursorReadable(size: Int, getSequence: (Int) -> Sequence<Memory>): CursorReadable = BufferedCursorReadableImpl(size, getSequence)
