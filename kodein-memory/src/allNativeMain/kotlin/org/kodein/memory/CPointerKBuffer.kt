package org.kodein.memory

import kotlinx.cinterop.*
import platform.posix.memcpy

@Suppress("ConstantConditionIf")
class CPointerKBuffer(val pointer: CPointer<ByteVar>, capacity: Int) : AbstractKBuffer(capacity) {

    override fun createDuplicate() = CPointerKBuffer(pointer, capacity)

    override fun slice(): KBuffer = CPointerKBuffer((pointer + position)!!, limit - position)

    override fun unsafeWriteBytes(src: ByteArray, offset: Int, length: Int) {
        memcpy((pointer + position)!!, src.refTo(offset), length.convert())
    }

    override fun unsafeTryWriteAllOptimized(src: Readable, length: Int): Boolean {
        if (src !is CPointerKBuffer) return false
        memcpy((pointer + position)!!, (src.pointer + src.position)!!, length.convert())
        return true
    }

    override fun unsafeSet(index: Int, value: Byte) {
        pointer[position + index] = value
    }

    override fun unsafeSetShort(index: Int, value: Short) {
        if (unalignedAccessAllowed) {
            (pointer + position + index)!!.reinterpret<ShortVar>().pointed.value = value.toBigEndian()
        } else {
            slowStoreShort(value) { i, b -> pointer[position + index + i] = b }
        }
    }

    override fun unsafeSetInt(index: Int, value: Int) {
        if (unalignedAccessAllowed) {
            (pointer + position + index)!!.reinterpret<IntVar>().pointed.value = value.toBigEndian()
        } else {
            slowStoreInt(value) { i, b -> pointer[position + index + i] = b }
        }
    }

    override fun unsafeSetLong(index: Int, value: Long) {
        if (unalignedAccessAllowed) {
            (pointer + position + index)!!.reinterpret<LongVar>().pointed.value = value.toBigEndian()
        } else {
            slowStoreLong(value) { i, b -> pointer[position + index + i] = b }
        }
    }

    override fun unsafeReadBytes(dst: ByteArray, offset: Int, length: Int) {
        memcpy(dst.refTo(offset), (pointer + position)!!, length.convert())
    }

    override fun unsafeGet(index: Int): Byte = pointer[position + index]

    override fun unsafeGetShort(index: Int): Short =
            if (unalignedAccessAllowed) {
                (pointer + position + index)!!.reinterpret<ShortVar>().pointed.value.toBigEndian()
            } else {
                slowLoadShort { pointer[position + index + it] }
            }

    override fun unsafeGetInt(index: Int): Int =
            if (unalignedAccessAllowed) {
                (pointer + position + index)!!.reinterpret<IntVar>().pointed.value.toBigEndian()
            } else {
                slowLoadInt { pointer[position + index + it] }
            }

    override fun unsafeGetLong(index: Int): Long =
            if (unalignedAccessAllowed) {
                (pointer + position + index)!!.reinterpret<LongVar>().pointed.value.toBigEndian()
            } else {
                slowLoadLong { pointer[position + index + it] }
            }
}
