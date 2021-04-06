package org.kodein.memory.io

@Suppress("DuplicatedCode")
public abstract class AbstractMemory<M : AbstractMemory<M>> : Memory {

    protected fun ReadMemory.requireSizeAt(index: Int, needed: Int) {
        require(index >= 0) { "index: $index < 0." }
        if (index + needed > size) throw IOException("Needed at least ${index + needed} total bytes, but memory size is $size bytes.")
    }

    protected fun ByteArray.requireSizeAt(index: Int, needed: Int) {
        require(index >= 0) { "index: $index < 0." }
        if (index + needed > this.size) throw IOException("Needed at least ${index + needed} bytes, but array size is $size bytes.")
    }

    protected abstract fun unsafeSlice(index: Int, length: Int): M

    final override fun slice(index: Int, length: Int): M {
        require(index >= 0) { "index: $index < 0" }
        require(length >= 0) { "length: $length < 0" }
        requireSizeAt(index, length)

        @Suppress("UNCHECKED_CAST")
        if (index == 0 && length == size) return this as M

        return unsafeSlice(index, length)
    }

    final override fun putByte(index: Int, value: Byte) {
        requireSizeAt(index, Byte.SIZE_BYTES)
        unsafePutByte(index, value)
    }

    protected abstract fun unsafePutByte(index: Int, value: Byte)

    final override fun putShort(index: Int, value: Short) {
        requireSizeAt(index, Short.SIZE_BYTES)
        unsafePutShort(index, value)
    }

    protected abstract fun unsafePutShort(index: Int, value: Short)

    final override fun putInt(index: Int, value: Int) {
        requireSizeAt(index, Int.SIZE_BYTES)
        unsafePutInt(index, value)
    }

    protected abstract fun unsafePutInt(index: Int, value: Int)

    final override fun putLong(index: Int, value: Long) {
        requireSizeAt(index, Long.SIZE_BYTES)
        unsafePutLong(index, value)
    }

    protected abstract fun unsafePutLong(index: Int, value: Long)

    final override fun putBytes(index: Int, src: ByteArray, srcOffset: Int, length: Int) {
        require(srcOffset >= 0) { "srcOffset: $srcOffset < 0" }
        require(length >= 0) { "length: $length < 0" }
        src.requireSizeAt(srcOffset, length)
        requireSizeAt(index, length)
        if (length == 0) return
        unsafePutBytes(index, src, srcOffset, length)
    }

    protected abstract fun unsafePutBytes(index: Int, src: ByteArray, srcOffset: Int, length: Int)

    protected abstract fun unsafeTryPutBytesOptimized(index: Int, src: AbstractMemory<*>): Boolean

    final override fun putBytes(index: Int, src: ReadMemory) {
        requireSizeAt(index, src.size)
        if (src.size == 0) return
        val srcMemory = src.internalMemory()
        val hasOptimized =
            when (srcMemory) {
                is ByteArrayMemory -> {
                    unsafePutBytes(index, srcMemory.array, srcMemory.offset, src.size)
                    true
                }
                is AbstractMemory<*> -> unsafeTryPutBytesOptimized(index, srcMemory)
                else -> false
            }
        if (!hasOptimized) {
            repeat (src.size) {
                unsafePutByte(index + it, srcMemory.getByte(it))
            }
        }
    }

    final override fun putBytes(index: Int, src: Readable, length: Int) {
        require(length >= 0) { "length: $length < 0" }
        requireSizeAt(index, length)
        if (length == 0) return
        src.requestCanRead(length)
        val hasOptimized =
            when (val srcMemory = (src as? MemoryReadable)?.memory?.internalMemory()) {
                is ByteArrayMemory -> {
                    unsafePutBytes(index, srcMemory.array, srcMemory.offset + src.position, length)
                    true
                }
                is AbstractMemory<*> -> unsafeTryPutBytesOptimized(index, srcMemory.slice(src.position, length))
                else -> false
            }
        if (hasOptimized) {
            src.skip(length)
        } else {
            repeat(length) {
                unsafePutByte(index + it, src.readByte())
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

    @Suppress("UNCHECKED_CAST")
    final override fun internalMemory(): M = this as M

    override fun equals(other: Any?): Boolean {
        if (other === this) return true
        if (other == null) return false
        if (other !is Memory) return false
        if (other.size != size) return false

        val otherInternalMemory = other.internalMemory()
        val optimized = (otherInternalMemory as? AbstractMemory<*>)?.let {
            unsafeTryEqualsOptimized(otherInternalMemory)
        }
        if (optimized != null) return optimized

        return compareTo(otherInternalMemory) == 0
    }

    protected abstract fun unsafeTryEqualsOptimized(other: AbstractMemory<*>): Boolean?

    final override fun hashCode(): Int {
        var h = 1
        for (i in (size - 1) downTo 0) {
            h = 31 * h + unsafeGetByte(i).toInt()
        }
        return h
    }

    override fun toString(): String = "${this::class.simpleName}($size)"
}
