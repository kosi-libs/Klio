package org.kodein.memory.io

import org.kodein.memory.Closeable
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

internal class WriteableOutputStream(private val writeable: Writeable, private val transientClose: Boolean = false) : OutputStream() {
    override fun write(b: Int) = writeable.putByte(b.toByte())

    override fun write(b: ByteArray) = writeable.putBytes(b)

    override fun write(b: ByteArray, off: Int, len: Int) = writeable.putBytes(b, off, len)

    override fun close() {
        if (transientClose && writeable is Closeable)
            writeable.close()
    }
}

public fun Writeable.asOuputStream(transientClose: Boolean = false): OutputStream = WriteableOutputStream(this, transientClose)

internal class ReadableInputStream(private val readable: Readable, private val transientClose: Boolean = false) : InputStream() {

    private var mark = -1

    override fun available(): Int = if (readable.valid()) 1 else 0 // TODO use real available when fake available has been renamed to remaining.

    override fun read(): Int = if (readable.valid()) readable.readByte().toInt() else -1

    override fun read(b: ByteArray): Int = readable.receive(b)

    override fun read(b: ByteArray, off: Int, len: Int): Int = readable.receive(b, off, len)

    override fun skip(n: Long): Long = readable.skip(n.toInt()).toLong()

    override fun markSupported() = readable is ReadBuffer

    override fun mark(readlimit: Int) {
        if (readable is ReadBuffer) {
            mark = readable.position
        }
    }

    override fun reset() {
        if (readable is ReadBuffer) {
            if (mark == -1) throw IOException("Mark has not been set")
            readable.position = mark
        }
    }

    override fun close() {
        if (transientClose && readable is Closeable)
            readable.close()
    }
}

public fun Readable.asInputStream(transientClose: Boolean = false): InputStream = ReadableInputStream(this, transientClose)


internal class InputStreamReadable(private val stream: InputStream): Readable {

    override var position: Int = 0
        private set

    private var valid = true

    override fun requireCanRead(needed: Int) {}  // noop

    override fun valid(): Boolean = valid

    override fun receive(): Int {
        val r = stream.read()
        if (r == -1) valid = false
        else position += 1
        return r
    }

    override fun receive(dst: ByteArray, dstOffset: Int, length: Int): Int {
        val r = stream.read(dst, dstOffset, length)
        if (r == -1) valid = false
        else position += r
        return r
    }

    override fun readByte(): Byte {
        val b = stream.read()
        if (b == -1) throw IOException("Stream is over")
        position += 1
        return b.toByte()
    }

    override fun readChar(): Char = readShort().toChar()

    override fun readShort(): Short = slowLoadShort { readByte() }

    override fun readInt(): Int = slowLoadInt { readByte() }

    override fun readLong(): Long = slowLoadLong { readByte() }

    override fun readFloat(): Float = Float.fromBits(readInt())

    override fun readDouble(): Double = Double.fromBits(readLong())

    override fun readBytes(dst: ByteArray, dstOffset: Int, length: Int) {
        require(dstOffset >= 0)
        require(dstOffset + length <= dst.size)

        var read = 0
        while (read < length) {
            val r = stream.read(dst, dstOffset + read, length - read)
            if (r == -1) throw IOException("Stream is over")
            read += r
            position += r
        }
    }

    override fun skip(count: Int): Int = stream.skip(count.toLong()).toInt()

    override fun internalBuffer(): Readable = this
}

public fun InputStream.asReadable(): Readable = InputStreamReadable(this)


internal class OutputStreamWriteable(private val stream: OutputStream) : Writeable {

    override var position: Int = 0
        private set

    override fun requireCanWrite(needed: Int) {} // noop

    override fun putByte(value: Byte) {
        stream.write(value.toInt())
        position += 1
    }

    override fun putChar(value: Char) = putShort(value.toShort())

    override fun putShort(value: Short) = slowStoreShort(value) { _, b -> putByte(b) }

    override fun putInt(value: Int) = slowStoreInt(value) { _, b -> putByte(b) }

    override fun putLong(value: Long) = slowStoreLong(value) { _, b -> putByte(b) }

    override fun putFloat(value: Float) = putInt(value.toBits())

    override fun putDouble(value: Double) = putLong(value.toBits())

    override fun putBytes(src: ByteArray, srcOffset: Int, length: Int) {
        stream.write(src, srcOffset, length)
        position += length
    }

    override fun putMemoryBytes(src: ReadMemory, srcOffset: Int, length: Int) {
        repeat(length) { putByte(src[srcOffset + it]) }
    }

    override fun putReadableBytes(src: Readable, length: Int) {
        repeat(length) { putByte(src.readByte()) }
    }

    override fun flush() {
        stream.flush()
    }

}

public fun OutputStream.asWriteable(): Writeable = OutputStreamWriteable(this)
