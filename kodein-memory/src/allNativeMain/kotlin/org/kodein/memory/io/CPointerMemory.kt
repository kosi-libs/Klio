package org.kodein.memory.io

import kotlinx.cinterop.*
import platform.posix.memcmp
import platform.posix.memcpy
import platform.posix.memset

@Suppress("ConstantConditionIf")
public class CPointerMemory(public val pointer: CPointer<ByteVar>, override val size: Int) : AbstractMemory<CPointerMemory>() {

    override fun unsafeSlice(index: Int, length: Int): CPointerMemory =
        CPointerMemory((pointer + index)!!, length)

    override fun unsafePutBytes(index: Int, src: ByteArray, srcOffset: Int, length: Int) {
        memcpy((pointer + index)!!, src.refTo(srcOffset), length.convert())
    }

    override fun unsafeTryPutBytesOptimized(index: Int, src: AbstractMemory<*>): Boolean {
        if (src !is CPointerMemory) return false
        memcpy((pointer + index)!!, src.pointer, src.size.convert())
        return true
    }

    override fun unsafePutByte(index: Int, value: Byte) {
        pointer[index] = value
    }

    override fun unsafePutShort(index: Int, value: Short) {
        if (unalignedAccessAllowed) {
            (pointer + index)!!.reinterpret<ShortVar>().pointed.value = value.toBigEndian()
        } else {
            slowStoreShort(value) { i, b -> pointer[index + i] = b }
        }
    }

    override fun unsafePutInt(index: Int, value: Int) {
        if (unalignedAccessAllowed) {
            (pointer + index)!!.reinterpret<IntVar>().pointed.value = value.toBigEndian()
        } else {
            slowStoreInt(value) { i, b -> pointer[index + i] = b }
        }
    }

    override fun unsafePutLong(index: Int, value: Long) {
        if (unalignedAccessAllowed) {
            (pointer + index)!!.reinterpret<LongVar>().pointed.value = value.toBigEndian()
        } else {
            slowStoreLong(value) { i, b -> pointer[index + i] = b }
        }
    }

    override fun unsafeGetBytes(index: Int, dst: ByteArray, dstOffset: Int, length: Int) {
        memcpy(dst.refTo(dstOffset), (pointer + index)!!, length.convert())
    }

    override fun unsafeGetByte(index: Int): Byte = pointer[index]

    override fun unsafeGetShort(index: Int): Short =
            if (unalignedAccessAllowed) {
                (pointer + index)!!.reinterpret<ShortVar>().pointed.value.toBigEndian()
            } else {
                slowLoadShort { pointer[index + it] }
            }

    override fun unsafeGetInt(index: Int): Int =
            if (unalignedAccessAllowed) {
                (pointer + index)!!.reinterpret<IntVar>().pointed.value.toBigEndian()
            } else {
                slowLoadInt { pointer[index + it] }
            }

    override fun unsafeGetLong(index: Int): Long =
            if (unalignedAccessAllowed) {
                (pointer + index)!!.reinterpret<LongVar>().pointed.value.toBigEndian()
            } else {
                slowLoadLong { pointer[index + it] }
            }

    override fun unsafeTryEqualsOptimized(other: AbstractMemory<*>): Boolean? =
        when (other) {
            is CPointerMemory -> memcmp(pointer, other.pointer, size.convert()) == 0
            is ByteArrayMemory -> other.array.usePinned { pinned -> memcmp(pointer, pinned.addressOf(other.offset), size.convert()) == 0 }
            else -> null
        }

    override fun fill(byte: Byte) {
        memset(pointer, byte.toInt(), size.convert())
    }
}
