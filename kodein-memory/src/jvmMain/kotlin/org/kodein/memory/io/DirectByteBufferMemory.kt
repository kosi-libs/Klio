package org.kodein.memory.io

import java.nio.ByteBuffer

public class DirectByteBufferMemory(byteBuffer: ByteBuffer) : AbstractMemory<DirectByteBufferMemory>() {

    public val byteBuffer: ByteBuffer = byteBuffer.duplicate().also { it.jPosition = 0 }

    init {
        require(byteBuffer.isDirect) { "ByteBuffer is not direct." }
    }

    override val size: Int get() = byteBuffer.jLimit

    override fun unsafeSlice(index: Int, length: Int): DirectByteBufferMemory =
        byteBuffer.withPosition(index) {
            val slice = byteBuffer.slice()
            slice.jLimit = length
            DirectByteBufferMemory(slice)
        }

    override fun unsafePutBytes(index: Int, src: ByteArray, srcOffset: Int, length: Int) {
        byteBuffer.withPosition(index) {
            byteBuffer.put(src, srcOffset, length)
        }
    }

    override fun unsafeTryPutBytesOptimized(index: Int, src: AbstractMemory<*>): Boolean {
        if (src !is DirectByteBufferMemory || byteBuffer === src.byteBuffer) return false

        byteBuffer.withPosition(index) {
            src.byteBuffer.withPosition(0) {
                byteBuffer.put(src.byteBuffer)
            }
        }

        return true
    }

    override fun unsafePutByte(index: Int, value: Byte) {
        byteBuffer.put(index, value)
    }

    override fun unsafePutShort(index: Int, value: Short) {
        byteBuffer.putShort(index, value)
    }

    override fun unsafePutInt(index: Int, value: Int) {
        byteBuffer.putInt(index, value)
    }

    override fun unsafePutLong(index: Int, value: Long) {
        byteBuffer.putLong(index, value)
    }

    override fun unsafeGetByte(index: Int): Byte = byteBuffer.get(index)

    override fun unsafeGetShort(index: Int): Short = byteBuffer.getShort(index)

    override fun unsafeGetInt(index: Int): Int = byteBuffer.getInt(index)

    override fun unsafeGetLong(index: Int): Long = byteBuffer.getLong(index)

    override fun unsafeGetBytes(index: Int, dst: ByteArray, dstOffset: Int, length: Int) {
        byteBuffer.withPosition(index) {
            byteBuffer.get(dst, dstOffset, length)
        }
    }

    override fun unsafeTryEqualsOptimized(other: AbstractMemory<*>): Boolean? {
        if (other !is DirectByteBufferMemory || byteBuffer === other.byteBuffer) return null

        return byteBuffer == other.byteBuffer
    }

    override fun fill(byte: Byte) {
        repeat(size) {
            byteBuffer.put(it, byte)
        }
    }
}
