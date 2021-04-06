package org.kodein.memory.io

import org.kodein.memory.Closeable
import java.io.InputStream
import java.io.OutputStream


internal class WriteableOutputStream(private val writeable: Writeable) : OutputStream() {
    override fun write(b: Int) = writeable.writeByte(b.toByte())

    override fun write(b: ByteArray) = writeable.writeBytes(b)

    override fun write(b: ByteArray, off: Int, len: Int) = writeable.writeBytes(b, off, len)

    override fun close() {
        if (writeable is Closeable)
            writeable.close()
    }
}

public fun Writeable.asOutputStream(): OutputStream = WriteableOutputStream(this)


internal class ReadableInputStream(private val readable: Readable) : InputStream() {

    private var mark = -1

    override fun available(): Int = if (readable.valid()) 1 else 0 // TODO use real available when fake available has been renamed to remaining.

    override fun read(): Int = if (readable.valid()) readable.readByte().toInt() else -1

    override fun read(b: ByteArray): Int = readable.tryReadBytes(b)

    override fun read(b: ByteArray, off: Int, len: Int): Int = readable.tryReadBytes(b, off, len)

    override fun skip(n: Long): Long = readable.skipAtMost(n.toInt()).toLong()

    override fun markSupported() = readable is CursorReadable

    override fun mark(readlimit: Int) {
        if (readable !is SeekableCursorReadable) throw IOException("Mark is not supported on ${readable::class.simpleName}.")
        mark = readable.position
    }

    override fun reset() {
        if (readable !is SeekableCursorReadable) throw IOException("Mark is not supported on ${readable::class.simpleName}.")
        if (mark == -1) throw IOException("Mark has not been set")
        readable.position = mark
    }

    override fun close() {
        if (readable is Closeable)
            readable.close()
    }
}

public fun Readable.asInputStream(): InputStream = ReadableInputStream(this)


internal class InputStreamReadable(private val stream: InputStream): Readable, Closeable {

    private var valid = true

    private var buffer = ByteArray(8)

    override var position: Int = 0
        private set

    override fun requestCanRead(needed: Int) {} // noop

    override fun valid(): Boolean = valid

    override fun tryReadByte(): Int {
        val r = stream.read()
        if (r == -1) valid = false
        else position += 1
        return r
    }

    override fun tryReadBytes(dst: ByteArray, dstOffset: Int, length: Int): Int {
        require(dstOffset >= 0)
        require(dstOffset + length <= dst.size)
        val r = stream.read(dst, dstOffset, length)
        if (r == -1) valid = false
        else position += r
        return r
    }

    override fun tryReadBytes(dst: Memory): Int {
        val dstMemory = dst.internalMemory()
        if (dstMemory is ByteArrayMemory) {
            val r = stream.read(dstMemory.array, dstMemory.offset, dstMemory.size)
            if (r == -1) valid = false
            else position += r
            return r
        } else {
            val buffer = ByteArray(dst.size)
            val r = tryReadBytes(buffer)
            if (r == -1) dst.putBytes(0, buffer, 0, r)
            return r
        }
    }

    override fun readByte(): Byte {
        val b = tryReadByte()
        if (b == -1) throw IOException("Stream is over.")
        return b.toByte()
    }

    private inline fun <T> readValue(size: Int, loadValue: ((Int) -> Byte) -> T): T {
        val n = stream.readNBytes(buffer, 0, size)
        position += n
        if (n != size) throw IOException("Stream is over. Needed $size bytes, but only got $n.")
        return loadValue { buffer[it] }
    }

    override fun readShort(): Short = readValue(2, ::slowLoadShort)

    override fun readInt(): Int = readValue(4, ::slowLoadInt)

    override fun readLong(): Long = readValue(8, ::slowLoadLong)

    override fun readBytes(dst: ByteArray, dstOffset: Int, length: Int) {
        require(dstOffset >= 0)
        require(dstOffset + length <= dst.size)

        var read = 0
        while (read < length) {
            val r = stream.read(dst, dstOffset + read, length - read)
            if (r == -1) throw IOException("Stream is over. Needed $length bytes, but only got $read.")
            read += r
            position += r
        }
    }

    override fun skip(count: Int) {
        require(count >= 0)
        val n = stream.skip(count.toLong()).toInt()
        position += n
        if (n != count) throw IOException("Could not skip $count bytes, skipped only $n.")
    }

    override fun skipAtMost(count: Int): Int {
        require(count >= 0)
        return stream.skip(count.toLong()).toInt()
    }

    override fun close() {
        stream.close()
    }
}

public fun InputStream.asReadable(): Readable = InputStreamReadable(this)


internal class OutputStreamWriteable(private val stream: OutputStream) : Writeable, Closeable {
    override var position: Int = 0
        private set

    override fun requestCanWrite(needed: Int) {} // noop

    override fun writeByte(value: Byte) {
        stream.write(value.toInt())
        position += 1
    }

    private fun <T> writeValue(size: Int, value: T, store: (T, (Int, Byte) -> Unit) -> Unit) {
        store(value) { _, b -> stream.write(b.toInt()) }
        position += size
    }

    override fun writeShort(value: Short) = writeValue(2, value, ::slowStoreShort)

    override fun writeInt(value: Int) = writeValue(4, value, ::slowStoreInt)

    override fun writeLong(value: Long) = writeValue(8, value, ::slowStoreLong)

    override fun writeBytes(src: ByteArray, srcOffset: Int, length: Int) {
        stream.write(src, srcOffset, length)
        position += length
    }

    override fun writeBytes(src: ReadMemory) {
        val srcMemory = src.internalMemory()
        if (srcMemory is ByteArrayMemory) {
            stream.write(srcMemory.array, srcMemory.offset, srcMemory.size)
        } else {
            val buffer = src.getBytes()
            stream.write(buffer)
        }
        position += src.size
    }

    override fun writeBytes(src: Readable, length: Int) {
        if (src is MemoryReadable) {
            writeBytes(src.readMemory(length))
        } else {
            writeBytesBuffered(src, length)
        }
    }

    override fun flush() {
        stream.flush()
    }

    override fun close() {
        stream.close()
    }

}

public fun OutputStream.asWriteable(): Writeable = OutputStreamWriteable(this)
