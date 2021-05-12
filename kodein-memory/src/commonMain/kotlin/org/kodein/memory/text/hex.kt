package org.kodein.memory.text

import org.kodein.memory.io.*

@OptIn(ExperimentalUnsignedTypes::class)
public fun ByteArray.toHex(): String = asUByteArray().toHex()

@OptIn(ExperimentalUnsignedTypes::class)
public fun UByteArray.toHex(): String {
    val sb = StringBuilder(size * 2)
    for (i in indices) {
        sb.append(this[i].toString(radix = 16).padStart(2, '0'))
    }
    return sb.toString()
}

@OptIn(ExperimentalUnsignedTypes::class)
public fun ReadMemory.toHex(): String {
    val sb = StringBuilder(size * 2)
    for (i in indices) {
        sb.append(this[i].toUByte().toString(radix = 16).padStart(2, '0'))
    }
    return sb.toString()
}

@OptIn(ExperimentalUnsignedTypes::class)
public fun UByteArray.putHex(hex: String, dstOffset: Int = 0) {
    require(hex.length % 2 == 0) { "Bad hex string (length % 2 != 0)" }
    require(dstOffset + (hex.length / 2) <= size) { "Array too small (need ${(hex.length / 2)} bytes at offset $dstOffset, but only has $size total bytes)." }

    hex.chunked(2).forEachIndexed { i, str ->
        this[i] = str.toUByte(radix = 16)
    }
}

@OptIn(ExperimentalUnsignedTypes::class)
public fun ByteArray.putHex(hex: String, dstOffset: Int = 0): Unit = asUByteArray().putHex(hex, dstOffset)

@OptIn(ExperimentalUnsignedTypes::class)
public fun Memory.putHex(hex: String) {
    require(hex.length % 2 == 0) { "Bad hex string (length % 2 != 0)" }
    require(hex.length / 2 <= size) { "Memory too small (need ${(hex.length / 2)} bytes, but only has $size bytes)." }

    hex.chunked(2).forEachIndexed { i, str ->
        this[i] = str.toUByte(radix = 16).toByte()
    }
}

public fun Memory.Companion.arrayFromHex(hex: String): ByteArrayMemory {
    require(hex.length % 2 == 0) { "Bad hex string (length % 2 != 0)" }
    return Memory.array(hex.length / 2).apply { putHex(hex) }
}

public fun Allocation.Companion.nativeFromHex(hex: String): PlatformNativeAllocation {
    require(hex.length % 2 == 0) { "Bad hex string (length % 2 != 0)" }
    return Allocation.native(hex.length / 2).apply { putHex(hex) }
}

public fun byteArrayFromHex(hex: String): ByteArray {
    require(hex.length % 2 == 0) { "Bad hex string (length % 2 != 0)" }
    return ByteArray(hex.length / 2).apply { putHex(hex) }
}
