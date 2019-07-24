package org.kodein.memory.io

import org.kodein.memory.Closeable
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

class WriteableOutputStream(val writeable: Writeable, val transientClose: Boolean = false) : OutputStream() {
    override fun write(b: Int) = writeable.put(b.toByte())

    override fun write(b: ByteArray) = writeable.putBytes(b)

    override fun write(b: ByteArray, off: Int, len: Int) = writeable.putBytes(b, off, len)

    override fun close() {
        if (transientClose && writeable is Closeable)
            writeable.close()
    }
}

fun Writeable.asOuputStream(transientClose: Boolean = false) = WriteableOutputStream(this, transientClose)


class ReadableInputStream(val readable: Readable, val transientClose: Boolean = false) : InputStream() {

    private var mark = -1

    override fun available(): Int = readable.remaining

    override fun read(): Int = if (readable.hasRemaining()) readable.read().toInt() else -1

    override fun read(b: ByteArray): Int = readable.readAtMostBytes(b)

    override fun read(b: ByteArray, off: Int, len: Int): Int = readable.readAtMostBytes(b, off, len)

    override fun skip(n: Long): Long = readable.skipAtMost(n.toInt()).toLong()

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

fun Readable.asInputStream(transientClose: Boolean = false) = ReadableInputStream(this, transientClose)
