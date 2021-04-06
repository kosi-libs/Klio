package org.kodein.memory.crypto

import kotlinx.cinterop.*
import org.kodein.memory.io.*


internal typealias DigestInit<C> = (CValuesRef<C>) -> Unit
internal typealias DigestUpdate<C> = (CValuesRef<C>, CValuesRef<*>, Int) -> Unit
internal typealias DigestFinal<C> = (CValuesRef<C>, CPointer<*>) -> Unit

@OptIn(ExperimentalUnsignedTypes::class)
internal class NativeDigestWriteable<C : CVariable>(
    override val digestSize: Int,
    ctx: C,
    private val init: DigestInit<C>,
    private val update: DigestUpdate<C>,
    private val final: DigestFinal<C>,
    private val onClose: () -> Unit = {}
) : DigestWriteable {

    private var finalized = false

    private var ctx: C? = ctx

    init {
        init(ctx.ptr)
    }

    private fun ctx(): C {
        val ctx = ctx
        when {
            finalized -> error("Digest finalized. Call reset to restart a new one.")
            ctx == null -> error("Digest closed.")
            else -> return ctx
        }
    }

    private var buffer: Pair<CPointer<ByteVar>, CPointerMemory>? = null

    private var bytesWritten: Int = 0
    override val position: Int get() = bytesWritten

    override fun requestCanWrite(needed: Int) {}

    override fun writeByte(value: Byte) {
        memScoped {
            val byte = alloc<ByteVar>()
            byte.value = value
            update(ctx().ptr, byte.ptr, 1)
        }
        bytesWritten += 1
    }

    override fun writeBytes(src: ByteArray, srcOffset: Int, length: Int) {
        require(srcOffset >= 0) { "srcOffset: $srcOffset < 0" }
        require(length >= 0) { "length: $length < 0" }
        require(srcOffset + length <= src.size) { "srcOffset: $srcOffset + length: $length > src.size: ${src.size}" }

        src.usePinned {
            update(ctx().ptr, it.addressOf(srcOffset), length)
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
        if (buffer == null) {
            val ptr = nativeHeap.allocArray<ByteVar>(8)
            val mem = CPointerMemory(ptr, 8)
            buffer = ptr to mem
        }
        val (ptr, mem) = buffer!!
        mem.setValue(0, value)
        update(ctx().ptr, ptr, size)
        bytesWritten += size
    }

    override fun writeShort(value: Short): Unit = writeValue(2, value, Memory::putShort)
    override fun writeInt(value: Int): Unit = writeValue(4, value, Memory::putInt)
    override fun writeLong(value: Long): Unit = writeValue(8, value, Memory::putLong)

    override fun flush() {}

    override fun digestInto(dst: ByteArray, dstOffset: Int) {
        require(dstOffset >= 0)
        require(dst.size >= digestSize + dstOffset) { "Memory is too small" }
        val ctx = ctx()
        dst.usePinned {
            final(ctx.ptr, it.addressOf(dstOffset))
            finalized = true
        }
    }

    override fun digestInto(dst: Memory, dstOffset: Int) {
        require(dstOffset >= 0)
        require(dst.size >= digestSize + dstOffset) { "Memory is too small" }
        when (dst) {
            is ByteArrayMemory -> digestInto(dst.array, dst.offset + dstOffset)
            is CPointerMemory -> {
                val ctx = ctx()
                final(ctx.ptr, (dst.pointer + dstOffset)!!)
                finalized = true
            }
            else -> {
                val array = ByteArray(digestSize)
                digestInto(array)
                dst.putBytes(dstOffset, array)
            }
        }
    }

    override fun digestInto(dst: Writeable) {
        when (dst) {
            is MemoryWriteable -> digestInto(dst.memory, dst.position)
            else -> {
                val array = ByteArray(digestSize)
                digestInto(array)
                dst.writeBytes(array)
            }
        }
    }

    override fun close() {
        val ctx = ctx ?: return
        nativeHeap.free(ctx)
        this.ctx = null
        buffer?.let { nativeHeap.free(it.first) }
        buffer = null
        onClose()
    }

    override fun reset() {
        val ctx = ctx ?: error("Digest closed.")
        init(ctx.ptr)
        bytesWritten = 0
        finalized = false
    }
}
