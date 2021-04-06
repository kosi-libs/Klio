@file:Suppress("NOTHING_TO_INLINE")
package org.kodein.memory.io


@PublishedApi
internal inline fun swapEndian(s: Short): Short =
    (((s.toInt() and 0xff) shl 8) or ((s.toInt() and 0xffff) ushr 8)).toShort()

@PublishedApi
internal inline fun swapEndian(s: Int): Int =
    (swapEndian((s and 0xffff).toShort()).toInt() shl 16) or (swapEndian((s ushr 16).toShort()).toInt() and 0xffff)

@PublishedApi
internal inline fun swapEndian(s: Long): Long =
    (swapEndian((s and 0xffffffff).toInt()).toLong() shl 32) or (swapEndian((s ushr 32).toInt()).toLong() and 0xffffffff)


public fun ReadMemory.getInt8(index: Int): Byte = getByte(index)
public fun ReadMemory.getInt16BE(index: Int): Short = getShort(index)
public fun ReadMemory.getInt16LE(index: Int): Short = swapEndian(getShort(index))
public fun ReadMemory.getInt32BE(index: Int): Int = getInt(index)
public fun ReadMemory.getInt32LE(index: Int): Int = swapEndian(getInt(index))
public fun ReadMemory.getInt64BE(index: Int): Long = getLong(index)
public fun ReadMemory.getInt64LE(index: Int): Long = swapEndian(getLong(index))
public fun ReadMemory.getFloat32BE(index: Int): Float = Float.fromBits(getInt32BE(index))
public fun ReadMemory.getFloat32LE(index: Int): Float = Float.fromBits(getInt32LE(index))
public fun ReadMemory.getFloat64BE(index: Int): Double = Double.fromBits(getInt64BE(index))
public fun ReadMemory.getFloat64LE(index: Int): Double = Double.fromBits(getInt64LE(index))

public fun Readable.readInt8(): Byte = readByte()
public fun Readable.readInt16BE(): Short = readShort()
public fun Readable.readInt16LE(): Short = swapEndian(readShort())
public fun Readable.readInt32BE(): Int = readInt()
public fun Readable.readInt32LE(): Int = swapEndian(readInt())
public fun Readable.readInt64BE(): Long = readLong()
public fun Readable.readInt64LE(): Long = swapEndian(readLong())
public fun Readable.readFloat32BE(): Float = Float.fromBits(readInt32BE())
public fun Readable.readFloat32LE(): Float = Float.fromBits(readInt32LE())
public fun Readable.readFloat64BE(): Double = Double.fromBits(readInt64BE())
public fun Readable.readFloat64LE(): Double = Double.fromBits(readInt64LE())

public fun Memory.putInt8(index: Int, value: Byte): Unit = putByte(index, value)
public fun Memory.putInt16BE(index: Int, value: Short): Unit = putShort(index, value)
public fun Memory.putInt16LE(index: Int, value: Short): Unit = putShort(index, swapEndian(value))
public fun Memory.putInt32BE(index: Int, value: Int): Unit = putInt(index, value)
public fun Memory.putInt32LE(index: Int, value: Int): Unit = putInt(index, swapEndian(value))
public fun Memory.putInt64BE(index: Int, value: Long): Unit = putLong(index, value)
public fun Memory.putInt64LE(index: Int, value: Long): Unit = putLong(index, swapEndian(value))
public fun Memory.putFloat32BE(index: Int, value: Float): Unit = putInt32BE(index, value.toBits())
public fun Memory.putFloat32LE(index: Int, value: Float): Unit = putInt32LE(index, value.toBits())
public fun Memory.putFloat64BE(index: Int, value: Double): Unit = putInt64BE(index, value.toBits())
public fun Memory.putFloat64LE(index: Int, value: Double): Unit = putInt64LE(index, value.toBits())

public fun Writeable.writeInt8(value: Byte): Unit = writeByte(value)
public fun Writeable.writeInt16BE(value: Short): Unit = writeShort(value)
public fun Writeable.writeInt16LE(value: Short): Unit = writeShort(swapEndian(value))
public fun Writeable.writeInt32BE(value: Int): Unit = writeInt(value)
public fun Writeable.writeInt32LE(value: Int): Unit = writeInt(swapEndian(value))
public fun Writeable.writeInt64BE(value: Long): Unit = writeLong(value)
public fun Writeable.writeInt64LE(value: Long): Unit = writeLong(swapEndian(value))
public fun Writeable.writeFloat32BE(value: Float): Unit = writeInt32BE(value.toBits())
public fun Writeable.writeFloat32LE(value: Float): Unit = writeInt32LE(value.toBits())
public fun Writeable.writeFloat64BE(value: Double): Unit = writeInt64BE(value.toBits())
public fun Writeable.writeFloat64LE(value: Double): Unit = writeInt64LE(value.toBits())
