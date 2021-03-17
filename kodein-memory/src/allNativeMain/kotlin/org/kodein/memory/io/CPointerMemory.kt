package org.kodein.memory.io

import kotlinx.cinterop.*
import platform.posix.memcmp
import platform.posix.memcpy

@Suppress("ConstantConditionIf")
public class CPointerMemory(public val pointer: CPointer<ByteVar>, override val size: Int) : AbstractMemory() {

    override fun unsafeSlice(index: Int, length: Int): AbstractMemory =
        CPointerMemory((pointer + index)!!, length)

    override fun unsafeSetBytes(index: Int, src: ByteArray, srcOffset: Int, length: Int) {
        memcpy((pointer + index)!!, src.refTo(srcOffset), length.convert())
    }

    override fun unsafeTrySetBytesOptimized(index: Int, src: AbstractMemory, srcOffset: Int, length: Int): Boolean {
        if (src !is CPointerMemory) return false
        memcpy((pointer + index)!!, (src.pointer + srcOffset)!!, length.convert())
        return true
    }

    override fun unsafeSetByte(index: Int, value: Byte) {
        pointer[index] = value
    }

    override fun unsafeSetShort(index: Int, value: Short) {
        if (unalignedAccessAllowed) {
            (pointer + index)!!.reinterpret<ShortVar>().pointed.value = value.toBigEndian()
        } else {
            slowStoreShort(value) { i, b -> pointer[index + i] = b }
        }
    }

    override fun unsafeSetInt(index: Int, value: Int) {
        if (unalignedAccessAllowed) {
            (pointer + index)!!.reinterpret<IntVar>().pointed.value = value.toBigEndian()
        } else {
            slowStoreInt(value) { i, b -> pointer[index + i] = b }
        }
    }

    override fun unsafeSetLong(index: Int, value: Long) {
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

    override fun unsafeTryEqualsOptimized(other: AbstractMemory): Boolean? =
        when (other) {
            is CPointerMemory -> memcmp(pointer, other.pointer, size.convert()) == 0
            is ByteArrayMemory -> other.array.usePinned { pinned -> memcmp(pointer, pinned.addressOf(other.offset), size.convert()) == 0 }
            else -> null
        }
}
