package org.kodein.memory.io

import org.kodein.memory.text.Charset

public interface Readable {

    public val available: Int
    public fun valid(): Boolean

    public fun receive(): Int
    public fun receive(dst: ByteArray, dstOffset: Int = 0, length: Int = dst.size): Int

    public fun readByte(): Byte
    public fun readChar(): Char
    public fun readShort(): Short
    public fun readInt(): Int
    public fun readLong(): Long
    public fun readFloat(): Float
    public fun readDouble(): Double
    public fun readBytes(dst: ByteArray, dstOffset: Int = 0, length: Int = dst.size - dstOffset)

    public fun skip(count: Int): Int

    public fun internalBuffer(): Readable
}

public fun Readable.readBytes(length: Int = available): ByteArray {
    val array = ByteArray(length)
    readBytes(array)
    return array
}

@ExperimentalUnsignedTypes
public fun Readable.readUByte(): UByte = readByte().toUByte()
@ExperimentalUnsignedTypes
public fun Readable.readUShort(): UShort = readShort().toUShort()
@ExperimentalUnsignedTypes
public fun Readable.readUInt(): UInt = readInt().toUInt()
@ExperimentalUnsignedTypes
public fun Readable.readULong(): ULong = readLong().toULong()
@ExperimentalUnsignedTypes
public fun Readable.readUBytes(dst: UByteArray, offset: Int = 0, length: Int = dst.size - offset): Unit = readBytes(dst.asByteArray(), offset, length)

public fun Readable.readLine(charset: Charset = Charset.UTF8): String? = buildString {
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
