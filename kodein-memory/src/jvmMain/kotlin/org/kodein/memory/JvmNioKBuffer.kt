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

    override val remaining: Int get() = byteBuffer.remaining()

    override fun put(value: Byte) { byteBuffer.put(value) }

    override fun putChar(value: Char) { byteBuffer.putChar(value) }

    override fun putShort(value: Short) { byteBuffer.putShort(value) }

    override fun putInt(value: Int) { byteBuffer.putInt(value) }

    override fun putLong(value: Long) { byteBuffer.putLong(value) }

    override fun putFloat(value: Float) { byteBuffer.putFloat(value) }

    override fun putDouble(value: Double) { byteBuffer.putDouble(value) }

    override fun putBytes(src: ByteArray, offset: Int, length: Int) { byteBuffer.put(src, offset, length) }

    override fun putBytes(src: Readable, length: Int) {
        when (src) {
            is JvmNioKBuffer -> {
                if (src.byteBuffer.remaining() == length) {
                    byteBuffer.put(src.byteBuffer)
                } else {
                    byteBuffer.put(src.byteBuffer.duplicate().also { it.limit(it.position() + length) })
                }
            }
            is ByteArrayKBuffer -> {
                byteBuffer.put(src.array, src.offset + src.position, length)
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

    override fun skip(count: Int) {
        require(count >= 0) { "count (=$count) < 0" }
        byteBuffer.position(byteBuffer.position() + count)
    }

}
