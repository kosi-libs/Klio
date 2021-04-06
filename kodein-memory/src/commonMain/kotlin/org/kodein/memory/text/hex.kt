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
public fun String.fromHex(dst: UByteArray, dstOffset: Int = 0) {
    require(length % 2 == 0) { "Bad hex string (length % 2 != 0)" }
    require(dstOffset + (length / 2) <= dst.size) { "Array too small (need ${(length / 2)} bytes at offset $dstOffset, but only has ${dst.size} total bytes)." }

    chunked(2).forEachIndexed { i, str ->
        dst[i] = str.toUByte(radix = 16)
    }
}

@OptIn(ExperimentalUnsignedTypes::class)
public fun String.fromHex(dst: ByteArray, dstOffset: Int = 0): Unit = fromHex(dst.asUByteArray(), dstOffset)

@OptIn(ExperimentalUnsignedTypes::class)
public fun String.fromHex(dst: Memory) {
    require(length % 2 == 0) { "Bad hex string (length % 2 != 0)" }
    require(length / 2 <= dst.size) { "Memory too small (need ${(length / 2)} bytes, but only has ${dst.size} bytes)." }

    chunked(2).forEachIndexed { i, str ->
        dst[i] = str.toUByte(radix = 16).toByte()
    }
}

public fun String.fromHex(): ByteArray {
    require(length % 2 == 0) { "Bad hex string (length % 2 != 0)" }
    val dst = ByteArray(length / 2)
    fromHex(dst)
    return dst
}
