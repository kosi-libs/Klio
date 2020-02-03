package org.kodein.memory.io

interface WriteMemory {

    val limit: Int

    fun duplicate(): WriteBuffer

    operator fun set(index: Int, value: Byte)
    fun setChar(index: Int, value: Char)
    fun setShort(index: Int, value: Short)
    fun setInt(index: Int, value: Int)
    fun setLong(index: Int, value: Long)
    fun setFloat(index: Int, value: Float)
    fun setDouble(index: Int, value: Double)

    fun setBytes(index: Int, src: ByteArray, srcOffset: Int = 0, length: Int = src.size - srcOffset)
    fun setBytes(index: Int, src: ReadMemory, srcOffset: Int = 0, length: Int = src.limit - srcOffset)

}

@ExperimentalUnsignedTypes
fun WriteMemory.setUByte(index: Int, value: UByte) = set(index, value.toByte())
@ExperimentalUnsignedTypes
fun WriteMemory.setUShort(index: Int, value: UShort) = setShort(index, value.toShort())
@ExperimentalUnsignedTypes
fun WriteMemory.setUInt(index: Int, value: UInt) = setInt(index, value.toInt())
@ExperimentalUnsignedTypes
fun WriteMemory.setULong(index: Int, value: ULong) = setLong(index, value.toLong())
@ExperimentalUnsignedTypes
fun WriteMemory.setUBytes(index: Int, src: UByteArray, srcOffset: Int = 0, length: Int = src.size - srcOffset) = setBytes(index, src.asByteArray(), srcOffset, length)
