package org.kodein.memory

class ByteArrayKBuffer private constructor(val array: ByteArray, val offset: Int, capacity: Int) : AbstractKBuffer(capacity) {

    constructor(array: ByteArray) : this(array, 0, array.size)

    override fun createDuplicate() = ByteArrayKBuffer(array, offset, capacity)

    override fun slice(): KBuffer = ByteArrayKBuffer(array, offset + position, limit - position)

    override fun unsafeWriteBytes(src: ByteArray, offset: Int, length: Int) {
        src.copyInto(array, destinationOffset = this.offset + position, startIndex = offset, endIndex = offset + length)
    }

    override fun unsafeTryWriteAllOptimized(src: Readable, length: Int): Boolean {
        if (src !is ByteArrayKBuffer) return false
        unsafeWriteBytes(src.array, 0, length)
        return true
    }

    override fun unsafeSet(index: Int, value: Byte) { array[offset + position + index] = value }

    override fun unsafeSetShort(index: Int, value: Short) {
        slowStoreShort(value) { i, b -> array[offset + position + index + i] = b }
    }

    override fun unsafeSetInt(index: Int, value: Int) {
        slowStoreInt(value) { i, b -> array[offset + position + index + i] = b }
    }

    override fun unsafeSetLong(index: Int, value: Long) {
        slowStoreLong(value) { i, b -> array[offset + position + index + i] = b }
    }

    override fun unsafeReadBytes(dst: ByteArray, offset: Int, length: Int) {
        array.copyInto(dst, destinationOffset = offset, startIndex = this.offset + position, endIndex = this.offset + position + length)
    }

    override fun unsafeGet(index: Int): Byte = array[offset + position + index]

    override fun unsafeGetShort(index: Int): Short = slowLoadShort { array[offset + position + index + it] }

    override fun unsafeGetInt(index: Int): Int = slowLoadInt { array[offset + position + index + it] }

    override fun unsafeGetLong(index: Int): Long = slowLoadLong { array[offset + position + index + it] }
}
