package org.kodein.memory

import java.nio.ByteBuffer

class JvmNioKBuffer(val byteBuffer: ByteBuffer) : KBuffer {

    override val capacity: Int get() = byteBuffer.capacity()

    override var position: Int
        get() = byteBuffer.position()
        set(value) { byteBuffer.position(value) }

    override var limit: Int
        get() = byteBuffer.limit()
        set(value) { byteBuffer.limit(value) }

    override fun clear() { byteBuffer.clear() }

    override fun flip() { byteBuffer.flip() }

    override fun rewind() { byteBuffer.rewind() }

    override fun mark() { byteBuffer.mark() }

    override fun reset() { byteBuffer.reset() }

    override fun duplicate(): KBuffer = JvmNioKBuffer(byteBuffer.duplicate())

    override fun slice(): KBuffer = JvmNioKBuffer(byteBuffer.slice())

    override fun view(index: Int, length: Int): JvmNioKBuffer {
        val position = byteBuffer.position()
        val limit = byteBuffer.limit()
        try {
            byteBuffer.position(index)
            byteBuffer.limit(index + length)
            val view = JvmNioKBuffer(byteBuffer.slice())
            return view
        } finally {
            byteBuffer.position(position)
            byteBuffer.limit(limit)
        }
    }

    override val remaining: Int get() = byteBuffer.remaining()

    override fun put(value: Byte) { byteBuffer.put(value) }

    override fun putChar(value: Char) { byteBuffer.putChar(value) }

    override fun putShort(value: Short) { byteBuffer.putShort(value) }

    override fun putInt(value: Int) { byteBuffer.putInt(value) }

    override fun putLong(value: Long) { byteBuffer.putLong(value) }

    override fun putFloat(value: Float) { byteBuffer.putFloat(value) }

    override fun putDouble(value: Double) { byteBuffer.putDouble(value) }

    override fun putBytes(src: ByteArray, srcOffset: Int, length: Int) { byteBuffer.put(src, srcOffset, length) }

    override fun putBytes(src: Readable, length: Int) {
        when (val srcBuffer = src.internalBuffer()) {
            is JvmNioKBuffer -> {
                if (srcBuffer.byteBuffer.remaining() == length) {
                    byteBuffer.put(srcBuffer.byteBuffer)
                } else {
                    val srcLimit = srcBuffer.byteBuffer.limit()
                    try {
                        srcBuffer.byteBuffer.limit(srcBuffer.byteBuffer.position() + length)
                        byteBuffer.put(srcBuffer.byteBuffer)
                    } finally {
                        srcBuffer.byteBuffer.limit(srcLimit)
                    }
                }
            }
            is ByteArrayKBuffer -> {
                byteBuffer.put(srcBuffer.array, srcBuffer.offset + srcBuffer.position, length)
                srcBuffer.skip(length)
            }
            else -> {
                for (i in 0 until length) {
                    byteBuffer.put(src.read())
                }
            }
        }
    }

    override fun set(index: Int, value: Byte) { byteBuffer.put(index, value) }

    override fun setChar(index: Int, value: Char) { byteBuffer.putChar(index, value) }

    override fun setShort(index: Int, value: Short) { byteBuffer.putShort(index, value) }

    override fun setInt(index: Int, value: Int) { byteBuffer.putInt(index, value) }

    override fun setLong(index: Int, value: Long) { byteBuffer.putLong(index, value) }

    override fun setFloat(index: Int, value: Float) { byteBuffer.putFloat(index, value) }

    override fun setDouble(index: Int, value: Double) { byteBuffer.putDouble(index, value) }

    override fun setBytes(index: Int, src: ByteArray, srcOffset: Int, length: Int) {
        val position = byteBuffer.position()
        try {
            byteBuffer.position(index)
            putBytes(src, srcOffset, length)
        } finally {
            byteBuffer.position(position)
        }
    }

    override fun setBytes(index: Int, src: ReadBuffer, srcIndex: Int, length: Int) {
        val thisPosition = byteBuffer.position()
        val srcPosition = src.position
        try {
            byteBuffer.position(index)
            src.position = srcIndex
            putBytes(src, length)
        } finally {
            byteBuffer.position(thisPosition)
            src.position = srcPosition
        }
    }

    override fun read(): Byte = byteBuffer.get()

    override fun readChar(): Char = byteBuffer.getChar()

    override fun readShort(): Short = byteBuffer.getShort()

    override fun readInt(): Int = byteBuffer.getInt()

    override fun readLong(): Long = byteBuffer.getLong()

    override fun readFloat(): Float = byteBuffer.getFloat()

    override fun readDouble(): Double = byteBuffer.getDouble()

    override fun readBytes(dst: ByteArray, offset: Int, length: Int) { byteBuffer.get(dst, offset, length) }

    override fun get(index: Int): Byte = byteBuffer.get(index)

    override fun getChar(index: Int): Char = byteBuffer.getChar(index)

    override fun getShort(index: Int): Short = byteBuffer.getShort(index)

    override fun getInt(index: Int): Int = byteBuffer.getInt(index)

    override fun getLong(index: Int): Long = byteBuffer.getLong(index)

    override fun getFloat(index: Int): Float = byteBuffer.getFloat(index)

    override fun getDouble(index: Int): Double = byteBuffer.getDouble(index)

    override fun getBytes(index: Int, dst: ByteArray, offset: Int, length: Int) {
        val position = byteBuffer.position()
        try {
            byteBuffer.position(index)
            byteBuffer.get(dst, offset, length)
        } finally {
            byteBuffer.position(position)
        }
    }

    override fun skip(count: Int) {
        require(count >= 0) { "count (=$count) < 0" }
        byteBuffer.position(byteBuffer.position() + count)
    }

    override fun internalBuffer() = this

    override fun equals(other: Any?): Boolean {
        if (other === this) return true
        if (other == null) return false
        if (other !is KBuffer) return false
        if (other.remaining != remaining) return false

        when (val otherBuffer = other.internalBuffer()) {
            is JvmNioKBuffer -> return byteBuffer == otherBuffer.byteBuffer
            is ByteArrayKBuffer -> return byteBuffer == ByteBuffer.wrap(otherBuffer.array, otherBuffer.offset + otherBuffer.position, otherBuffer.remaining)
            else -> {
                var otherP = other.limit - 1
                for (thisP in (limit - 1) downTo position) {
                    if (byteBuffer.get(thisP) != otherBuffer.get(otherP))
                        return false
                    --otherP
                }
                return true
            }
        }
    }

    override fun hashCode() = byteBuffer.hashCode()
}
