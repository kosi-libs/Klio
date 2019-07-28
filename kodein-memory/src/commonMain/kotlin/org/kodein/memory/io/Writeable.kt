package org.kodein.memory.io

interface Writeable {

    val remaining: Int

    fun put(value: Byte)
    fun putChar(value: Char)
    fun putShort(value: Short)
    fun putInt(value: Int)
    fun putLong(value: Long)
    fun putFloat(value: Float)
    fun putDouble(value: Double)

    fun putBytes(src: ByteArray, srcOffset: Int = 0, length: Int = src.size - srcOffset)
    fun putBytes(src: Readable, length: Int = src.remaining)

}

@ExperimentalUnsignedTypes
fun Writeable.putUByte(value: UByte) = put(value.toByte())
@ExperimentalUnsignedTypes
fun Writeable.putUShort(value: UShort) = putShort(value.toShort())
@ExperimentalUnsignedTypes
fun Writeable.putUInt(value: UInt) = putInt(value.toInt())
@ExperimentalUnsignedTypes
fun Writeable.putULong(value: ULong) = putLong(value.toLong())
@ExperimentalUnsignedTypes
fun Writeable.putUBytes(src: UByteArray, srcOffset: Int = 0, length: Int = src.size - srcOffset) = putBytes(src.asByteArray(), srcOffset, length)
