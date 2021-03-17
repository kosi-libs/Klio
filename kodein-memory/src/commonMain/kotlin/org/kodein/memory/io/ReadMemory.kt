package org.kodein.memory.io

import kotlin.math.min

public interface ReadMemory {

    public val size: Int

    public fun slice(index: Int, length: Int = size - index): ReadMemory

    public operator fun get(index: Int): Byte = getByte(index)
    public fun getByte(index: Int): Byte
    public fun getShort(index: Int): Short
    public fun getInt(index: Int): Int
    public fun getLong(index: Int): Long

    public fun getBytes(index: Int, dst: ByteArray, dstOffset: Int = 0, length: Int = dst.size - dstOffset)

    public fun internalMemory(): ReadMemory
}

public fun ReadMemory.getFloat(index: Int): Float = Float.fromBits(getInt(index))
public fun ReadMemory.getDouble(index: Int): Double = Double.fromBits(getLong(index))

public fun ReadMemory.getBytesCopy(index: Int, length: Int = size - index): ByteArray {
    val array = ByteArray(length)
    getBytes(index, array)
    return array
}

public fun ReadMemory.getBytes(index: Int, dst: Memory, dstOffset: Int = 0, length: Int = dst.size - dstOffset): Unit = dst.setBytes(dstOffset, this, index, length)
public fun ReadMemory.getBytes(index: Int, dst: Writeable, length: Int): Unit = dst.writeBytes(this, index, length)

@ExperimentalUnsignedTypes
public fun ReadMemory.getUByte(index: Int): UByte = getByte(index).toUByte()
@ExperimentalUnsignedTypes
public fun ReadMemory.getUShort(index: Int): UShort = getShort(index).toUShort()
@ExperimentalUnsignedTypes
public fun ReadMemory.getUInt(index: Int): UInt = getInt(index).toUInt()
@ExperimentalUnsignedTypes
public fun ReadMemory.getULong(index: Int): ULong = getLong(index).toULong()
@ExperimentalUnsignedTypes
public fun ReadMemory.getUBytes(index: Int, dst: UByteArray, dstOffset: Int = 0, length: Int = dst.size - dstOffset): Unit = getBytes(index, dst.asByteArray(), dstOffset, length)
@ExperimentalUnsignedTypes
public fun ReadMemory.getUBytesCopy(index: Int, length: Int = size - index): UByteArray = getBytesCopy(index, length).asUByteArray()

public operator fun ReadMemory.compareTo(other: ReadMemory): Int {
    for (i in 0 until min(size, other.size)) {
        val b1 = this.getByte(i)
        val b2 = other.getByte(i)
        if (b1 != b2) return b1 - b2
    }
    return size - other.size
}

public operator fun ReadMemory.compareTo(other: ByteArray): Int {
    for (i in 0 until min(size, other.size)) {
        val b1 = this.getByte(i)
        val b2 = other[i]
        if (b1 != b2) return b1 - b2
    }
    return size - other.size
}

public fun ReadMemory.firstIndexOf(search: Byte, startAt: Int = 0): Int {
    for (index in startAt until size) {
        if (get(index) == search)
            return index
    }

    return -1
}

public fun ReadMemory.lastIndexOf(search: Byte, startFrom: Int = size - 1): Int {
    for (index in startFrom downTo 0) {
        if (get(index) == search)
            return index
    }

    return -1
}

public fun ReadMemory.startsWith(prefix: ReadMemory): Boolean {
    if (this.size < prefix.size)
        return false

    val start = this.slice(0, prefix.size)

    return start.compareTo(prefix) == 0
}

public fun ReadMemory.startsWith(prefix: ByteArray): Boolean {
    if (this.size < prefix.size)
        return false

    val start = this.slice(0, prefix.size)

    return start.compareTo(prefix) == 0
}
