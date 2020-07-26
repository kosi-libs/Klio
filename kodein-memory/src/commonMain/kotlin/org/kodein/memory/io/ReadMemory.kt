package org.kodein.memory.io

import kotlin.math.min

public interface ReadMemory {

    public val limit: Int

    public fun duplicate(): ReadBuffer
    public fun slice(): ReadBuffer
    public fun slice(index: Int, length: Int = limit - index): ReadBuffer

    public operator fun get(index: Int): Byte = getByte(index)
    public fun getByte(index: Int): Byte
    public fun getChar(index: Int): Char
    public fun getShort(index: Int): Short
    public fun getInt(index: Int): Int
    public fun getLong(index: Int): Long
    public fun getFloat(index: Int): Float
    public fun getDouble(index: Int): Double

    public fun getBytes(index: Int, dst: ByteArray, dstOffset: Int = 0, length: Int = dst.size - dstOffset)

    public fun internalBuffer(): ReadMemory
}

public fun ReadMemory.getBytes(index: Int, length: Int = limit - index): ByteArray {
    val array = ByteArray(length)
    getBytes(index, array)
    return array
}

@ExperimentalUnsignedTypes
public fun ReadMemory.getUByte(index: Int): UByte = getByte(index).toUByte()
@ExperimentalUnsignedTypes
public fun ReadMemory.getUShort(index: Int): UShort = getShort(index).toUShort()
@ExperimentalUnsignedTypes
public fun ReadMemory.getUInt(index: Int): UInt = getInt(index).toUInt()
@ExperimentalUnsignedTypes
public fun ReadMemory.getULong(index: Int): ULong = getLong(index).toULong()
@ExperimentalUnsignedTypes
public fun ReadMemory.getUBytes(index: Int): UByteArray = getBytes(index).asUByteArray()

public operator fun ReadMemory.compareTo(other: ReadMemory): Int {
    for (i in 0 until min(limit, other.limit)) {
        val b1 = this.getByte(i)
        val b2 = other.getByte(i)
        if (b1 != b2) return b1 - b2
    }
    return limit - other.limit
}

public operator fun ReadMemory.compareTo(other: ByteArray): Int {
    for (i in 0 until min(limit, other.size)) {
        val b1 = this.getByte(i)
        val b2 = other[i]
        if (b1 != b2) return b1 - b2
    }
    return limit - other.size
}

public val ReadMemory.size: Int get() = limit

public inline fun <R> ReadMemory.markBuffer(block: (ReadBuffer) -> R): R =
        when (this) {
            is ReadBuffer -> mark<R> { block(this) }
            else -> block(duplicate())
        }

public inline fun <R> ReadMemory.viewBuffer(index: Int, length: Int, block: (ReadBuffer) -> R): R =
        if (this is KBuffer) view<R>(index, length) { block(this) }
        else block(slice(index, length))
