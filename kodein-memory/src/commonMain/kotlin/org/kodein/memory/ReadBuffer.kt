package org.kodein.memory

interface ReadBuffer : Readable {

    var position: Int

    val limit: Int

    fun duplicate(): ReadBuffer
    fun slice(): ReadBuffer
    fun slice(index: Int, length: Int = limit - index): ReadBuffer

    operator fun get(index: Int): Byte
    fun getChar(index: Int): Char
    fun getShort(index: Int): Short
    fun getInt(index: Int): Int
    fun getLong(index: Int): Long
    fun getFloat(index: Int): Float
    fun getDouble(index: Int): Double

    fun getBytes(index: Int, dst: ByteArray, offset: Int = 0, length: Int = dst.size - offset)

    override fun internalBuffer(): ReadBuffer
}

fun ReadBuffer.getBytes(index: Int, length: Int = limit - index): ByteArray {
    val array = ByteArray(length)
    getBytes(index, array)
    return array
}

fun ReadBuffer.getCharHere() = getChar(position)
fun ReadBuffer.getShortHere() = getShort(position)
fun ReadBuffer.getIntHere() = getInt(position)
fun ReadBuffer.getLongHere() = getLong(position)
fun ReadBuffer.getFloatHere() = getFloat(position)
fun ReadBuffer.getDoubleHere() = getDouble(position)
fun ReadBuffer.getBytesHere() = getBytes(position)

inline fun mark(buffer: ReadBuffer, block: () -> Unit) {
    val mark = buffer.position
    try {
        block()
    } finally {
        buffer.position = mark
    }
}

inline fun mark(vararg buffers: ReadBuffer, block: () -> Unit) {
    val marks = IntArray(buffers.size) { index -> buffers[index].position }
    try {
        block()
    } finally {
        marks.forEachIndexed { index, mark -> buffers[index].position = mark }
    }
}
