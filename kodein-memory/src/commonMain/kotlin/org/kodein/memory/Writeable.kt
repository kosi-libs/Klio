package org.kodein.memory

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

    fun internalBuffer(): Writeable

}
