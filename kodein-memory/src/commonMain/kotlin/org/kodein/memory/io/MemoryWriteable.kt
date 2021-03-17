package org.kodein.memory.io


public class MemoryWriteable(public val memory: Memory) : CursorWriteable {

    override var position: Int = 0 ; private set

    private val remaining: Int get() = memory.size - position

    override fun requestCanWrite(needed: Int) {
        if (needed > remaining) throw IOException("Needed at least $needed remaining bytes, but has only $remaining bytes.")
    }

    private inline fun <T> writeValue(size: Int, value: T, setValue: Memory.(Int, T) -> Unit) {
        memory.setValue(position, value)
        position += size
    }

    override fun writeByte(value: Byte): Unit = writeValue(1, value, Memory::setByte)

    override fun writeShort(value: Short): Unit = writeValue(2, value, Memory::setShort)

    override fun writeInt(value: Int): Unit = writeValue(4, value, Memory::setInt)

    override fun writeLong(value: Long): Unit = writeValue(8, value, Memory::setLong)

    override fun writeBytes(src: ByteArray, srcOffset: Int, length: Int) {
        memory.setBytes(position, src, srcOffset, length)
        position += length
    }

    override fun writeBytes(src: ReadMemory, srcOffset: Int, length: Int) {
        memory.setBytes(position, src, srcOffset, length)
        position += length
    }

    override fun writeBytes(src: Readable, length: Int) {
        memory.setBytes(position, src, length)
        position += length
    }

    override fun flush() {}

    override fun skip(count: Int) {
        require(count >= 0) { "count: $count < 0." }
        requestCanWrite(count)
        position += count
    }
}

public fun Memory.asWriteable(): MemoryWriteable = MemoryWriteable(this)

public inline fun Memory.write(index: Int = 0, block: CursorWriteable.() -> Unit): Int {
    val w = slice(index).asWriteable()
    w.block()
    return w.position
}
