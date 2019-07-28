package org.kodein.memory.io

interface WriteBuffer : Writeable {

    var position: Int

    val limit: Int

    operator fun set(index: Int, value: Byte)
    fun setChar(index: Int, value: Char)
    fun setShort(index: Int, value: Short)
    fun setInt(index: Int, value: Int)
    fun setLong(index: Int, value: Long)
    fun setFloat(index: Int, value: Float)
    fun setDouble(index: Int, value: Double)

    fun setBytes(index: Int, src: ByteArray, srcOffset: Int = 0, length: Int = src.size - srcOffset)
    fun setBytes(index: Int, src: ReadBuffer, srcOffset: Int = src.position, length: Int = src.remaining)

}


@ExperimentalUnsignedTypes
fun WriteBuffer.setUByte(index: Int, value: UByte) = set(index, value.toByte())
@ExperimentalUnsignedTypes
fun WriteBuffer.setUShort(index: Int, value: UShort) = setShort(index, value.toShort())
@ExperimentalUnsignedTypes
fun WriteBuffer.setUInt(index: Int, value: UInt) = setInt(index, value.toInt())
@ExperimentalUnsignedTypes
fun WriteBuffer.setULong(index: Int, value: ULong) = setLong(index, value.toLong())
@ExperimentalUnsignedTypes
fun WriteBuffer.setUBytes(index: Int, src: UByteArray, srcOffset: Int = 0, length: Int = src.size - srcOffset) = setBytes(index, src.asByteArray(), srcOffset, length)
