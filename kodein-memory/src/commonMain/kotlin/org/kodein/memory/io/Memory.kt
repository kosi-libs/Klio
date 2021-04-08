@file:OptIn(ExperimentalUnsignedTypes::class)
package org.kodein.memory.io


public interface Memory : ReadMemory {

    override val size: Int

    override fun slice(index: Int, length: Int): Memory

    public operator fun set(index: Int, value: Byte): Unit = putByte(index, value)
    public fun putByte(index: Int, value: Byte)
    public fun putShort(index: Int, value: Short)
    public fun putInt(index: Int, value: Int)
    public fun putLong(index: Int, value: Long)

    public fun putBytes(index: Int, src: ByteArray, srcOffset: Int, length: Int)
    public fun putBytes(index: Int, src: ReadMemory)
    public fun putBytes(index: Int, src: Readable, length: Int)

    public fun fill(byte: Byte)

    override fun internalMemory(): Memory

    public companion object
}

public fun Memory.sliceAt(index: Int): Memory = slice(index, size - index)

public fun Memory.putBytes(index: Int, src: ByteArray): Unit = putBytes(index, src, 0, src.size)

public fun Memory.putBytes(index: Int, src: CursorReadable): Int {
    val count = src.remaining
    putBytes(index, src, count)
    return count
}

public fun Memory.putFloat(index: Int, value: Float): Unit = putInt(index, value.toBits())
public fun Memory.putDouble(index: Int, value: Double): Unit = putLong(index, value.toBits())

public fun Memory.putUByte(index: Int, value: UByte): Unit = putByte(index, value.toByte())
public fun Memory.putUShort(index: Int, value: UShort): Unit = putShort(index, value.toShort())
public fun Memory.putUInt(index: Int, value: UInt): Unit = putInt(index, value.toInt())
public fun Memory.putULong(index: Int, value: ULong): Unit = putLong(index, value.toLong())
public fun Memory.putUBytes(index: Int, src: UByteArray, srcOffset: Int, length: Int): Unit =
    putBytes(index, src.asByteArray(), srcOffset, length)
public fun Memory.putUBytes(index: Int, src: UByteArray): Unit =
    putBytes(index, src.asByteArray(), 0, src.size)

public fun Memory.Companion.wrap(array: ByteArray, offset: Int, size: Int): ByteArrayMemory = ByteArrayMemory(array, offset, size)
public fun Memory.Companion.wrap(array: ByteArray): ByteArrayMemory = ByteArrayMemory(array, 0, array.size)

public fun ByteArray.asMemory(offset: Int, size: Int): ByteArrayMemory = Memory.wrap(this, offset, size)
public fun ByteArray.asMemory(): ByteArrayMemory = Memory.wrap(this)
public fun UByteArray.asMemory(offset: Int, size: Int): ByteArrayMemory = Memory.wrap(this.asByteArray(), offset, size)
public fun UByteArray.asMemory(): ByteArrayMemory = Memory.wrap(this.asByteArray())

public fun Memory.Companion.array(size: Int): ByteArrayMemory = ByteArrayMemory(ByteArray(size))

public fun Memory.Companion.arrayCopy(src: ReadMemory): ByteArrayMemory =
    array(src.size).apply { putBytes(0, src) }

public fun Memory.Companion.arrayCopy(src: Readable, length: Int): ByteArrayMemory =
    array(length).apply { putBytes(0, src, length) }

public fun Memory.Companion.arrayCopy(src: CursorReadable): ByteArrayMemory =
    array(src.remaining).apply { putBytes(0, src) }

public inline fun Memory.Companion.array(size: Int, write: Writeable.() -> Unit): ByteArrayMemory {
    val memory = Memory.array(size)
    val length = memory.write { write() }
    return if (size == length) memory else memory.slice(0, length)
}

public inline fun Memory.slice(index: Int = 0, write: CursorWriteable.() -> Unit): Memory {
    val length = write { write() }
    return slice(index, length)
}

public inline fun <M : AbstractMemory<M>> M.slice(index: Int = 0, write: CursorWriteable.() -> Unit): M {
    val length = write { write() }
    return slice(index, length)
}
