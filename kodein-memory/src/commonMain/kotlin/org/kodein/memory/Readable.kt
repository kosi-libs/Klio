package org.kodein.memory

interface Readable {

    val remaining: Int

    fun read(): Byte
    fun readChar(): Char
    fun readShort(): Short
    fun readInt(): Int
    fun readLong(): Long
    fun readFloat(): Float
    fun readDouble(): Double

    fun readBytes(dst: ByteArray, offset: Int = 0, length: Int = dst.size)

    operator fun get(index: Int): Byte
    fun getChar(index: Int): Char
    fun getShort(index: Int): Short
    fun getInt(index: Int): Int
    fun getLong(index: Int): Long
    fun getFloat(index: Int): Float
    fun getDouble(index: Int): Double

    fun skip(count: Int)
}
