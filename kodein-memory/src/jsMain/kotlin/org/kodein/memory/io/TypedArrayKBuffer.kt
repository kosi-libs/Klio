package org.kodein.memory.io

import org.khronos.webgl.ArrayBuffer
import org.khronos.webgl.DataView
import org.khronos.webgl.Int8Array

class TypedArrayKBuffer(val buffer: ArrayBuffer) : AbstractKBuffer(buffer.byteLength) {

    private val array = Int8Array(buffer, 0, capacity)
    private val data = DataView(buffer, 0, capacity)

    override val implementation: String get() = "TypedArrayKBuffer"

    override fun createDuplicate() = TypedArrayKBuffer(buffer)

    override fun unsafeSetBytes(index: Int, src: ByteArray, srcOffset: Int, length: Int) {
        if (srcOffset == 0 && length == src.size) {
            array.set(src.unsafeCast<Array<Byte>>(), index)
        } else {
            src.copyInto(array.unsafeCast<ByteArray>(), index, srcOffset, srcOffset + length)
        }
    }

    override fun unsafeTrySetBytesOptimized(index: Int, src: AbstractKBuffer, srcOffset: Int, length: Int): Boolean {
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

    override fun unsafeGetBytes(index: Int, dst: ByteArray, dstOffset: Int, length: Int) {
        array.unsafeCast<ByteArray>().copyInto(dst, dstOffset, index, index + length)
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

    override fun tryEqualsOptimized(index: Int, other: AbstractKBuffer, otherIndex: Int, length: Int): Boolean? = null

    override fun backingArray(): ByteArray? = null
}
