package org.kodein.memory.io

import org.khronos.webgl.ArrayBuffer
import org.khronos.webgl.ArrayBufferView
import org.khronos.webgl.DataView
import org.khronos.webgl.Uint8Array

public class ArrayBufferMemory(public val data: DataView) : AbstractMemory<ArrayBufferMemory>() {
    public constructor(array: ArrayBufferView) : this(DataView(array.buffer, array.byteOffset, array.byteLength))
    public constructor(buffer: ArrayBuffer) : this(DataView(buffer, 0, buffer.byteLength))
    public constructor(size: Int) : this(DataView(ArrayBuffer(size), 0, size))

    public val uint8Array: Uint8Array get() = Uint8Array(data.buffer, data.byteOffset, data.byteLength)

    override val size: Int get() = data.byteLength

    override fun unsafeSlice(index: Int, length: Int): ArrayBufferMemory =
        ArrayBufferMemory(DataView(data.buffer, data.byteOffset + index, length))

    override fun unsafePutBytes(index: Int, src: ByteArray, srcOffset: Int, length: Int) {
        if (srcOffset == 0 && length == src.size) {
            uint8Array.set(src.unsafeCast<Array<Byte>>(), index)
        } else {
            src.copyInto(uint8Array.unsafeCast<ByteArray>(), index, srcOffset, srcOffset + length)
        }
    }

    override fun unsafeTryPutBytesOptimized(index: Int, src: AbstractMemory<*>): Boolean {
        if (src !is ArrayBufferMemory) return false

        uint8Array.set(src.uint8Array, index)

        return true
    }

    override fun unsafePutByte(index: Int, value: Byte) {
        data.setInt8(index, value)
    }

    override fun unsafePutShort(index: Int, value: Short) {
        data.setInt16(index, value)
    }

    override fun unsafePutInt(index: Int, value: Int) {
        data.setInt32(index, value)
    }

    override fun unsafePutLong(index: Int, value: Long) {
        data.setInt32(index + 0, (value ushr 0x20 and 0xFFFFFFFF).toInt())
        data.setInt32(index + 4, (value ushr 0x00 and 0xFFFFFFFF).toInt())
    }

    override fun unsafeGetBytes(index: Int, dst: ByteArray, dstOffset: Int, length: Int) {
        uint8Array.unsafeCast<ByteArray>().copyInto(dst, dstOffset, index, index + length)
    }

    override fun unsafeGetByte(index: Int): Byte = data.getInt8(index)

    override fun unsafeGetShort(index: Int): Short = data.getInt16(index)

    override fun unsafeGetInt(index: Int): Int = data.getInt32(index)

    override fun unsafeGetLong(index: Int): Long {
        return (
                (data.getInt32(index + 0).toLong() and 0xFFFFFFFF shl 0x20) or
                (data.getInt32(index + 4).toLong() and 0xFFFFFFFF shl 0x00)
        )
    }

    override fun unsafeTryEqualsOptimized(other: AbstractMemory<*>): Boolean? = null

    override fun fill(byte: Byte) {
        repeat(data.byteLength) {
            data.setInt8(it, byte)
        }
    }

    public companion object {
        public fun copyOf(memory: ReadMemory): ArrayBufferMemory =
            ArrayBufferMemory(memory.size).also { it.putBytes(0, memory) }
    }
}
