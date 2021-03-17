package org.kodein.memory.io

@Suppress("DuplicatedCode")
public abstract class AbstractMemory : Memory {

    protected fun ReadMemory.requireSizeAt(index: Int, needed: Int) {
        require(index >= 0) { "index: $index < 0." }
        if (index + needed > size) throw IOException("Needed at least ${index + needed} total bytes, but memory size is $size bytes.")
    }

    protected fun ByteArray.requireSizeAt(index: Int, needed: Int) {
        require(index >= 0) { "index: $index < 0." }
        if (index + needed > this.size) throw IOException("Needed at least ${index + needed} bytes, but array size is $size bytes.")
    }

    protected abstract fun unsafeSlice(index: Int, length: Int): AbstractMemory

    final override fun slice(index: Int, length: Int): Memory {
        require(index >= 0) { "index: $index < 0" }
        require(length >= 0) { "length: $length < 0" }
        requireSizeAt(index, length)

        if (index == 0 && length == size) return this

        return unsafeSlice(index, length)
    }

    final override fun setByte(index: Int, value: Byte) {
        requireSizeAt(index, Byte.SIZE_BYTES)
        unsafeSetByte(index, value)
    }

    protected abstract fun unsafeSetByte(index: Int, value: Byte)

    final override fun setShort(index: Int, value: Short) {
        requireSizeAt(index, Short.SIZE_BYTES)
        unsafeSetShort(index, value)
    }

    protected abstract fun unsafeSetShort(index: Int, value: Short)

    final override fun setInt(index: Int, value: Int) {
        requireSizeAt(index, Int.SIZE_BYTES)
        unsafeSetInt(index, value)
    }

    protected abstract fun unsafeSetInt(index: Int, value: Int)

    final override fun setLong(index: Int, value: Long) {
        requireSizeAt(index, Long.SIZE_BYTES)
        unsafeSetLong(index, value)
    }

    protected abstract fun unsafeSetLong(index: Int, value: Long)

    final override fun setBytes(index: Int, src: ByteArray, srcOffset: Int, length: Int) {
        require(srcOffset >= 0) { "srcOffset: $srcOffset < 0" }
        require(length >= 0) { "length: $length < 0" }
        src.requireSizeAt(srcOffset, length)
        requireSizeAt(index, length)
        if (length == 0) return
        unsafeSetBytes(index, src, srcOffset, length)
    }

    protected abstract fun unsafeSetBytes(index: Int, src: ByteArray, srcOffset: Int, length: Int)

    protected abstract fun unsafeTrySetBytesOptimized(index: Int, src: AbstractMemory, srcOffset: Int, length: Int): Boolean

    final override fun setBytes(index: Int, src: ReadMemory, srcOffset: Int, length: Int) {
        require(length >= 0) { "length: $length < 0" }
        src.requireSizeAt(srcOffset, length)
        requireSizeAt(index, length)
        if (length == 0) return
        val srcMemory = src.internalMemory()
        val hasOptimized =
            when (srcMemory) {
                is ByteArrayMemory -> {
                    unsafeSetBytes(index, srcMemory.array, srcMemory.offset + srcOffset, length)
                    true
                }
                is AbstractMemory -> unsafeTrySetBytesOptimized(index, srcMemory, srcOffset, length)
                else -> false
            }
        if (!hasOptimized) {
            repeat (length) {
                unsafeSetByte(index + it, srcMemory.getByte(srcOffset + it))
            }
        }
    }

    final override fun setBytes(index: Int, src: Readable, length: Int) {
        require(length >= 0) { "length: $length < 0" }
        requireSizeAt(index, length)
        if (length == 0) return
        src.requestCanRead(length)
        val hasOptimized =
            when (val srcMemory = (src as? MemoryReadable)?.memory?.internalMemory()) {
                is ByteArrayMemory -> {
                    unsafeSetBytes(index, srcMemory.array, srcMemory.offset + src.position, length)
                    true
                }
                is AbstractMemory -> unsafeTrySetBytesOptimized(index, srcMemory, src.position, length)
                else -> false
            }
        if (hasOptimized) {
            src.skip(length)
        } else {
            repeat(length) {
                unsafeSetByte(index + it, src.readByte())
            }
        }
    }


    final override fun getByte(index: Int): Byte {
        requireSizeAt(index, Byte.SIZE_BYTES)
        return unsafeGetByte(index)
    }

    protected abstract fun unsafeGetByte(index: Int): Byte

    final override fun getShort(index: Int): Short {
        requireSizeAt(index, Short.SIZE_BYTES)
        return unsafeGetShort(index)
    }

    protected abstract fun unsafeGetShort(index: Int): Short

    final override fun getInt(index: Int): Int {
        requireSizeAt(index, Int.SIZE_BYTES)
        return unsafeGetInt(index)
    }

    protected abstract fun unsafeGetInt(index: Int): Int

    final override fun getLong(index: Int): Long {
        requireSizeAt(index, Long.SIZE_BYTES)
        return unsafeGetLong(index)
    }

    protected abstract fun unsafeGetLong(index: Int): Long

    final override fun getBytes(index: Int, dst: ByteArray, dstOffset: Int, length: Int) {
        require(dstOffset >= 0) { "offset: $dstOffset < 0" }
        require(length >= 0) { "length: $length < 0" }
        dst.requireSizeAt(dstOffset, length)
        requireSizeAt(index, length)
        if (length == 0) return
        unsafeGetBytes(index, dst, dstOffset, length)
    }

    protected abstract fun unsafeGetBytes(index: Int, dst: ByteArray, dstOffset: Int, length: Int)

    override fun internalMemory(): AbstractMemory = this

    override fun equals(other: Any?): Boolean {
        if (other === this) return true
        if (other == null) return false
        if (other !is Memory) return false
        if (other.size != size) return false

        val otherInternalMemory = other.internalMemory()
        val optimized = (otherInternalMemory as? AbstractMemory)?.let {
            unsafeTryEqualsOptimized(otherInternalMemory)
        }
        if (optimized != null) return optimized

        return compareTo(otherInternalMemory) == 0
    }

    protected abstract fun unsafeTryEqualsOptimized(other: AbstractMemory): Boolean?

    final override fun hashCode(): Int {
        var h = 1
        for (i in (size - 1) downTo 0) {
            h = 31 * h + unsafeGetByte(i).toInt()
        }
        return h
    }

    override fun toString(): String = "${this::class.simpleName}($size)"
}
