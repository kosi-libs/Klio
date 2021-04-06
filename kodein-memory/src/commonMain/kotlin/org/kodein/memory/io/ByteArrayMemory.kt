package org.kodein.memory.io

public class ByteArrayMemory constructor(public val array: ByteArray, public val offset: Int = 0, override val size: Int = array.size - offset) : AbstractMemory<ByteArrayMemory>() {

    override fun unsafeSlice(index: Int, length: Int): ByteArrayMemory =
        ByteArrayMemory(array, offset + index, length)

    override fun unsafePutBytes(index: Int, src: ByteArray, srcOffset: Int, length: Int) {
        src.copyInto(array, destinationOffset = offset + index, startIndex = srcOffset, endIndex = srcOffset + length)
    }

    override fun unsafeTryPutBytesOptimized(index: Int, src: AbstractMemory<*>): Boolean = false

    override fun unsafePutByte(index: Int, value: Byte) {
        array[offset + index] = value
    }

    override fun unsafePutShort(index: Int, value: Short) {
        slowStoreShort(value) { i, b -> array[offset + index + i] = b }
    }

    override fun unsafePutInt(index: Int, value: Int) {
        slowStoreInt(value) { i, b -> array[offset + index + i] = b }
    }

    override fun unsafePutLong(index: Int, value: Long) {
        slowStoreLong(value) { i, b -> array[offset + index + i] = b }
    }

    override fun unsafeGetBytes(index: Int, dst: ByteArray, dstOffset: Int, length: Int) {
        array.copyInto(dst, destinationOffset = dstOffset, startIndex = offset + index, endIndex = offset + index + length)
    }

    override fun unsafeGetByte(index: Int): Byte = array[offset + index]

    override fun unsafeGetShort(index: Int): Short = slowLoadShort { array[offset + index + it] }

    override fun unsafeGetInt(index: Int): Int = slowLoadInt { array[offset + index + it] }

    override fun unsafeGetLong(index: Int): Long = slowLoadLong { array[offset + index + it] }

    override fun unsafeTryEqualsOptimized(other: AbstractMemory<*>): Boolean? {
        if (offset == 0 && size == array.size && other is ByteArrayMemory && other.offset == 0 && size == other.array.size) {
            return array.contentEquals(other.array)
        }
        return null
    }

    override fun fill(byte: Byte) {
        array.fill(byte, offset, offset + size)
    }
}
