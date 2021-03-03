package org.kodein.memory.io

import kotlin.math.min

public interface WriteMemory {

    public val limit: Int

    public fun duplicate(): WriteBuffer
    public fun slice(index: Int): WriteBuffer = slice(index, limit - index) // Bug in non-IR JS backend that prevents default param values here.
    public fun slice(index: Int, length: Int /*= limit - index*/): WriteBuffer

    public operator fun set(index: Int, value: Byte): Unit = setByte(index, value)
    public fun setByte(index: Int, value: Byte)
    public fun setChar(index: Int, value: Char)
    public fun setShort(index: Int, value: Short)
    public fun setInt(index: Int, value: Int)
    public fun setLong(index: Int, value: Long)
    public fun setFloat(index: Int, value: Float)
    public fun setDouble(index: Int, value: Double)

    public fun setBytes(index: Int, src: ByteArray, srcOffset: Int = 0, length: Int = src.size - srcOffset)
    public fun setMemoryBytes(index: Int, src: ReadMemory, srcOffset: Int = 0, length: Int = src.limit - srcOffset)
    public fun setReadableBytes(index: Int, src: Readable, length: Int)
}

public fun WriteMemory.setReadableBytes(index: Int, src: ReadBuffer): Int {
    val count = src.remaining
    setReadableBytes(index, src, src.remaining)
    return count
}

@ExperimentalUnsignedTypes
public fun WriteMemory.setUByte(index: Int, value: UByte): Unit = setByte(index, value.toByte())
@ExperimentalUnsignedTypes
public fun WriteMemory.setUShort(index: Int, value: UShort): Unit = setShort(index, value.toShort())
@ExperimentalUnsignedTypes
public fun WriteMemory.setUInt(index: Int, value: UInt): Unit = setInt(index, value.toInt())
@ExperimentalUnsignedTypes
public fun WriteMemory.setULong(index: Int, value: ULong): Unit = setLong(index, value.toLong())
@ExperimentalUnsignedTypes
public fun WriteMemory.setUBytes(index: Int, src: UByteArray, srcOffset: Int = 0, length: Int = src.size - srcOffset): Unit = setBytes(index, src.asByteArray(), srcOffset, length)
