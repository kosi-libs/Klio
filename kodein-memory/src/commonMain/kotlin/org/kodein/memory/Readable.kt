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

    fun readBytes(dst: ByteArray, offset: Int = 0, length: Int = dst.size - offset)

    fun skip(count: Int)

    fun internalBuffer(): Readable
}

fun Readable.readBytes(length: Int = remaining): ByteArray {
    val array = ByteArray(length)
    readBytes(array)
    return array
}

@Suppress("NOTHING_TO_INLINE")
inline fun ReadBuffer.hasRemaining(): Boolean = remaining != 0
