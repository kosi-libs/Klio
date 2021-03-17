package org.kodein.memory.io


public interface Memory : ReadMemory {

    override val size: Int

    override fun slice(index: Int, length: Int): Memory

    public operator fun set(index: Int, value: Byte): Unit = setByte(index, value)
    public fun setByte(index: Int, value: Byte)
    public fun setShort(index: Int, value: Short)
    public fun setInt(index: Int, value: Int)
    public fun setLong(index: Int, value: Long)

    public fun setBytes(index: Int, src: ByteArray, srcOffset: Int = 0, length: Int = src.size - srcOffset)
    public fun setBytes(index: Int, src: ReadMemory, srcOffset: Int = 0, length: Int = src.size - srcOffset)
    public fun setBytes(index: Int, src: Readable, length: Int)

    public companion object
}

public fun Memory.setBytes(index: Int, src: CursorReadable): Int {
    val count = src.remaining
    setBytes(index, src, count)
    return count
}

public fun Memory.setFloat(index: Int, value: Float): Unit = setInt(index, value.toBits())
public fun Memory.setDouble(index: Int, value: Double): Unit = setLong(index, value.toBits())

@ExperimentalUnsignedTypes
public fun Memory.setUByte(index: Int, value: UByte): Unit = setByte(index, value.toByte())
@ExperimentalUnsignedTypes
public fun Memory.setUShort(index: Int, value: UShort): Unit = setShort(index, value.toShort())
@ExperimentalUnsignedTypes
public fun Memory.setUInt(index: Int, value: UInt): Unit = setInt(index, value.toInt())
@ExperimentalUnsignedTypes
public fun Memory.setULong(index: Int, value: ULong): Unit = setLong(index, value.toLong())
@ExperimentalUnsignedTypes
public fun Memory.setUBytes(index: Int, src: UByteArray, srcOffset: Int = 0, length: Int = src.size - srcOffset): Unit =
    setBytes(index, src.asByteArray(), srcOffset, length)

public fun Memory.Companion.wrap(array: ByteArray, offset: Int = 0, size: Int = array.size - offset): Memory =
    ByteArrayMemory(array, offset, size)

public fun Memory.Companion.array(size: Int): Memory =
    ByteArrayMemory(ByteArray(size))

public fun Memory.Companion.arrayCopy(src: ReadMemory, srcOffset: Int = 0, length: Int = src.size - srcOffset): Memory =
    array(length).apply { setBytes(0, src, srcOffset, length) }

public fun Memory.Companion.arrayCopy(src: Readable, length: Int): Memory =
    array(length).apply { setBytes(0, src, length) }

public fun Memory.Companion.arrayCopy(src: CursorReadable): Memory =
    array(src.remaining).apply { setBytes(0, src) }

public inline fun Memory.Companion.array(size: Int, write: Writeable.() -> Unit): ReadMemory {
    val memory = Memory.array(size)
    val length = memory.write { write() }
    return if (size == length) memory else memory.slice(0, length)
}

public inline fun Memory.slice(index: Int = 0, write: CursorWriteable.() -> Unit): Memory {
    val length = write { write() }
    return slice(index, length)
}
