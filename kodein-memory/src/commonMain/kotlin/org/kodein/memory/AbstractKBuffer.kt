package org.kodein.memory

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

    private var mark: Int = -1

    final override fun clear() {
        position = 0
        limit = capacity
        mark = -1
    }

    final override fun flip() {
        limit = position
        position = 0
        mark = -1
    }

    final override fun rewind() {
        position = 0
        mark = -1
    }

    final override fun mark() {
        mark = position
    }

    final override fun reset() {
        require(mark >= 0) { "Mark is unset" }
        position = mark
    }

    final override fun duplicate(): KBuffer {
        val dup = createDuplicate()
        dup.position = position
        dup.limit = limit
        dup.mark = mark
        return dup
    }

    protected abstract fun createDuplicate(): AbstractKBuffer

    @Suppress("NOTHING_TO_INLINE")
    private inline fun checkPositiveIndex(index: Int) {
        require(position + index <= limit) { "position (=$position) + $index > limit (=$limit)." }
    }

    @Suppress("NOTHING_TO_INLINE")
    private inline fun checkIndex(index: Int) {
        checkPositiveIndex(index)
        require(position + index >= 0) { "position (=$position) + $index < 0." }
    }

    final override fun put(value: Byte) {
        checkPositiveIndex(1)
        set(0, value)
        position += 1
    }

    final override fun putChar(value: Char) = putShort(value.toShort())

    final override fun putShort(value: Short) {
        checkPositiveIndex(2)
        setShort(0, value)
        position += 2
    }

    final override fun putInt(value: Int) {
        checkPositiveIndex(4)
        setInt(0, value)
        position += 4
    }

    final override fun putLong(value: Long) {
        checkPositiveIndex(8)
        setLong(0, value)
        position += 8
    }

    final override fun putFloat(value: Float) = putInt(value.toRawBits())

    final override fun putDouble(value: Double) = putLong(value.toRawBits())

    final override fun putBytes(src: ByteArray, offset: Int, length: Int) {
        require(offset >= 0) { "offset (=$offset) < 0" }
        require(length >= 0) { "length (=$length) < 0" }
        require((offset + length) <= src.size) { "offset (=$offset) + length (=$length) > src.size (=${src.size}" }
        checkPositiveIndex(length)
        if (length == 0) return
        unsafeWriteBytes(src, offset, length)
        position += length
    }

    protected abstract fun unsafeWriteBytes(src: ByteArray, offset: Int, length: Int)

    final override fun putBytes(src: Readable, length: Int) {
        require(length >= 0) { "length (=$length) < 0" }
        require(length <= src.remaining) { "length (=$length) < src.remaining (=${src.remaining})" }
        if (length == 0) return
        if (src is ByteArrayKBuffer) {
            putBytes(src.array, src.offset + src.position, length)
        } else {
            val hasOptimized = unsafeTryWriteAllOptimized(src, length)
            if (!hasOptimized) {
                for (i in 0 until length) {
                    unsafeSet(i, src[i])
                }
            }
            src.skip(length)
            position += length
        }
    }

    abstract protected fun unsafeTryWriteAllOptimized(src: Readable, length: Int): Boolean

    final override fun set(index: Int, value: Byte) {
        checkIndex(index)
        unsafeSet(index, value)
    }

    protected abstract fun unsafeSet(index: Int, value: Byte)

    final override fun setChar(index: Int, value: Char) = setShort(index, value.toShort())

    final override fun setShort(index: Int, value: Short) {
        checkIndex(index)
        unsafeSetShort(index, value)
    }

    protected abstract fun unsafeSetShort(index: Int, value: Short)

    final override fun setInt(index: Int, value: Int) {
        checkIndex(index)
        unsafeSetInt(index, value)
    }

    protected abstract fun unsafeSetInt(index: Int, value: Int)

    final override fun setLong(index: Int, value: Long) {
        checkIndex(index)
        unsafeSetLong(index, value)
    }

    protected abstract fun unsafeSetLong(index: Int, value: Long)

    final override fun setFloat(index: Int, value: Float) = setInt(index, value.toRawBits())

    final override fun setDouble(index: Int, value: Double) = setLong(index, value.toRawBits())

    final override fun read(): Byte {
        checkPositiveIndex(1)
        val ret = unsafeGet(0)
        position += 1
        return ret
    }

    final override fun readChar() = readShort().toChar()

    final override fun readShort(): Short {
        checkPositiveIndex(2)
        val ret = unsafeGetShort(0)
        position += 2
        return ret
    }

    final override fun readInt(): Int {
        checkPositiveIndex(4)
        val ret = unsafeGetInt(0)
        position += 4
        return ret
    }

    final override fun readLong(): Long {
        checkPositiveIndex(8)
        val ret = unsafeGetLong(0)
        position += 8
        return ret
    }

    final override fun readFloat(): Float = Float.fromBits(readInt())

    final override fun readDouble(): Double = Double.fromBits(readLong())

    final override fun readBytes(dst: ByteArray, offset: Int, length: Int) {
        require(offset >= 0) { "offset (=$offset) < 0" }
        require(length >= 0) { "length (=$length) < 0" }
        require((offset + length) <= dst.size) { "offset (=$offset) + length (=$length) > dst.size (=${dst.size}" }
        checkPositiveIndex(length)
        if (length == 0) return
        unsafeReadBytes(dst, offset, length)
        position += length
    }

    protected abstract fun unsafeReadBytes(dst: ByteArray, offset: Int, length: Int)

    final override fun get(index: Int): Byte {
        checkIndex(index)
        return unsafeGet(index)
    }

    protected abstract fun unsafeGet(index: Int): Byte

    final override fun getChar(index: Int): Char = getShort(index).toChar()

    final override fun getShort(index: Int): Short {
        checkIndex(index)
        return unsafeGetShort(index)
    }

    protected abstract fun unsafeGetShort(index: Int): Short

    final override fun getInt(index: Int): Int {
        checkIndex(index)
        return unsafeGetInt(index)
    }

    protected abstract fun unsafeGetInt(index: Int): Int

    final override fun getLong(index: Int): Long {
        checkIndex(index)
        return unsafeGetLong(index)
    }

    protected abstract fun unsafeGetLong(index: Int): Long

    final override fun getFloat(index: Int): Float = Float.fromBits(getInt(index))

    final override fun getDouble(index: Int): Double = Double.fromBits(getLong(index))

    final override fun skip(count: Int) {
        require(count >= 0) { "count (=$count) < 0" }
        checkPositiveIndex(count)
        position += count
    }

}
