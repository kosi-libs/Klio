package org.kodein.memory

import kotlinx.cinterop.*
import platform.posix.memcmp
import platform.posix.memcpy

@Suppress("ConstantConditionIf")
class CPointerKBuffer(val pointer: CPointer<ByteVar>, capacity: Int) : AbstractKBuffer(capacity) {

    override val name: String get() = "CPointerKBuffer"

    override fun createDuplicate() = CPointerKBuffer(pointer, capacity)

    override fun unsafeView(index: Int, length: Int) = CPointerKBuffer((pointer + index)!!, length)

    override fun unsafeSetBytes(index: Int, src: ByteArray, offset: Int, length: Int) {
        memcpy((pointer + index)!!, src.refTo(offset), length.convert())
    }

    override fun unsafeTrySetBytesOptimized(index: Int, src: ReadBuffer, srcIndex: Int, length: Int): Boolean {
        if (src !is CPointerKBuffer) return false
        memcpy((pointer + index)!!, (src.pointer + srcIndex)!!, length.convert())
        return true
    }

    override fun unsafeSet(index: Int, value: Byte) {
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

    override fun unsafeGetBytes(index: Int, dst: ByteArray, offset: Int, length: Int) {
        memcpy(dst.refTo(offset), (pointer + index)!!, length.convert())
    }

    override fun unsafeGet(index: Int): Byte = pointer[index]

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

    override fun tryEqualsOptimized(other: KBuffer): Boolean? {
        if (other !is CPointerKBuffer) return null

        return memcmp(pointer, other.pointer, remaining.convert()) == 0
    }
}
