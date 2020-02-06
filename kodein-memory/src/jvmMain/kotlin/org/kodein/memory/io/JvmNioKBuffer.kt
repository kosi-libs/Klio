package org.kodein.memory.io

import java.nio.ByteBuffer

class JvmNioKBuffer(val byteBuffer: ByteBuffer) : AbstractKBuffer(byteBuffer.capacity()) {

    val hasArray get() = byteBuffer.hasArray()
    val isDirect get() = byteBuffer.isDirect

    override val name: String get() = "JvmNioKBuffer"

    override fun createDuplicate() = JvmNioKBuffer(byteBuffer)

    private inline fun <R> ByteBuffer.tmp(g: ByteBuffer.() -> Int, s: ByteBuffer.(Int) -> Unit, new: Int, block: () -> R): R {
        val old = g()
        if (old == new) return block()

        s(new)
        try {
            return block()
        } finally {
            s(old)
        }
    }

    private inline fun <R> ByteBuffer.limit(newLimit: Int, block: () -> R): R = tmp(ByteBuffer::limit, ByteBuffer::limit, newLimit, block)
    private inline fun <R> ByteBuffer.position(newPosition: Int, block: () -> R): R = tmp(ByteBuffer::position, ByteBuffer::position, newPosition, block)


    override fun unsafeSetBytes(index: Int, src: ByteArray, srcOffset: Int, length: Int) {
        byteBuffer.position(index) {
            byteBuffer.put(src, srcOffset, length)
        }
    }

    override fun unsafeTrySetBytesOptimized(index: Int, src: AbstractKBuffer, srcOffset: Int, length: Int): Boolean {
        if (src !is JvmNioKBuffer || byteBuffer === src.byteBuffer) return false

        byteBuffer.position(index) {
            src.byteBuffer.position(srcOffset) {
                src.byteBuffer.limit(srcOffset + length) {
                    byteBuffer.put(src.byteBuffer)
                }
            }
        }

        return true
    }

    override fun unsafeSet(index: Int, value: Byte) {
        byteBuffer.put(index, value)
    }

    override fun unsafeSetShort(index: Int, value: Short) {
        byteBuffer.putShort(index, value)
    }

    override fun unsafeSetInt(index: Int, value: Int) {
        byteBuffer.putInt(index, value)
    }

    override fun unsafeSetLong(index: Int, value: Long) {
        byteBuffer.putLong(index, value)
    }

    override fun unsafeGet(index: Int): Byte = byteBuffer.get(index)

    override fun unsafeGetShort(index: Int): Short = byteBuffer.getShort(index)

    override fun unsafeGetInt(index: Int): Int = byteBuffer.getInt(index)

    override fun unsafeGetLong(index: Int): Long = byteBuffer.getLong(index)

    override fun unsafeGetBytes(index: Int, dst: ByteArray, dstOffset: Int, length: Int) {
        byteBuffer.position(index) {
            byteBuffer.get(dst, dstOffset, length)
        }
    }

    override fun tryEqualsOptimized(index: Int, other: AbstractKBuffer, otherIndex: Int, length: Int): Boolean? {
        if (other !is JvmNioKBuffer || byteBuffer === other.byteBuffer) return null

        byteBuffer.position(index) {
            byteBuffer.limit(index + length) {
                other.byteBuffer.position(otherIndex) {
                    other.byteBuffer.limit(otherIndex + length) {
                        return byteBuffer == other.byteBuffer
                    }
                }
            }
        }
    }

    override fun backingArray(): ByteArray? = if (byteBuffer.hasArray()) byteBuffer.array() else null
}
