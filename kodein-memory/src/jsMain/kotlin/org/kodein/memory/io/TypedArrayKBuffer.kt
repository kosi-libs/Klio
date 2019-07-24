package org.kodein.memory.io

import org.khronos.webgl.*

class TypedArrayKBuffer(val buffer: ArrayBuffer, val offset: Int = 0, capacity: Int = buffer.byteLength - offset) : AbstractKBuffer(capacity) {

    private val array = Int8Array(buffer, offset, capacity)
    private val data = DataView(buffer, offset, capacity)

    override val name: String get() = "TypedArrayKBuffer"

    override fun createDuplicate() = TypedArrayKBuffer(buffer, offset, capacity)

    override fun unsafeView(index: Int, length: Int) = TypedArrayKBuffer(buffer, offset + index, length)

    override fun unsafeSetBytes(index: Int, src: ByteArray, srcOffset: Int, length: Int) {
        if (srcOffset == 0 && length == src.size) {
            array.set(src.unsafeCast<Array<Byte>>(), index)
        } else {
            src.copyInto(array.unsafeCast<ByteArray>(), index, srcOffset, srcOffset + length)
        }
    }

    override fun unsafeTrySetBytesOptimized(index: Int, src: ReadBuffer, srcOffset: Int, length: Int): Boolean {
        if (src !is TypedArrayKBuffer) return false

        if (srcOffset == 0 && length == src.array.length) {
            array.set(src.array, index)
        } else {
            array.set(src.array.subarray(srcOffset, srcOffset + length), index)
        }

        return true
    }

    override fun unsafeSet(index: Int, value: Byte) {
        data.setInt8(index, value)
    }

    override fun unsafeSetShort(index: Int, value: Short) {
        data.setInt16(index, value)
    }

    override fun unsafeSetInt(index: Int, value: Int) {
        data.setInt32(index, value)
    }

    override fun unsafeSetLong(index: Int, value: Long) {
        data.setInt32(index + 0, (value ushr 0x20 and 0xFFFFFFFF).toInt())
        data.setInt32(index + 4, (value ushr 0x00 and 0xFFFFFFFF).toInt())
    }

    override fun unsafeGetBytes(index: Int, dst: ByteArray, offset: Int, length: Int) {
        array.unsafeCast<ByteArray>().copyInto(dst, offset, index, index + length)
    }

    override fun unsafeGet(index: Int): Byte = data.getInt8(index)

    override fun unsafeGetShort(index: Int): Short = data.getInt16(index)

    override fun unsafeGetInt(index: Int): Int = data.getInt32(index)

    override fun unsafeGetLong(index: Int): Long {
        return (
                (data.getInt32(index + 0).toLong() and 0xFFFFFFFF shl 0x20) or
                (data.getInt32(index + 4).toLong() and 0xFFFFFFFF shl 0x00)
        )
    }

    override fun tryEqualsOptimized(other: KBuffer): Boolean? = null
}
