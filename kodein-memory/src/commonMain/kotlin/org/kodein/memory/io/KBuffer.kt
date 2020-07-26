package org.kodein.memory.io

import kotlin.math.min
import kotlin.random.Random

public interface KBuffer : WriteBuffer, ReadBuffer {

    public val offset: Int

    public val capacity: Int

    override var limit: Int

    public fun offset(newOffset: Int)

    public fun reset()
    public fun flip()

    override fun duplicate(): KBuffer
    override fun slice(): KBuffer
    override fun slice(index: Int, length: Int): KBuffer

    override fun internalBuffer(): KBuffer

    public fun backingArray(): ByteArray?

    public companion object
}

@Suppress("NOTHING_TO_INLINE")
public inline fun KBuffer.limitHere() {
    limit = position
}

public fun KBuffer.Companion.wrap(array: ByteArray, offset: Int = 0, limit: Int = array.size - offset): ByteArrayKBuffer =
        ByteArrayKBuffer(array).also {
            it.offset(offset)
            it.limit = limit
        }

public fun KBuffer.Companion.array(capacity: Int): ByteArrayKBuffer = ByteArrayKBuffer(ByteArray(capacity))

public inline fun KBuffer.Companion.array(capacity: Int, block: KBuffer.() -> Unit): ByteArrayKBuffer {
    val buf = KBuffer.array(capacity)
    buf.block()
    buf.flip()
    return buf
}

public fun KBuffer.Companion.arrayCopy(src: ReadMemory, srcOffset: Int = 0, length: Int = src.limit - srcOffset): ByteArrayKBuffer = array(length).apply { setBytes(0, src, srcOffset, length) }

public fun Random.nextBytes(dst: Writeable, len: Int = dst.available) {
    val buffer = ByteArray(min(len, 64))
    var available = len
    while (available > 0) {
        val count = min(available, 64)
        nextBytes(buffer, 0, count)
        dst.putBytes(buffer, 0, count)
        available -= count
    }
}

public val KBuffer.absPosition: Int get() = offset + position

public inline fun <R> KBuffer.view(index: Int = position, length: Int = limit - index, block: () -> R): R {
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
