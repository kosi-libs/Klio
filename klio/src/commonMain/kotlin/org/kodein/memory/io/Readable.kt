package org.kodein.memory.io

public interface Readable {

    public val position: Int

    public fun requestCanRead(needed: Int)

    public fun valid(): Boolean

    public fun tryReadByte(): Int
    public fun tryReadBytes(dst: ByteArray, dstOffset: Int, length: Int): Int
    public fun tryReadBytes(dst: Memory): Int

    public fun readByte(): Byte
    public fun readShort(): Short
    public fun readInt(): Int
    public fun readLong(): Long
    public fun readBytes(dst: ByteArray, dstOffset: Int, length: Int)

    public fun skip(count: Int)
    public fun skipAtMost(count: Int): Int
}

public fun Readable.tryReadBytes(dst: ByteArray): Int = tryReadBytes(dst, 0, dst.size)
public fun Readable.readBytes(dst: ByteArray): Unit = readBytes(dst, 0, dst.size)

public fun Readable.readFloat(): Float = Float.fromBits(readInt())
public fun Readable.readDouble(): Double = Double.fromBits(readLong())

public fun Readable.readBytes(length: Int): ByteArray {
    val array = ByteArray(length)
    readBytes(array)
    return array
}

public fun Readable.readBytes(dst: Memory): Unit = dst.putBytes(0, this, dst.size)
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
public fun Readable.readUBytes(length: Int): UByteArray = readBytes(length).asUByteArray()

public interface CursorReadable : Readable {
    public val size: Int
    public val remaining: Int
}

public interface SeekableCursorReadable : CursorReadable {
    override var position: Int
}

public fun CursorReadable.readBytes(): ByteArray = readBytes(remaining)
public fun CursorReadable.readBytes(dst: Writeable): Unit = readBytes(dst, remaining)

public fun ByteArray.asReadable(offset: Int, size: Int): CursorReadable = asMemory(offset, size).asReadable()
public fun ByteArray.asReadable(): CursorReadable = asMemory().asReadable()
