package org.kodein.memory.io

public interface ReadBuffer : ResettableReadable, ReadMemory {

    public val remaining: Int
    override var position: Int

    public fun sliceHere(): ReadBuffer = sliceHere(remaining)
    public fun sliceHere(length: Int): ReadBuffer

    override fun internalBuffer(): ReadBuffer
}

public fun ReadBuffer.readAllBytes(): ByteArray = readBytes(remaining)

public fun ReadBuffer.getByteHere(offset: Int = 0): Byte = getByte(position + offset)
public fun ReadBuffer.getCharHere(offset: Int = 0): Char = getChar(position + offset)
public fun ReadBuffer.getShortHere(offset: Int = 0): Short = getShort(position + offset)
public fun ReadBuffer.getIntHere(offset: Int = 0): Int = getInt(position + offset)
public fun ReadBuffer.getLongHere(offset: Int = 0): Long = getLong(position + offset)
public fun ReadBuffer.getFloatHere(offset: Int = 0): Float = getFloat(position + offset)
public fun ReadBuffer.getDoubleHere(offset: Int = 0): Double = getDouble(position + offset)
public fun ReadBuffer.getBytesHere(offset: Int = 0): ByteArray = getBytes(position + offset)

@ExperimentalUnsignedTypes
public fun ReadBuffer.getUByteHere(offset: Int = 0): UByte = getByte(position + offset).toUByte()
@ExperimentalUnsignedTypes
public fun ReadBuffer.getUShortHere(offset: Int = 0): UShort = getShort(position + offset).toUShort()
@ExperimentalUnsignedTypes
public fun ReadBuffer.getUIntHere(offset: Int = 0): UInt = getInt(position + offset).toUInt()
@ExperimentalUnsignedTypes
public fun ReadBuffer.getULongHere(offset: Int = 0): ULong = getLong(position + offset).toULong()
@ExperimentalUnsignedTypes
public fun ReadBuffer.getUBytesHere(offset: Int = 0): UByteArray = getBytes(position + offset).asUByteArray()

public inline fun <R> ReadBuffer.mark(block: () -> R): R {
    val mark = position
    try {
        return block()
    } finally {
        position = mark
    }
}

public inline fun <R> markAll(buffers: List<ReadBuffer>, block: () -> R): R {
    val marks = IntArray(buffers.size) { index -> buffers[index].position }
    try {
        return block()
    } finally {
        marks.forEachIndexed { index, mark -> buffers[index].position = mark }
    }
}

public inline fun <R> ReadBuffer.viewBuffer(block: (ReadBuffer) -> R): R = viewBuffer(position, remaining, block)
