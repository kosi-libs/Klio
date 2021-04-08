package org.kodein.memory.io


public class MemoryWriteable(public val memory: Memory) : CursorWriteable {

    override var position: Int = 0 ; private set

    override val remaining: Int get() = memory.size - position

    public inline fun writeMemory(crossinline block: (Memory) -> Int): Int {
        val slice = memory.sliceAt(position)
        val count = block(slice)
        if (count > 0) skip(count)
        return count
    }

    override fun requestCanWrite(needed: Int) {
        if (needed > remaining) throw IOException("Needed at least $needed remaining bytes, but has only $remaining bytes.")
    }

    private inline fun <T> writeValue(size: Int, value: T, putValue: Memory.(Int, T) -> Unit) {
        memory.putValue(position, value)
        position += size
    }

    override fun writeByte(value: Byte): Unit = writeValue(1, value, Memory::putByte)

    override fun writeShort(value: Short): Unit = writeValue(2, value, Memory::putShort)

    override fun writeInt(value: Int): Unit = writeValue(4, value, Memory::putInt)

    override fun writeLong(value: Long): Unit = writeValue(8, value, Memory::putLong)

    override fun writeBytes(src: ByteArray, srcOffset: Int, length: Int) {
        memory.putBytes(position, src, srcOffset, length)
        position += length
    }

    override fun writeBytes(src: ReadMemory) {
        memory.putBytes(position, src)
        position += src.size
    }

    override fun writeBytes(src: Readable, length: Int) {
        memory.putBytes(position, src, length)
        position += length
    }

    override fun flush() {}

    override fun skip(count: Int) {
        require(count >= 0) { "count: $count < 0." }
        requestCanWrite(count)
        position += count
    }

    override fun toString(): String = "MemoryWriteable(memory=${memory}, position=$position)"
}

public fun Memory.asWriteable(): MemoryWriteable = MemoryWriteable(this)

public inline fun Memory.write(index: Int = 0, block: CursorWriteable.() -> Unit): Int {
    val w = sliceAt(index).asWriteable()
    w.block()
    return w.position
}
