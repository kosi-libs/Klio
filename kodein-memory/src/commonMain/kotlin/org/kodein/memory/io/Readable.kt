package org.kodein.memory.io

import org.kodein.memory.text.Charset

interface Readable {

    val available: Int
    fun valid(): Boolean

    fun receive(): Int
    fun receive(dst: ByteArray, dstOffset: Int = 0, length: Int = dst.size): Int

    fun readByte(): Byte
    fun readChar(): Char
    fun readShort(): Short
    fun readInt(): Int
    fun readLong(): Long
    fun readFloat(): Float
    fun readDouble(): Double
    fun readBytes(dst: ByteArray, dstOffset: Int = 0, length: Int = dst.size - dstOffset)

    fun skip(count: Int): Int

    fun internalBuffer(): Readable
}

fun Readable.readBytes(length: Int = available): ByteArray {
    val array = ByteArray(length)
    readBytes(array)
    return array
}

@ExperimentalUnsignedTypes
fun Readable.readUByte() = readByte().toUByte()
@ExperimentalUnsignedTypes
fun Readable.readUShort() = readShort().toUShort()
@ExperimentalUnsignedTypes
fun Readable.readUInt() = readInt().toUInt()
@ExperimentalUnsignedTypes
fun Readable.readULong() = readLong().toULong()
@ExperimentalUnsignedTypes
fun Readable.readUBytes(dst: UByteArray, offset: Int = 0, length: Int = dst.size - offset) = readBytes(dst.asByteArray(), offset, length)

fun Readable.readLine(charset: Charset = Charset.UTF8): String? = buildString {
    var count = 0
    while (true) {
        val next = charset.tryDecode(this@readLine)
        if (next < 0) {
            if (count == 0) return null
            break
        }
        val nextChar = next.toChar()
        if (nextChar == '\n') break
        if (nextChar == '\r') {
            val after = charset.tryDecode(this@readLine)
            if (after == -1) {
                append(nextChar)
                break
            }
            val afterChar = after.toChar()
            if (afterChar == '\n') break
            append(nextChar)
            append(afterChar)
            continue
        }
        append(nextChar)
        ++count
    }
}
