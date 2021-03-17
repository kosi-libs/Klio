package org.kodein.memory.io

import java.nio.ByteBuffer

public class DirectByteBufferMemory(public val byteBuffer: ByteBuffer) : AbstractMemory() {

    init {
        require(byteBuffer.isDirect) { "ByteBuffer is not direct." }
    }

    override val size: Int get() = byteBuffer.limit()

    override fun unsafeSlice(index: Int, length: Int): AbstractMemory =
        byteBuffer.position(index) {
            val slice = byteBuffer.slice()
            slice.limit(length)
            DirectByteBufferMemory(slice)
        }

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

    private inline fun <R> ByteBuffer.limit(newLimit: Int, block: () -> R): R = tmp<R>(ByteBuffer::limit, { limit(it) }, newLimit, block)
    private inline fun <R> ByteBuffer.position(newPosition: Int, block: () -> R): R = tmp<R>(ByteBuffer::position, { position(it) }, newPosition, block)

    override fun unsafeSetBytes(index: Int, src: ByteArray, srcOffset: Int, length: Int) {
        byteBuffer.position(index) {
            byteBuffer.put(src, srcOffset, length)
        }
    }

    override fun unsafeTrySetBytesOptimized(index: Int, src: AbstractMemory, srcOffset: Int, length: Int): Boolean {
        if (src !is DirectByteBufferMemory || byteBuffer === src.byteBuffer) return false

        byteBuffer.position(index) {
            src.byteBuffer.position(srcOffset) {
                src.byteBuffer.limit(srcOffset + length) {
                    byteBuffer.put(src.byteBuffer)
                }
            }
        }

        return true
    }

    override fun unsafeSetByte(index: Int, value: Byte) {
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

    override fun unsafeGetByte(index: Int): Byte = byteBuffer.get(index)

    override fun unsafeGetShort(index: Int): Short = byteBuffer.getShort(index)

    override fun unsafeGetInt(index: Int): Int = byteBuffer.getInt(index)

    override fun unsafeGetLong(index: Int): Long = byteBuffer.getLong(index)

    override fun unsafeGetBytes(index: Int, dst: ByteArray, dstOffset: Int, length: Int) {
        byteBuffer.position(index) {
            byteBuffer.get(dst, dstOffset, length)
        }
    }

    override fun unsafeTryEqualsOptimized(other: AbstractMemory): Boolean? {
        if (other !is DirectByteBufferMemory || byteBuffer === other.byteBuffer) return null

        return byteBuffer == other.byteBuffer
    }
}
