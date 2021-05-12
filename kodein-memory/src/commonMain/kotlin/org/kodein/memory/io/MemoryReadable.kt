package org.kodein.memory.io

import kotlin.math.min


public class MemoryReadable(public val memory: ReadMemory) : SeekableCursorReadable {

    override var position: Int = 0
        set(value) {
            require(value >= 0) { "$value < 0." }
            require(value <= memory.size) { "position: $value > memory.size: ${memory.size}." }
            field = value
        }

    public fun readSlice(length: Int): ReadMemory {
        val slice = memory.slice(position, length)
        position += length
        return slice
    }

    override val remaining: Int get() = memory.size - position
    override val size: Int get() = memory.size

    override fun valid(): Boolean = remaining != 0

    override fun requestCanRead(needed: Int) {
        if (needed > remaining)
            throw IOException("Needed at least $needed remaining bytes, but has only $remaining bytes.")
    }

    override fun tryReadByte(): Int {
        if (!valid()) return -1
        val b = memory.getByte(position)
        position += 1
        return b.toInt()
    }

    override fun tryReadBytes(dst: ByteArray, dstOffset: Int, length: Int): Int {
        if (!valid()) return -1
        val readLength = min(length, remaining)
        memory.getBytes(position, dst, dstOffset, readLength)
        position += readLength
        return readLength
    }

    override fun tryReadBytes(dst: Memory): Int {
        if (!valid()) return -1
        val readLength = min(dst.size, remaining)
        dst.putBytes(0, memory.slice(position, readLength))
        position += readLength
        return readLength
    }

    private inline fun <T> readValue(size: Int, getValue: ReadMemory.(Int) -> T): T {
        requestCanRead(size)
        val value = memory.getValue(position)
        position += size
        return value
    }

    override fun readByte(): Byte = readValue(1, ReadMemory::getByte)
    override fun readShort(): Short = readValue(2, ReadMemory::getShort)
    override fun readInt(): Int = readValue(4, ReadMemory::getInt)
    override fun readLong(): Long = readValue(8, ReadMemory::getLong)

    override fun readBytes(dst: ByteArray, dstOffset: Int, length: Int) {
        requestCanRead(length)
        memory.getBytes(position, dst, dstOffset, length)
        position += length
    }

    override fun skip(count: Int) {
        requestCanRead(count)
        position += count
    }

    override fun skipAtMost(count: Int): Int {
        if (!valid()) return 0
        val skipCount = min(count, remaining)
        position += skipCount
        return skipCount
    }
}

public fun ReadMemory.asReadable(): MemoryReadable = MemoryReadable(this)
