package org.kodein.memory.io

import kotlin.math.min

interface ReadMemory {

    val limit: Int

    fun duplicate(): ReadBuffer
    fun slice(): ReadBuffer
    fun slice(index: Int, length: Int = limit - index): ReadBuffer

    operator fun get(index: Int) = getByte(index)
    fun getByte(index: Int): Byte
    fun getChar(index: Int): Char
    fun getShort(index: Int): Short
    fun getInt(index: Int): Int
    fun getLong(index: Int): Long
    fun getFloat(index: Int): Float
    fun getDouble(index: Int): Double

    fun getBytes(index: Int, dst: ByteArray, dstOffset: Int = 0, length: Int = dst.size - dstOffset)

    fun internalBuffer(): ReadMemory
}

fun ReadMemory.getBytes(index: Int, length: Int = limit - index): ByteArray {
    val array = ByteArray(length)
    getBytes(index, array)
    return array
}

@ExperimentalUnsignedTypes
fun ReadMemory.getUByte(index: Int) = getByte(index).toUByte()
@ExperimentalUnsignedTypes
fun ReadMemory.getUShort(index: Int) = getShort(index).toUShort()
@ExperimentalUnsignedTypes
fun ReadMemory.getUInt(index: Int) = getInt(index).toUInt()
@ExperimentalUnsignedTypes
fun ReadMemory.getULong(index: Int) = getLong(index).toULong()
@ExperimentalUnsignedTypes
fun ReadMemory.getUBytes(index: Int) = getBytes(index).asUByteArray()

operator fun ReadMemory.compareTo(other: ReadMemory): Int {
    for (i in 0 until min(limit, other.limit)) {
        val b1 = this.getByte(i)
        val b2 = other.getByte(i)
        if (b1 != b2) return b1 - b2
    }
    return limit - other.limit
}

operator fun ReadMemory.compareTo(other: ByteArray): Int {
    for (i in 0 until min(limit, other.size)) {
        val b1 = this.getByte(i)
        val b2 = other[i]
        if (b1 != b2) return b1 - b2
    }
    return limit - other.size
}

val ReadMemory.size: Int get() = limit

inline fun <R> ReadMemory.markBuffer(block: (ReadBuffer) -> R): R =
        when (this) {
            is ReadBuffer -> mark<R> { block(this) }
            else -> block(duplicate())
        }

inline fun <R> ReadMemory.viewBuffer(index: Int, length: Int, block: (ReadBuffer) -> R): R =
        if (this is KBuffer) view<R>(index, length) { block(this) }
        else block(slice(index, length))