package org.kodein.memory.io

import kotlin.math.min

interface Writeable {

    val available: Int

    fun putByte(value: Byte)
    fun putChar(value: Char)
    fun putShort(value: Short)
    fun putInt(value: Int)
    fun putLong(value: Long)
    fun putFloat(value: Float)
    fun putDouble(value: Double)

    fun putBytes(src: ByteArray, srcOffset: Int = 0, length: Int = src.size - srcOffset)
    fun putBytes(src: Readable, length: Int = src.available)

    fun flush()

}

@ExperimentalUnsignedTypes
fun Writeable.putUByte(value: UByte) = putByte(value.toByte())
@ExperimentalUnsignedTypes
fun Writeable.putUShort(value: UShort) = putShort(value.toShort())
@ExperimentalUnsignedTypes
fun Writeable.putUInt(value: UInt) = putInt(value.toInt())
@ExperimentalUnsignedTypes
fun Writeable.putULong(value: ULong) = putLong(value.toLong())
@ExperimentalUnsignedTypes
fun Writeable.putUBytes(src: UByteArray, srcOffset: Int = 0, length: Int = src.size - srcOffset) = putBytes(src.asByteArray(), srcOffset, length)

fun Writeable.putBytesBuffered(src: Readable, length: Int = src.available, bufferSize: Int = 16384) {
    val buffer = ByteArray(min(length, bufferSize))
    var left = length
    while (left > 0) {
        val read = min(left, buffer.size)
        src.readBytes(buffer, 0, read)
        this.putBytes(buffer, 0, read)
        left -= read
    }
}
