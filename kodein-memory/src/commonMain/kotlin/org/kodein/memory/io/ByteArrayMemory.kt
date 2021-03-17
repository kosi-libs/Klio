package org.kodein.memory.io

public class ByteArrayMemory constructor(public val array: ByteArray, public val offset: Int = 0, override val size: Int = array.size - offset) : AbstractMemory() {

    override fun unsafeSlice(index: Int, length: Int): AbstractMemory =
        ByteArrayMemory(array, offset + index, length)

    override fun unsafeSetBytes(index: Int, src: ByteArray, srcOffset: Int, length: Int) {
        src.copyInto(array, destinationOffset = offset + index, startIndex = srcOffset, endIndex = srcOffset + length)
    }

    override fun unsafeTrySetBytesOptimized(index: Int, src: AbstractMemory, srcOffset:Int, length: Int): Boolean = false

    override fun unsafeSetByte(index: Int, value: Byte) {
        array[offset + index] = value
    }

    override fun unsafeSetShort(index: Int, value: Short) {
        slowStoreShort(value) { i, b -> array[offset + index + i] = b }
    }

    override fun unsafeSetInt(index: Int, value: Int) {
        slowStoreInt(value) { i, b -> array[offset + index + i] = b }
    }

    override fun unsafeSetLong(index: Int, value: Long) {
        slowStoreLong(value) { i, b -> array[offset + index + i] = b }
    }

    override fun unsafeGetBytes(index: Int, dst: ByteArray, dstOffset: Int, length: Int) {
        array.copyInto(dst, destinationOffset = dstOffset, startIndex = offset + index, endIndex = offset + index + length)
    }

    override fun unsafeGetByte(index: Int): Byte = array[offset + index]

    override fun unsafeGetShort(index: Int): Short = slowLoadShort { array[offset + index + it] }

    override fun unsafeGetInt(index: Int): Int = slowLoadInt { array[offset + index + it] }

    override fun unsafeGetLong(index: Int): Long = slowLoadLong { array[offset + index + it] }

    override fun unsafeTryEqualsOptimized(other: AbstractMemory): Boolean? {
        if (offset == 0 && size == array.size && other is ByteArrayMemory && other.offset == 0 && size == other.array.size) {
            return array.contentEquals(other.array)
        }
        return null
    }

}
