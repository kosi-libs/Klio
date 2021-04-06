package org.kodein.memory.io

import kotlin.math.min

public interface ReadMemory {

    public val size: Int

    public fun slice(index: Int, length: Int): ReadMemory

    public operator fun get(index: Int): Byte = getByte(index)
    public fun getByte(index: Int): Byte
    public fun getShort(index: Int): Short
    public fun getInt(index: Int): Int
    public fun getLong(index: Int): Long

    public fun getBytes(index: Int, dst: ByteArray, dstOffset: Int, length: Int)

    public fun internalMemory(): ReadMemory
}

public fun ReadMemory.sliceAt(index: Int): ReadMemory = slice(index, size - index)

public fun ReadMemory.getFloat(index: Int): Float = Float.fromBits(getInt(index))
public fun ReadMemory.getDouble(index: Int): Double = Double.fromBits(getLong(index))

public fun ReadMemory.getBytes(index: Int, dst: ByteArray): Unit = getBytes(index, dst, 0, dst.size)

public fun ReadMemory.getBytes(index: Int, length: Int): ByteArray {
    val array = ByteArray(length)
    getBytes(index, array)
    return array
}

public fun ReadMemory.getBytes(): ByteArray = getBytes(0, size)


@ExperimentalUnsignedTypes
public fun ReadMemory.getUByte(index: Int): UByte = getByte(index).toUByte()
@ExperimentalUnsignedTypes
public fun ReadMemory.getUShort(index: Int): UShort = getShort(index).toUShort()
@ExperimentalUnsignedTypes
public fun ReadMemory.getUInt(index: Int): UInt = getInt(index).toUInt()
@ExperimentalUnsignedTypes
public fun ReadMemory.getULong(index: Int): ULong = getLong(index).toULong()
@ExperimentalUnsignedTypes
public fun ReadMemory.getUBytes(index: Int, dst: UByteArray, dstOffset: Int, length: Int): Unit = getBytes(index, dst.asByteArray(), dstOffset, length)
@ExperimentalUnsignedTypes
public fun ReadMemory.getUBytes(index: Int, dst: UByteArray): Unit = getBytes(index, dst.asByteArray(), 0, dst.size)
@ExperimentalUnsignedTypes
public fun ReadMemory.getUBytes(index: Int, length: Int): UByteArray = getBytes(index, length).asUByteArray()
@ExperimentalUnsignedTypes
public fun ReadMemory.getUBytes(): UByteArray = getBytes().asUByteArray()

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

public val ReadMemory.lastIndex: Int get() = size - 1

public val ReadMemory.indices: IntRange get() = 0..lastIndex

public fun ReadMemory.lastIndexOf(search: Byte, startFrom: Int = lastIndex): Int {
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
