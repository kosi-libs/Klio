package org.kodein.memory.io

class ByteArrayKBuffer constructor(val array: ByteArray, val offset: Int = 0, capacity: Int = array.size - offset) : AbstractKBuffer(capacity) {

    override val name: String get() = "ByteArrayKBuffer"

    override fun createDuplicate() = ByteArrayKBuffer(array, offset, capacity)

    override fun unsafeView(index: Int, length: Int): KBuffer = ByteArrayKBuffer(array, offset + index, length)

    override fun unsafeSetBytes(index: Int, src: ByteArray, srcOffset: Int, length: Int) {
        src.copyInto(array, destinationOffset = this.offset + index, startIndex = srcOffset, endIndex = srcOffset + length)
    }

    override fun unsafeTrySetBytesOptimized(index: Int, src: ReadBuffer, srcOffset:Int, length: Int): Boolean {
        if (src !is ByteArrayKBuffer) return false
        unsafeSetBytes(index, src.array, srcOffset, length)
        return true
    }

    override fun unsafeSet(index: Int, value: Byte) { array[offset + index] = value }

    override fun unsafeSetShort(index: Int, value: Short) {
        slowStoreShort(value) { i, b -> array[offset + index + i] = b }
    }

    override fun unsafeSetInt(index: Int, value: Int) {
        slowStoreInt(value) { i, b -> array[offset + index + i] = b }
    }

    override fun unsafeSetLong(index: Int, value: Long) {
        slowStoreLong(value) { i, b -> array[offset + index + i] = b }
    }

    override fun unsafeGetBytes(index: Int, dst: ByteArray, offset: Int, length: Int) {
        array.copyInto(dst, destinationOffset = offset, startIndex = this.offset + index, endIndex = this.offset + index + length)
    }

    override fun unsafeGet(index: Int): Byte = array[offset + index]

    override fun unsafeGetShort(index: Int): Short = slowLoadShort { array[offset + index + it] }

    override fun unsafeGetInt(index: Int): Int = slowLoadInt { array[offset + index + it] }

    override fun unsafeGetLong(index: Int): Long = slowLoadLong { array[offset + index + it] }

    override fun tryEqualsOptimized(other: KBuffer): Boolean? = null
}
