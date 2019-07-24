package org.kodein.memory.io

import kotlin.math.min

interface Readable {

    val remaining: Int

    fun peek(): Byte
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
inline fun Readable.hasRemaining(): Boolean = remaining != 0

fun Readable.readAtMostBytes(dst: ByteArray, offset: Int = 0, length: Int = dst.size - offset): Int {
    if (length == 0) return 0
    if (!hasRemaining()) return -1
    val read = min(length, remaining)
    readBytes(dst, offset, read)
    return read
}

fun Readable.skipAtMost(count: Int): Int {
    if (count <= 0 || !hasRemaining())
        return 0
    val skip = min(count, remaining)
    skip(skip)
    return skip
}
