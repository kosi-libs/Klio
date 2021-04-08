package org.kodein.memory.crypto

import kotlinx.cinterop.*
import org.kodein.memory.io.*
import org.kodein.memory.*


@OptIn(ExperimentalUnsignedTypes::class)
internal abstract class NativeDigestWriteable(override val digestSize: Int) : DigestWriteable {

    private var isClosed = false
    private var isFinalized = false

    internal abstract fun doReset()
    internal abstract fun doUpdate(dataPtr: CPointer<*>, dataLength: Int)
    internal abstract fun doFinal(outputPtr: CPointer<*>)
    internal abstract fun doClose()

    private val buffer = Allocation.native(8)

    private var bytesWritten: Int = 0
    override val position: Int get() = bytesWritten

    override fun requestCanWrite(needed: Int) {}

    private fun checkStatus(withFinalized: Boolean) {
        check(!isClosed) { "Digest closed." }
        if (withFinalized) check(!isFinalized) { "Digest finalized. Call reset to restart a new one." }
    }

    override fun writeBytes(src: ByteArray, srcOffset: Int, length: Int) {
        require(srcOffset >= 0) { "srcOffset: $srcOffset < 0" }
        require(length >= 0) { "length: $length < 0" }
        require(srcOffset + length <= src.size) { "srcOffset: $srcOffset + length: $length > src.size: ${src.size}" }
        checkStatus(true)
        src.usePinned {
            doUpdate(it.addressOf(srcOffset), length)
        }
        bytesWritten += length
    }

    override fun writeBytes(src: ReadMemory) {
        if (src is ByteArrayMemory) writeBytes(src.array, src.offset, src.size)
        else writeBytes(src.getBytes())
    }

    override fun writeBytes(src: Readable, length: Int) {
        require(length >= 0)
        if (src is MemoryReadable) writeBytes(src.readMemory(length))
        else writeBytesBuffered(src, length)
    }

    private inline fun <T> writeValue(size: Int, value: T, setValue: Memory.(Int, T) -> Unit) {
        checkStatus(true)
        buffer.setValue(0, value)
        doUpdate(buffer.memory.pointer, size)
        bytesWritten += size
    }

    override fun writeByte(value: Byte): Unit = writeValue(1, value, Memory::putByte)
    override fun writeShort(value: Short): Unit = writeValue(2, value, Memory::putShort)
    override fun writeInt(value: Int): Unit = writeValue(4, value, Memory::putInt)
    override fun writeLong(value: Long): Unit = writeValue(8, value, Memory::putLong)

    override fun flush() {}

    override fun digestInto(dst: ByteArray, dstOffset: Int) {
        require(dstOffset >= 0)
        require(dst.size >= digestSize + dstOffset) { "Memory is too small" }
        checkStatus(true)
        dst.usePinned {
            doFinal(it.addressOf(dstOffset))
            isFinalized = true
        }
    }

    override fun digestInto(dst: Memory) {
        require(dst.size >= digestSize) { "Memory is too small (need at least $digestSize bytes)" }
        checkStatus(true)
        when (dst) {
            is ByteArrayMemory -> digestInto(dst.array, dst.offset)
            is CPointerMemory -> {
                doFinal(dst.pointer)
                isFinalized = true
            }
            else -> {
                Allocation.native(digestSize).use { buffer ->
                    doFinal(buffer.memory.pointer)
                    dst.putBytes(0, buffer)
                }
            }
        }
    }

    override fun digestInto(dst: Writeable) {
        when (dst) {
            is MemoryWriteable -> dst.writeMemory { dstMemory ->
                digestInto(dstMemory)
                digestSize
            }
            else -> {
                Allocation.native(digestSize).use { buffer ->
                    doFinal(buffer.memory.pointer)
                    dst.writeBytes(buffer)
                }
            }
        }
    }

    override fun close() {
        if (isClosed) return
        isClosed = true
        buffer.close()
        doClose()
    }

    override fun reset() {
        checkStatus(false)
        doReset()
        bytesWritten = 0
    }
}
