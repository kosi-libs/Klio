package org.kodein.memory.io

interface KBuffer : WriteBuffer, ReadBuffer {

    val capacity: Int

    override var limit: Int

    fun clear()
    fun flip()
    fun rewind()

    override fun duplicate(): KBuffer
    override fun slice(): KBuffer
    override fun slice(index: Int, length: Int): KBuffer

    override fun internalBuffer(): KBuffer

    companion object
}

@Suppress("NOTHING_TO_INLINE")
inline fun KBuffer.limitHere() {
    limit = position
}

fun KBuffer.Companion.wrap(array: ByteArray, offset: Int = 0, capacity: Int = array.size - offset) = ByteArrayKBuffer(array, offset, capacity)
fun KBuffer.Companion.array(capacity: Int) = ByteArrayKBuffer(ByteArray(capacity))

inline fun KBuffer.Companion.array(capacity: Int, block: KBuffer.() -> Unit): KBuffer {
    val buf = KBuffer.array(capacity)
    buf.block()
    buf.flip()
    return buf
}

fun KBuffer.Companion.arrayCopy(buffer: ReadBuffer) = array(buffer.remaining) { putBytes(buffer.duplicate()) }
