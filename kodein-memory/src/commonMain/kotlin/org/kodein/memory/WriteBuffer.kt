package org.kodein.memory

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

    override fun internalBuffer(): WriteBuffer

}

