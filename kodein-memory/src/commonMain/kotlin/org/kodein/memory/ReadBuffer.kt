package org.kodein.memory

interface ReadBuffer : Readable {

    var position: Int

    val limit: Int

    fun duplicate(): ReadBuffer
    fun slice(): ReadBuffer
    fun view(index: Int, length: Int = limit - index): ReadBuffer

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

inline fun ReadBuffer.mark(block: () -> Unit) {
    val mark = position
    try {
        block()
    } finally {
        position = mark
    }
}
