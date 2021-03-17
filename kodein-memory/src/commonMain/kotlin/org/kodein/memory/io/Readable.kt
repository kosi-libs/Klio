package org.kodein.memory.io

public interface Readable {

    public val position: Int

    public fun requestCanRead(needed: Int)

    public fun valid(): Boolean

    public fun tryReadByte(): Int
    public fun tryReadBytes(dst: ByteArray, dstOffset: Int = 0, length: Int = dst.size - dstOffset): Int
    public fun tryReadBytes(dst: Memory, dstOffset: Int = 0, length: Int = dst.size - dstOffset): Int

    public fun readByte(): Byte
    public fun readShort(): Short
    public fun readInt(): Int
    public fun readLong(): Long
    public fun readBytes(dst: ByteArray, dstOffset: Int = 0, length: Int = dst.size - dstOffset)

    public fun skip(count: Int)
    public fun skipAtMost(count: Int): Int
}

public fun Readable.readFloat(): Float = Float.fromBits(readInt())
public fun Readable.readDouble(): Double = Double.fromBits(readLong())

public fun Readable.readBytesCopy(length: Int): ByteArray {
    val array = ByteArray(length)
    readBytes(array)
    return array
}

public fun Readable.readBytes(dst: Memory, dstOffset: Int = 0, length: Int = dst.size - dstOffset): Unit = dst.setBytes(dstOffset, this, length)
public fun Readable.readBytes(dst: Writeable, length: Int): Unit = dst.writeBytes(this, length)

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
@ExperimentalUnsignedTypes
public fun Readable.readUBytesCopy(length: Int): UByteArray = readBytesCopy(length).asUByteArray()

public interface CursorReadable : Readable {
    public val remaining: Int
    override var position: Int
}

public fun CursorReadable.readBytesCopy(): ByteArray = readBytesCopy(remaining)
public fun CursorReadable.readBytes(dst: Writeable): Unit = readBytes(dst, remaining)
