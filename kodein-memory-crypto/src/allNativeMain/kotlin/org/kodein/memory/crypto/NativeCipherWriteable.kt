package org.kodein.memory.crypto

import kotlinx.cinterop.*
import org.kodein.memory.io.*


@OptIn(ExperimentalUnsignedTypes::class)
internal abstract class NativeCipherWriteable(private val key: Allocation, private val output: Writeable) : CipherWriteable {

    private var bytesWritten = 0

    override val position: Int get() = bytesWritten

    private val buffer = Allocation.native(8)

    internal abstract fun doUpdate(inputPtr: CPointer<*>, inputLength: Int, outputPtr: CPointer<*>, outputSize: Int): Int
    internal abstract fun doFinal(outputPtr: CPointer<*>, outputSize: Int): Int
    internal abstract fun doClose()

    override fun requestCanWrite(needed: Int) {}

    override fun writeByte(value: Byte) {
        buffer.putByte(0, value)
        update(buffer.memory.pointer, 1)
    }

    override fun writeShort(value: Short) {
        buffer.putShort(0, value)
        update(buffer.memory.pointer, 2)
    }

    override fun writeInt(value: Int) {
        buffer.putInt(0, value)
        update(buffer.memory.pointer, 4)
    }

    override fun writeLong(value: Long) {
        buffer.putLong(0, value)
        update(buffer.memory.pointer, 8)
    }

    override fun writeBytes(src: ByteArray, srcOffset: Int, length: Int) {
        require(srcOffset + length <= src.size)
        src.usePinned { pinned ->
            update(pinned.addressOf(srcOffset), length)
        }
    }

    override fun writeBytes(src: ReadMemory) {
        when (val srcMemory = src.internalMemory()) {
            is ByteArrayMemory -> writeBytes(srcMemory.array, srcMemory.offset, srcMemory.size)
            is CPointerMemory -> update(srcMemory.pointer, srcMemory.size)
            else -> writeBytes(srcMemory.getBytes())
        }
    }

    override fun writeBytes(src: Readable, length: Int) {
        if (src is MemoryReadable) writeBytes(src.readSlice(length))
        else writeBytes(src.readBytes(length))
    }

    private fun update(srcPtr: CPointer<*>, srcLength: Int) =
        output.writeNative(srcLength + 16) {
            doUpdate(srcPtr, srcLength, it.pointer, it.size)
        }

    override fun flush() {}

    override fun close() {
        output.requestCanWrite(16)
        output.writeNative(16) {
            doFinal(it.pointer, it.size)
        }

        key.fill(0)
        key.close()
        buffer.close()

        doClose()
    }
}
