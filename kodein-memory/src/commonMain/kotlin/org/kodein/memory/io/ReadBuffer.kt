package org.kodein.memory.io

interface ReadBuffer : Readable, ReadMemory {

    var position: Int

    override fun internalBuffer(): ReadBuffer
}


fun ReadBuffer.getHere(offset: Int = 0) = getByte(position + offset)
fun ReadBuffer.getCharHere(offset: Int = 0) = getChar(position + offset)
fun ReadBuffer.getShortHere(offset: Int = 0) = getShort(position + offset)
fun ReadBuffer.getIntHere(offset: Int = 0) = getInt(position + offset)
fun ReadBuffer.getLongHere(offset: Int = 0) = getLong(position + offset)
fun ReadBuffer.getFloatHere(offset: Int = 0) = getFloat(position + offset)
fun ReadBuffer.getDoubleHere(offset: Int = 0) = getDouble(position + offset)
fun ReadBuffer.getBytesHere(offset: Int = 0) = getBytes(position + offset)

@ExperimentalUnsignedTypes
fun ReadBuffer.getUByteHere(offset: Int = 0) = getByte(position + offset).toUByte()
@ExperimentalUnsignedTypes
fun ReadBuffer.getUShortHere(offset: Int = 0) = getShort(position + offset).toUShort()
@ExperimentalUnsignedTypes
fun ReadBuffer.getUIntHere(offset: Int = 0) = getInt(position + offset).toUInt()
@ExperimentalUnsignedTypes
fun ReadBuffer.getULongHere(offset: Int = 0) = getLong(position + offset).toULong()
@ExperimentalUnsignedTypes
fun ReadBuffer.getUBytesHere(offset: Int = 0) = getBytes(position + offset).asUByteArray()

inline fun <R> ReadBuffer.mark(block: () -> R): R {
    val mark = position
    try {
        return block()
    } finally {
        position = mark
    }
}

inline fun <R> markAll(buffers: List<ReadBuffer>, block: () -> R): R {
    val marks = IntArray(buffers.size) { index -> buffers[index].position }
    try {
        return block()
    } finally {
        marks.forEachIndexed { index, mark -> buffers[index].position = mark }
    }
}

inline fun <R> ReadBuffer.viewBuffer(block: (ReadBuffer) -> R): R = viewBuffer(position, available, block)
