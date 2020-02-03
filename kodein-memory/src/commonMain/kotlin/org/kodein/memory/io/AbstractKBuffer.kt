package org.kodein.memory.io

abstract class AbstractKBuffer(final override val capacity: Int) : KBuffer {

    final override var position: Int = 0
    set(value) {
        require(value >= 0) { "$value < 0." }
        require(value <= limit) { "$value > limit (=$limit)." }
        field = value
    }

    final override var limit: Int = capacity
    set(value) {
        require(value >= position) { "$value < position (=$position)." }
        require(value <= capacity) { "$value > capacity (=$capacity)." }
        field = value
    }

    final override val remaining: Int get() = limit - position

    final override fun clear() {
        position = 0
        limit = capacity
    }

    final override fun flip() {
        limit = position
        position = 0
    }

    final override fun rewind() {
        position = 0
    }

    final override fun duplicate(): KBuffer {
        val dup = createDuplicate()
        dup.position = position
        dup.limit = limit
        return dup
    }

    protected abstract fun createDuplicate(): AbstractKBuffer

    final override fun slice() = unsafeView(position, remaining)

    final override fun slice(index: Int, length: Int): KBuffer {
        require(index >= 0) { "$index < 0" }
        require(length >= 0) { "$length < 0" }
        require(index + length <= limit) { "$index + $length > limit (=$limit)" }

        return unsafeView(index, length)
    }

    protected abstract fun unsafeView(index: Int, length: Int): KBuffer

    @Suppress("NOTHING_TO_INLINE")
    private inline fun checkHasRemaining(size: Int) {
        require(size <= remaining) { "$size > remaining (=$remaining)." }
    }

    @Suppress("NOTHING_TO_INLINE")
    private inline fun checkIndex(index: Int, size: Int) {
        require(index >= 0) { "$index < 0." }
        require(index + size <= limit) { "$index + $size > limit (=$limit)" }
    }

    final override fun put(value: Byte) {
        checkHasRemaining(Byte.SIZE_BYTES)
        unsafeSet(position, value)
        position += 1
    }

    final override fun putChar(value: Char) = putShort(value.toShort())

    final override fun putShort(value: Short) {
        checkHasRemaining(Short.SIZE_BYTES)
        unsafeSetShort(position, value)
        position += 2
    }

    final override fun putInt(value: Int) {
        checkHasRemaining(Int.SIZE_BYTES)
        unsafeSetInt(position, value)
        position += 4
    }

    final override fun putLong(value: Long) {
        checkHasRemaining(Long.SIZE_BYTES)
        unsafeSetLong(position, value)
        position += 8
    }

    final override fun putFloat(value: Float) = putInt(value.toRawBits())

    final override fun putDouble(value: Double) = putLong(value.toRawBits())

    final override fun putBytes(src: ByteArray, srcOffset: Int, length: Int) {
        require(srcOffset >= 0) { "$srcOffset < 0" }
        require(length >= 0) { "$length < 0" }
        require((srcOffset + length) <= src.size) { "$srcOffset + $length > src.size (=${src.size}" }
        checkHasRemaining(length)
        if (length == 0) return
        unsafeSetBytes(position, src, srcOffset, length)
        position += length
    }

    final override fun putBytes(src: Readable, length: Int) {
        require(length >= 0) { "length (=$length) < 0" }
        require(length <= src.remaining) { "length (=$length) < src.remaining (=${src.remaining})" }
        checkHasRemaining(length)
        if (length == 0) return
        val srcBuffer = src.internalBuffer()
        if (srcBuffer is ByteArrayKBuffer) {
            unsafeSetBytes(position, srcBuffer.array, srcBuffer.offset + srcBuffer.position, length)
            position += length
            srcBuffer.skip(length)
        } else {
            val hasOptimized = if (srcBuffer is ReadBuffer) unsafeTrySetBytesOptimized(position, srcBuffer, srcBuffer.position, length) else false
            if (hasOptimized) {
                position += length
                srcBuffer.skip(length)
            } else {
                repeat(length) {
                    unsafeSet(position++, srcBuffer.read())
                }
            }
        }
    }

    final override fun set(index: Int, value: Byte) {
        checkIndex(index, Byte.SIZE_BYTES)
        unsafeSet(index, value)
    }

    protected abstract fun unsafeSet(index: Int, value: Byte)

    final override fun setChar(index: Int, value: Char) = setShort(index, value.toShort())

    final override fun setShort(index: Int, value: Short) {
        checkIndex(index, Short.SIZE_BYTES)
        unsafeSetShort(index, value)
    }

    protected abstract fun unsafeSetShort(index: Int, value: Short)

    final override fun setInt(index: Int, value: Int) {
        checkIndex(index, Int.SIZE_BYTES)
        unsafeSetInt(index, value)
    }

    protected abstract fun unsafeSetInt(index: Int, value: Int)

    final override fun setLong(index: Int, value: Long) {
        checkIndex(index, Long.SIZE_BYTES)
        unsafeSetLong(index, value)
    }

    protected abstract fun unsafeSetLong(index: Int, value: Long)

    final override fun setFloat(index: Int, value: Float) = setInt(index, value.toRawBits())

    final override fun setDouble(index: Int, value: Double) = setLong(index, value.toRawBits())

    final override fun peek(): Byte {
        checkHasRemaining(Byte.SIZE_BYTES)
        return unsafeGet(position)
    }

    final override fun read(): Byte {
        checkHasRemaining(Byte.SIZE_BYTES)
        val ret = unsafeGet(position)
        position += 1
        return ret
    }

    final override fun setBytes(index: Int, src: ByteArray, srcOffset: Int, length: Int) {
        require(srcOffset >= 0) { "$srcOffset < 0" }
        require(length >= 0) { "$length < 0" }
        require((srcOffset + length) <= src.size) { "$srcOffset + $length > src.size (=${src.size}" }
        checkIndex(index, length)
        if (length == 0) return
        unsafeSetBytes(index, src, srcOffset, length)
    }

    protected abstract fun unsafeSetBytes(index: Int, src: ByteArray, srcOffset: Int, length: Int)

    final override fun setBytes(index: Int, src: ReadMemory, srcOffset: Int, length: Int) {
        require(length >= 0) { "length (=$length) < 0" }
        require(srcOffset + length <= src.limit) { "$srcOffset + $length < src.limit (=${src.limit})" }
        checkIndex(index, length)
        if (length == 0) return
        val srcBuffer = src.internalBuffer()
        val hasOptimized =
                when (srcBuffer) {
                    is ByteArrayKBuffer -> {
                        unsafeSetBytes(index, srcBuffer.array, srcBuffer.offset + srcOffset, length)
                        true
                    }
                    is ReadBuffer -> unsafeTrySetBytesOptimized(index, srcBuffer, srcOffset, length)
                    else -> false
                }
        if (!hasOptimized) {
            for (i in 0 until length) {
                unsafeSet(index + i, srcBuffer[srcOffset + i])
            }
        }
    }

    protected abstract fun unsafeTrySetBytesOptimized(index: Int, src: ReadBuffer, srcOffset: Int, length: Int): Boolean

    final override fun readChar() = readShort().toChar()

    final override fun readShort(): Short {
        checkHasRemaining(Short.SIZE_BYTES)
        val ret = unsafeGetShort(position)
        position += 2
        return ret
    }

    final override fun readInt(): Int {
        checkHasRemaining(Int.SIZE_BYTES)
        val ret = unsafeGetInt(position)
        position += 4
        return ret
    }

    final override fun readLong(): Long {
        checkHasRemaining(Long.SIZE_BYTES)
        val ret = unsafeGetLong(position)
        position += 8
        return ret
    }

    final override fun readFloat(): Float = Float.fromBits(readInt())

    final override fun readDouble(): Double = Double.fromBits(readLong())

    final override fun readBytes(dst: ByteArray, offset: Int, length: Int) {
        require(offset >= 0) { "offset (=$offset) < 0" }
        require(length >= 0) { "length (=$length) < 0" }
        require((offset + length) <= dst.size) { "offset (=$offset) + length (=$length) > dst.size (=${dst.size}" }
        checkHasRemaining(length)
        if (length == 0) return
        unsafeGetBytes(position, dst, offset, length)
        position += length
    }

    final override fun get(index: Int): Byte {
        checkIndex(index, Byte.SIZE_BYTES)
        return unsafeGet(index)
    }

    protected abstract fun unsafeGet(index: Int): Byte

    final override fun getChar(index: Int): Char = getShort(index).toChar()

    final override fun getShort(index: Int): Short {
        checkIndex(index, Short.SIZE_BYTES)
        return unsafeGetShort(index)
    }

    protected abstract fun unsafeGetShort(index: Int): Short

    final override fun getInt(index: Int): Int {
        checkIndex(index, Int.SIZE_BYTES)
        return unsafeGetInt(index)
    }

    protected abstract fun unsafeGetInt(index: Int): Int

    final override fun getLong(index: Int): Long {
        checkIndex(index, Long.SIZE_BYTES)
        return unsafeGetLong(index)
    }

    protected abstract fun unsafeGetLong(index: Int): Long

    final override fun getFloat(index: Int): Float = Float.fromBits(getInt(index))

    final override fun getDouble(index: Int): Double = Double.fromBits(getLong(index))

    final override fun getBytes(index: Int, dst: ByteArray, offset: Int, length: Int) {
        require(offset >= 0) { "offset (=$offset) < 0" }
        require(length >= 0) { "length (=$length) < 0" }
        require((offset + length) <= dst.size) { "offset (=$offset) + length (=$length) > dst.size (=${dst.size}" }
        checkIndex(index, length)
        if (length == 0) return
        unsafeGetBytes(index, dst, offset, length)
    }

    protected abstract fun unsafeGetBytes(index: Int, dst: ByteArray, offset: Int, length: Int)

    final override fun skip(count: Int) {
        require(count >= 0) { "count (=$count) < 0" }
        checkHasRemaining(count)
        position += count
    }

    override fun internalBuffer() = this

    override fun equals(other: Any?): Boolean {
        if (other === this) return true
        if (other == null) return false
        if (other !is KBuffer) return false
        if (other.remaining != remaining) return false

        val otherBuffer = other.internalBuffer()
        val optimized = tryEqualsOptimized(otherBuffer)
        if (optimized != null)
            return optimized

        var otherP = other.limit - 1
        for (thisP in (limit - 1) downTo position) {
            if (unsafeGet(thisP) != otherBuffer[otherP]) {
                return false
            }
            --otherP
        }
        return true
    }

    protected abstract fun tryEqualsOptimized(other: KBuffer): Boolean?

    override fun hashCode(): Int {
        var h = 1
        for (i in (limit - 1) downTo position) {
            h = 31 * h + get(i).toInt()
        }
        return h
    }

    override fun toString(): String = "$name[pos=$position, lim=$limit, cap=$capacity]"

    protected abstract val name: String
}
