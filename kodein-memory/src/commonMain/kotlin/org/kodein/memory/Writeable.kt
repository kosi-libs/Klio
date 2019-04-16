package org.kodein.memory

import kotlin.math.max

interface Writeable {

    val remaining: Int

    fun put(value: Byte)
    fun putChar(value: Char)
    fun putShort(value: Short)
    fun putInt(value: Int)
    fun putLong(value: Long)
    fun putFloat(value: Float)
    fun putDouble(value: Double)

    fun putBytes(src: ByteArray, offset: Int = 0, length: Int = src.size)
    fun putBytes(src: Readable, length: Int = src.remaining)

    operator fun set(index: Int, value: Byte)
    fun setChar(index: Int, value: Char)
    fun setShort(index: Int, value: Short)
    fun setInt(index: Int, value: Int)
    fun setLong(index: Int, value: Long)
    fun setFloat(index: Int, value: Float)
    fun setDouble(index: Int, value: Double)
}
