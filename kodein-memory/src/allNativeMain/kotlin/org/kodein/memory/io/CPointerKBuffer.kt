package org.kodein.memory.io

import kotlinx.cinterop.*
import platform.posix.memcmp
import platform.posix.memcpy

@Suppress("ConstantConditionIf")
class CPointerKBuffer(val pointer: CPointer<ByteVar>, capacity: Int) : AbstractKBuffer(capacity) {

    override val implementation: String get() = "CPointerKBuffer"

    override fun createDuplicate() = CPointerKBuffer(pointer, capacity)

    override fun unsafeSetBytes(index: Int, src: ByteArray, srcOffset: Int, length: Int) {
        memcpy((pointer + index)!!, src.refTo(srcOffset), length.convert())
    }

    override fun unsafeTrySetBytesOptimized(index: Int, src: AbstractKBuffer, srcOffset: Int, length: Int): Boolean {
        if (src !is CPointerKBuffer) return false
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

    override fun tryEqualsOptimized(index: Int, other: AbstractKBuffer, otherIndex: Int, length: Int): Boolean? {
        if (other !is CPointerKBuffer) return null

        return memcmp(pointer + index, other.pointer + otherIndex, length.convert()) == 0
    }

    override fun backingArray(): ByteArray? = null
}
