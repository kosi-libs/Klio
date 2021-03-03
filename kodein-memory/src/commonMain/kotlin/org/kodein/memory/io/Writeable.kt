package org.kodein.memory.io

import kotlin.math.min

public interface Writeable {

    public val position: Int

    public fun requireCanWrite(needed: Int)

    public fun putByte(value: Byte)
    public fun putChar(value: Char)
    public fun putShort(value: Short)
    public fun putInt(value: Int)
    public fun putLong(value: Long)
    public fun putFloat(value: Float)
    public fun putDouble(value: Double)

    public fun putBytes(src: ByteArray, srcOffset: Int = 0, length: Int = src.size - srcOffset)
    public fun putMemoryBytes(src: ReadMemory, srcOffset: Int = 0, length: Int = src.limit - srcOffset)
    public fun putReadableBytes(src: Readable, length: Int)

    public fun flush()
}

public fun Writeable.putReadableBytes(src: ReadBuffer): Int {
    val count = src.remaining
    putReadableBytes(src, count)
    return count
}

public interface ResettableWriteable : Writeable {
    public fun reset()
    public fun resetHere()
    public fun flip()
}

@ExperimentalUnsignedTypes
public fun Writeable.putUByte(value: UByte): Unit = putByte(value.toByte())
@ExperimentalUnsignedTypes
public fun Writeable.putUShort(value: UShort): Unit = putShort(value.toShort())
@ExperimentalUnsignedTypes
public fun Writeable.putUInt(value: UInt): Unit = putInt(value.toInt())
@ExperimentalUnsignedTypes
public fun Writeable.putULong(value: ULong): Unit = putLong(value.toLong())
@ExperimentalUnsignedTypes
public fun Writeable.putUBytes(src: UByteArray, srcOffset: Int = 0, length: Int = src.size - srcOffset): Unit = putBytes(src.asByteArray(), srcOffset, length)

public fun Writeable.putReadableBytesBuffered(src: Readable, length: Int, bufferSize: Int = 16384) {
    val buffer = ByteArray(min(length, bufferSize))
    var left = length
    while (left > 0) {
        val read = min(left, buffer.size)
        src.readBytes(buffer, 0, read)
        this.putBytes(buffer, 0, read)
        left -= read
    }
}
