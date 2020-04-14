package org.kodein.memory.io

import kotlin.math.min

abstract class AbstractKBuffer(final override val capacity: Int) : KBuffer {

    final override var offset: Int = 0
        private set

    final override var position: Int = 0
    set(value) {
        require(value >= 0) { "$value < 0." }
        require(value <= limit) { "$value > limit ($limit)." }
        field = value
    }

    final override var limit: Int = capacity - offset
    set(value) {
        require(value >= position) { "$value < position ($position)." }
        require(value <= capacity - offset) { "$value > capacity - offset ($capacity - $offset)." }
        field = value
    }

    final override val available: Int get() = limit - position

    override fun valid(): Boolean = available != 0

    final override fun offset(newOffset: Int) {
        require(newOffset >= 0) { "$newOffset < 0" }
        require(newOffset <= capacity) { "$newOffset > capacity ($capacity)" }
        val newLimit = limit + offset - newOffset
        offset = newOffset
        limit = newLimit
        position = 0
    }

    final override fun reset() {
        offset = 0
        position = 0
        limit = capacity
    }

    final override fun flip() {
        limit = position
        position = 0
    }

    protected abstract fun createDuplicate(): AbstractKBuffer

    final override fun duplicate(): KBuffer = createDuplicate().also {
        it.offset = offset
        it.position = position
        it.limit = limit
    }

    final override fun slice() = createDuplicate().also {
        it.offset = offset + position
        it.position = 0
        it.limit = available
    }

    final override fun slice(index: Int, length: Int): KBuffer {
        require(index >= 0) { "$index < 0" }
        require(length >= 0) { "$length < 0" }
        require(index + length <= limit) { "$index + $length > limit ($limit)" }

        return createDuplicate().also {
            it.offset = offset + index
            it.position = 0
            it.limit = length
        }
    }

    @Suppress("NOTHING_TO_INLINE")
    private inline fun checkHasAvailable(size: Int) {
        require(size <= available) { "$size > available ($available)." }
    }

    final override fun putByte(value: Byte) {
        checkHasAvailable(Byte.SIZE_BYTES)
        unsafeSetByte(offset + position, value)
        position += 1
    }

    final override fun putChar(value: Char) = putShort(value.toShort())

    final override fun putShort(value: Short) {
        checkHasAvailable(Short.SIZE_BYTES)
        unsafeSetShort(offset + position, value)
        position += 2
    }

    final override fun putInt(value: Int) {
        checkHasAvailable(Int.SIZE_BYTES)
        unsafeSetInt(offset + position, value)
        position += 4
    }

    final override fun putLong(value: Long) {
        checkHasAvailable(Long.SIZE_BYTES)
        unsafeSetLong(offset + position, value)
        position += 8
    }

    final override fun putFloat(value: Float) = putInt(value.toRawBits())

    final override fun putDouble(value: Double) = putLong(value.toRawBits())

    final override fun putBytes(src: ByteArray, srcOffset: Int, length: Int) {
        require(srcOffset >= 0) { "$srcOffset < 0" }
        require(length >= 0) { "$length < 0" }
        require((srcOffset + length) <= src.size) { "$srcOffset + $length > src.size (${src.size}" }
        checkHasAvailable(length)
        if (length == 0) return
        unsafeSetBytes(offset + position, src, srcOffset, length)
        position += length
    }

    final override fun putBytes(src: Readable, length: Int) {
        require(length >= 0) { "length ($length) < 0" }
        require(length <= src.available) { "length ($length) < src.available (${src.available})" }
        checkHasAvailable(length)
        if (length == 0) return
        val srcBuffer = src.internalBuffer()

        val hasOptimized =
                when (srcBuffer) {
                    is ByteArrayKBuffer -> {
                        unsafeSetBytes(offset + position, srcBuffer.array, srcBuffer.offset + srcBuffer.position, length)
                        true
                    }
                    is AbstractKBuffer -> unsafeTrySetBytesOptimized(offset + position, srcBuffer, srcBuffer.offset + srcBuffer.position, length)
                    else -> false
                }
        if (hasOptimized) {
            position += length
            srcBuffer.skip(length)
        } else {
            repeat(length) {
                unsafeSetByte(offset + position++, srcBuffer.readByte())
            }
        }
    }

    @Suppress("NOTHING_TO_INLINE")
    private inline fun checkIndex(index: Int, size: Int) {
        require(index >= 0) { "$index < 0." }
        require(index + size <= limit) { "$index + $size > limit ($limit)" }
    }

    final override fun setByte(index: Int, value: Byte) {
        checkIndex(index, Byte.SIZE_BYTES)
        unsafeSetByte(offset + index, value)
    }

    protected abstract fun unsafeSetByte(index: Int, value: Byte)

    final override fun setChar(index: Int, value: Char) = setShort(index, value.toShort())

    final override fun setShort(index: Int, value: Short) {
        checkIndex(index, Short.SIZE_BYTES)
        unsafeSetShort(offset + index, value)
    }

    protected abstract fun unsafeSetShort(index: Int, value: Short)

    final override fun setInt(index: Int, value: Int) {
        checkIndex(index, Int.SIZE_BYTES)
        unsafeSetInt(offset + index, value)
    }

    protected abstract fun unsafeSetInt(index: Int, value: Int)

    final override fun setLong(index: Int, value: Long) {
        checkIndex(index, Long.SIZE_BYTES)
        unsafeSetLong(offset + index, value)
    }

    protected abstract fun unsafeSetLong(index: Int, value: Long)

    final override fun setFloat(index: Int, value: Float) = setInt(index, value.toRawBits())

    final override fun setDouble(index: Int, value: Double) = setLong(index, value.toRawBits())

    final override fun setBytes(index: Int, src: ByteArray, srcOffset: Int, length: Int) {
        require(srcOffset >= 0) { "$srcOffset < 0" }
        require(length >= 0) { "$length < 0" }
        require((srcOffset + length) <= src.size) { "$srcOffset + $length > src.size (${src.size}" }
        checkIndex(index, length)
        if (length == 0) return
        unsafeSetBytes(offset + index, src, srcOffset, length)
    }

    protected abstract fun unsafeSetBytes(index: Int, src: ByteArray, srcOffset: Int, length: Int)

    final override fun setBytes(index: Int, src: ReadMemory, srcOffset: Int, length: Int) {
        require(length >= 0) { "length ($length) < 0" }
        require(srcOffset + length <= src.limit) { "$srcOffset + $length < src.limit (${src.limit})" }
        checkIndex(index, length)
        if (length == 0) return
        val srcBuffer = src.internalBuffer()
        val hasOptimized =
                when (srcBuffer) {
                    is ByteArrayKBuffer -> {
                        unsafeSetBytes(offset + index, srcBuffer.array, srcBuffer.offset + srcOffset, length)
                        true
                    }
                    is AbstractKBuffer -> unsafeTrySetBytesOptimized(offset + index, srcBuffer, srcBuffer.offset + srcOffset, length)
                    else -> false
                }
        if (!hasOptimized) {
            repeat (length) {
                unsafeSetByte(offset + index + it, srcBuffer.getByte(srcOffset + it))
            }
        }
    }

    protected abstract fun unsafeTrySetBytesOptimized(index: Int, src: AbstractKBuffer, srcOffset: Int, length: Int): Boolean

    override fun flush() {}

    final override fun receive(): Int {
        if (available < Byte.SIZE_BYTES) return -1
        val ret = unsafeGetByte(offset + position)
        position += 1
        return ret.toInt()
    }

    final override fun receive(dst: ByteArray, dstOffset: Int, length: Int): Int {
        require(dstOffset >= 0) { "offset ($dstOffset) < 0" }
        require(length >= 0) { "length ($length) < 0" }
        require((dstOffset + length) <= dst.size) { "offset ($dstOffset) + length ($length) > dst.size (${dst.size}" }
        if (available == 0) return -1
        if (length == 0) return 0
        val actualLength = min(length, available)
        unsafeGetBytes(offset + position, dst, dstOffset, actualLength)
        position += actualLength
        return actualLength
    }

    final override fun readByte(): Byte {
        checkHasAvailable(Byte.SIZE_BYTES)
        val ret = unsafeGetByte(offset + position)
        position += 1
        return ret
    }

    final override fun readChar() = readShort().toChar()

    final override fun readShort(): Short {
        checkHasAvailable(Short.SIZE_BYTES)
        val ret = unsafeGetShort(offset + position)
        position += 2
        return ret
    }

    final override fun readInt(): Int {
        checkHasAvailable(Int.SIZE_BYTES)
        val ret = unsafeGetInt(offset + position)
        position += 4
        return ret
    }

    final override fun readLong(): Long {
        checkHasAvailable(Long.SIZE_BYTES)
        val ret = unsafeGetLong(offset + position)
        position += 8
        return ret
    }

    final override fun readFloat(): Float = Float.fromBits(readInt())

    final override fun readDouble(): Double = Double.fromBits(readLong())

    final override fun readBytes(dst: ByteArray, dstOffset: Int, length: Int) {
        require(dstOffset >= 0) { "offset ($dstOffset) < 0" }
        require(length >= 0) { "length ($length) < 0" }
        require((dstOffset + length) <= dst.size) { "offset ($dstOffset) + length ($length) > dst.size (${dst.size}" }
        checkHasAvailable(length)
        if (length == 0) return
        unsafeGetBytes(offset + position, dst, dstOffset, length)
        position += length
    }

    final override fun getByte(index: Int): Byte {
        checkIndex(index, Byte.SIZE_BYTES)
        return unsafeGetByte(offset + index)
    }

    protected abstract fun unsafeGetByte(index: Int): Byte

    final override fun getChar(index: Int): Char = getShort(index).toChar()

    final override fun getShort(index: Int): Short {
        checkIndex(index, Short.SIZE_BYTES)
        return unsafeGetShort(offset + index)
    }

    protected abstract fun unsafeGetShort(index: Int): Short

    final override fun getInt(index: Int): Int {
        checkIndex(index, Int.SIZE_BYTES)
        return unsafeGetInt(offset + index)
    }

    protected abstract fun unsafeGetInt(index: Int): Int

    final override fun getLong(index: Int): Long {
        checkIndex(index, Long.SIZE_BYTES)
        return unsafeGetLong(offset + index)
    }

    protected abstract fun unsafeGetLong(index: Int): Long

    final override fun getFloat(index: Int): Float = Float.fromBits(getInt(index))

    final override fun getDouble(index: Int): Double = Double.fromBits(getLong(index))

    final override fun getBytes(index: Int, dst: ByteArray, dstOffset: Int, length: Int) {
        require(dstOffset >= 0) { "offset ($dstOffset) < 0" }
        require(length >= 0) { "length ($length) < 0" }
        require((dstOffset + length) <= dst.size) { "offset ($dstOffset) + length ($length) > dst.size (${dst.size}" }
        checkIndex(index, length)
        if (length == 0) return
        unsafeGetBytes(offset + index, dst, dstOffset, length)
    }

    protected abstract fun unsafeGetBytes(index: Int, dst: ByteArray, dstOffset: Int, length: Int)

    final override fun skip(count: Int): Int {
        require(count >= 0) { "count ($count) < 0" }
        val skipped = min(count, available)
        position += skipped
        return skipped
    }

    override fun internalBuffer() = this

    private fun slowEquals(other: KBuffer): Boolean {
        var otherP = other.limit - 1
        for (thisP in (limit - 1) downTo position) {
            if (unsafeGetByte(offset + thisP) != other.getByte(otherP)) {
                return false
            }
            --otherP
        }
        return true
    }

    override fun equals(other: Any?): Boolean {
        if (other === this) return true
        if (other == null) return false
        if (other !is KBuffer) return false
        if (other.available != available) return false

        val otherBuffer = other.internalBuffer()
        val optimized = (otherBuffer as? AbstractKBuffer)?.let { tryEqualsOptimized(offset + position, otherBuffer, otherBuffer.offset + otherBuffer.position, available) }
        if (optimized != null)
            return optimized

        return slowEquals(otherBuffer)
    }

    protected abstract fun tryEqualsOptimized(index: Int, other: AbstractKBuffer, otherIndex: Int, length: Int): Boolean?

    final override fun hashCode(): Int {
        var h = 1
        for (i in (limit - 1) downTo position) {
            h = 31 * h + unsafeGetByte(offset + i).toInt()
        }
        return h
    }

    override fun toString(): String = "$implementation[offset=$offset, position=$position, limit=$limit, capacity=$capacity]"

    protected abstract val implementation: String
}
