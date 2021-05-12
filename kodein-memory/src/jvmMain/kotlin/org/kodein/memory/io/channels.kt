package org.kodein.memory.io

import org.kodein.memory.Closeable
import java.nio.ByteBuffer
import java.nio.channels.FileChannel
import java.nio.channels.ReadableByteChannel
import java.nio.channels.SeekableByteChannel
import java.nio.channels.WritableByteChannel
import kotlin.math.min


internal class WriteableNioByteChannel(private val writeable: Writeable, private val transientClose: Boolean = false) : WritableByteChannel {

    private var isOpen = true

    override fun isOpen(): Boolean = isOpen

    override fun write(src: ByteBuffer): Int {
        val slice = src.slice()
        val count = slice.remaining()
        writeable.writeBytes(ByteBufferMemory(src.slice()))
        src.position(src.position() + count)
        return count
    }

    override fun close() {
        if (transientClose && writeable is Closeable && isOpen) {
            isOpen = false
            writeable.close()
        }
    }
}

public fun Writeable.asNioByteChannel(transientClose: Boolean = false): WritableByteChannel = WriteableNioByteChannel(this, transientClose)


internal class ReadableNioByteChannel(private val readable: Readable, private val transientClose: Boolean = false) : ReadableByteChannel {

    private var isOpen = true

    override fun isOpen(): Boolean = isOpen

    override fun read(dst: ByteBuffer): Int {
        val read = readable.tryReadBytes(ByteBufferMemory(dst.slice()))
        dst.position(dst.position() + read)
        return read
    }

    override fun close() {
        if (transientClose && readable is Closeable && isOpen) {
            isOpen = false
            readable.close()
        }
    }
}

public fun Readable.asNioByteChannel(transientClose: Boolean = false): ReadableByteChannel = ReadableNioByteChannel(this, transientClose)


internal open class NioByteChannelReadable(private val channel: ReadableByteChannel): Readable, Closeable {
    private var valid = true

    private var buffer = ByteBuffer.allocate(8)

    private var bytesRead = 0

    override val position: Int = bytesRead

    override fun requestCanRead(needed: Int) {} // noop

    override fun valid(): Boolean = valid

    override fun tryReadByte(): Int {
        buffer.position(0)
        buffer.limit(1)
        val r = channel.read(buffer)
        if (r == -1) {
            valid = false
            return -1
        }
        bytesRead += 1
        return buffer.get(0).toInt()
    }

    override fun tryReadBytes(dst: ByteArray, dstOffset: Int, length: Int): Int {
        require(dstOffset >= 0)
        require(dstOffset + length <= dst.size)
        val arrayBuffer = ByteBuffer.wrap(dst, dstOffset, length)
        val r = channel.read(arrayBuffer)
        if (r == -1) {
            valid = false
            return -1
        }
        bytesRead += r
        return r
    }

    override fun tryReadBytes(dst: Memory): Int {
        val byteBuffer = when (val dstMemory = dst.internalMemory()) {
            is ByteArrayMemory -> ByteBuffer.wrap(dstMemory.array, dstMemory.offset, dstMemory.size)
            is DirectByteBufferMemory -> dstMemory.byteBuffer
            else -> null
        }

        if (byteBuffer != null) {
            byteBuffer.position(0) {
                val r = channel.read(byteBuffer)
                if (r == -1) valid = false
                else bytesRead += r
                return r
            }
        } else {
            val buffer = ByteArray(dst.size)
            val r = tryReadBytes(buffer)
            if (r == -1) dst.putBytes(0, buffer, 0, r)
            return r
        }
    }

    override fun readByte(): Byte {
        val b = tryReadByte()
        if (b == -1) throw IOException("Channel is over.")
        return b.toByte()
    }

    private inline fun <T> readValue(size: Int, getValue: ByteBuffer.(Int) -> T): T {
        buffer.position(0)
        buffer.limit(size)
        val n = channel.read(buffer)
        bytesRead += n
        if (n != size) throw IOException("Channel is over. Needed $size bytes, but only got $n.")
        return buffer.getValue(0)
    }

    override fun readShort(): Short = readValue(2, ByteBuffer::getShort)

    override fun readInt(): Int = readValue(4, ByteBuffer::getInt)

    override fun readLong(): Long = readValue(8, ByteBuffer::getLong)

    override fun readBytes(dst: ByteArray, dstOffset: Int, length: Int) {
        require(dstOffset >= 0)
        require(dstOffset + length <= dst.size)

        val byteBuffer = ByteBuffer.wrap(dst, dstOffset, length)

        var read = 0
        while (byteBuffer.hasRemaining()) {
            val r = channel.read(byteBuffer)
            if (r == -1) throw IOException("Stream is over. Needed $length bytes, but only got $read.")
            read += r
            bytesRead += r
        }
    }

    override fun skip(count: Int) {
        require(count >= 0)
        val skipped = skipAtMost(count)
        if (skipped != count) throw IOException("Could not skip $count bytes, only $skipped bytes.")
    }

    override fun skipAtMost(count: Int): Int {
        require(count >= 0)
        val buffer = ByteBuffer.allocate(min(8 * 1024, count))
        var skipped = 0
        while (skipped < count) {
            buffer.position(0)
            buffer.limit(min(buffer.capacity(), count - skipped))
            val r = channel.read(buffer)
            if (r == -1) break
            bytesRead += r
            skipped += r
        }
        return skipped
    }

    override fun close() {
        channel.close()
    }
}

public fun ReadableByteChannel.asReadable(): Readable = NioByteChannelReadable(this)

internal class NioSeekableByteChannelReadable(private val channel: SeekableByteChannel): NioByteChannelReadable(channel), SeekableCursorReadable {
    override var position: Int
        get() = channel.position().toInt()
        set(value) {
            require(value <= channel.size()) { "Position $value is out of file (size: ${channel.size()})" }
            channel.position(value.toLong())
        }

    override val size: Int get() = channel.size().toInt()
    override val remaining: Int = channel.size().toInt() - position

    override fun requestCanRead(needed: Int) {
        if (needed > remaining) throw IOException("End of channel reached: needed $needed bytes, but has only $remaining remaining bytes.")
    }
}

public fun SeekableByteChannel.asReadable(): SeekableCursorReadable = NioSeekableByteChannelReadable(this)


internal open class NioByteChannelWriteable(private val channel: WritableByteChannel): Writeable, Closeable {
    private var buffer = ByteBuffer.allocate(8)

    private var bytesWritten = 0

    override val position: Int = bytesWritten

    override fun requestCanWrite(needed: Int) {} // noop

    private inline fun <T> writeValue(size: Int, value: T, putValue: ByteBuffer.(T) -> Unit) {
        buffer.clear()
        buffer.putValue(value)
        buffer.flip()
        channel.write(buffer)
        bytesWritten += size
    }

    override fun writeByte(value: Byte) = writeValue(1, value, ByteBuffer::put)
    override fun writeShort(value: Short) = writeValue(2, value, ByteBuffer::putShort)
    override fun writeInt(value: Int) = writeValue(4, value, ByteBuffer::putInt)
    override fun writeLong(value: Long) = writeValue(8, value, ByteBuffer::putLong)

    override fun writeBytes(src: ByteArray, srcOffset: Int, length: Int) {
        channel.write(ByteBuffer.wrap(src, srcOffset, length))
        bytesWritten += src.size
    }

    override fun writeBytes(src: ReadMemory) {
        val byteBuffer = when (val srcMemory = src.internalMemory()) {
            is ByteArrayMemory -> ByteBuffer.wrap(srcMemory.array, srcMemory.offset, srcMemory.size)
            is DirectByteBufferMemory -> srcMemory.byteBuffer
            else -> ByteBuffer.wrap(src.getBytes())
        }

        byteBuffer.position(0) {
            channel.write(byteBuffer)
        }
    }

    override fun writeBytes(src: Readable, length: Int) {
        if (src is MemoryReadable) {
            writeBytes(src.readSlice(length))
        } else {
            writeBytesBuffered(src, length)
        }
    }

    override fun flush() {
        if (channel is FileChannel) channel.force(true)
    }

    override fun close() {
        channel.close()
    }
}

public fun WritableByteChannel.asWriteable(): Writeable = NioByteChannelWriteable(this)

internal class NioSeekableByteChannelWriteable(private val channel: SeekableByteChannel): NioByteChannelWriteable(channel), CursorWriteable {
    override val remaining: Int get() = (channel.size() - channel.position()).toInt()
    override fun skip(count: Int) {
        channel.position(channel.position() + count)
    }
}

public fun SeekableByteChannel.asWriteable(): CursorWriteable = NioSeekableByteChannelWriteable(this)
