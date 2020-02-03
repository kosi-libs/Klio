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

@ExperimentalUnsignedTypes
fun Readable.peekUByte() = peek().toUByte()
@ExperimentalUnsignedTypes
fun Readable.readUByte() = read().toUByte()
@ExperimentalUnsignedTypes
fun Readable.readUShort() = readShort().toUShort()
@ExperimentalUnsignedTypes
fun Readable.readUInt() = readInt().toUInt()
@ExperimentalUnsignedTypes
fun Readable.readULong() = readLong().toULong()
@ExperimentalUnsignedTypes
fun Readable.readUBytes(dst: UByteArray, offset: Int = 0, length: Int = dst.size - offset) = readBytes(dst.asByteArray(), offset, length)


fun Readable.asSequence(): Sequence<Byte> = sequence { while (hasRemaining()) yield(read()) }
fun Readable.asCharSequence(): Sequence<Char> = sequence { while (hasRemaining()) yield(readChar()) }
fun Readable.asShortSequence(): Sequence<Short> = sequence { while (hasRemaining()) yield(readShort()) }
fun Readable.asIntSequence(): Sequence<Int> = sequence { while (hasRemaining()) yield(readInt()) }
fun Readable.asLongSequence(): Sequence<Long> = sequence { while (hasRemaining()) yield(readLong()) }
fun Readable.asFloatSequence(): Sequence<Float> = sequence { while (hasRemaining()) yield(readFloat()) }
fun Readable.asDoubleSequence(): Sequence<Double> = sequence { while (hasRemaining()) yield(readDouble()) }

@ExperimentalUnsignedTypes
fun Readable.asUSequence(): Sequence<UByte> = sequence { while (hasRemaining()) yield(readUByte()) }
@ExperimentalUnsignedTypes
fun Readable.asUShortSequence(): Sequence<UShort> = sequence { while (hasRemaining()) yield(readUShort()) }
@ExperimentalUnsignedTypes
fun Readable.asUIntSequence(): Sequence<UInt> = sequence { while (hasRemaining()) yield(readUInt()) }
@ExperimentalUnsignedTypes
fun Readable.asULongSequence(): Sequence<ULong> = sequence { while (hasRemaining()) yield(readULong()) }
