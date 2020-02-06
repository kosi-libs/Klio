package org.kodein.memory.io

import kotlin.math.min
import kotlin.random.Random

interface KBuffer : WriteBuffer, ReadBuffer {

    val offset: Int

    val capacity: Int

    override var limit: Int

    fun offset(newOffset: Int)

    fun reset()
    fun flip()

    override fun duplicate(): KBuffer
    override fun slice(): KBuffer
    override fun slice(index: Int, length: Int): KBuffer

    override fun internalBuffer(): KBuffer

    fun backingArray(): ByteArray?

    companion object
}

@Suppress("NOTHING_TO_INLINE")
inline fun KBuffer.limitHere() {
    limit = position
}

fun KBuffer.Companion.wrap(array: ByteArray, offset: Int = 0, limit: Int = array.size - offset) =
        ByteArrayKBuffer(array).also {
            it.offset(offset)
            it.limit = limit
        }

fun KBuffer.Companion.array(capacity: Int) = ByteArrayKBuffer(ByteArray(capacity))

inline fun KBuffer.Companion.array(capacity: Int, block: KBuffer.() -> Unit): ByteArrayKBuffer {
    val buf = KBuffer.array(capacity)
    buf.block()
    buf.flip()
    return buf
}

fun KBuffer.Companion.arrayCopy(src: ReadMemory, srcOffset: Int = 0, length: Int = src.limit - srcOffset) = array(length).apply { setBytes(0, src, srcOffset, length) }

fun Random.nextBytes(dst: Writeable, len: Int = dst.remaining) {
    val buffer = ByteArray(min(len, 64))
    var remaining = len
    while (remaining > 0) {
        val count = min(remaining, 64)
        nextBytes(buffer, 0, count)
        dst.putBytes(buffer, 0, count)
        remaining -= count
    }
}

val KBuffer.absPosition: Int get() = offset + position

inline fun <R> KBuffer.view(index: Int = position, length: Int = limit - index, block: () -> R): R {
    val offsetMark = offset
    val positionMark = position
    val limitMark = limit
    offset(index)
    limit = length
    try {
        return block()
    } finally {
        offset(offsetMark)
        position = positionMark
        limit = limitMark
    }
}
