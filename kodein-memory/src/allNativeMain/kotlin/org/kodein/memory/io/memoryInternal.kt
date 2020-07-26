@file:Suppress("NOTHING_TO_INLINE")

package org.kodein.memory.io

import org.kodein.memory.internal.PLATFORM_BIG_ENDIAN
import org.kodein.memory.internal.UNALIGNED_ACCESS_ALLOWED

@PublishedApi
internal const val platformBigEndian: Boolean = (PLATFORM_BIG_ENDIAN == 1)

internal const val unalignedAccessAllowed: Boolean = (UNALIGNED_ACCESS_ALLOWED == 1)

public inline fun Short.toBigEndian(): Short = when {
    platformBigEndian -> this
    else -> swap(this)
}

public inline fun Int.toBigEndian(): Int = when {
    platformBigEndian -> this
    else -> swap(this)
}

public inline fun Long.toBigEndian(): Long = when {
    platformBigEndian -> this
    else -> swap(this)
}

@PublishedApi
internal inline fun swap(s: Short): Short =
        (((s.toInt() and 0xff) shl 8) or ((s.toInt() and 0xffff) ushr 8)).toShort()

@PublishedApi
internal inline fun swap(s: Int): Int =
        (swap((s and 0xffff).toShort()).toInt() shl 16) or (swap((s ushr 16).toShort()).toInt() and 0xffff)

@PublishedApi
internal inline fun swap(s: Long): Long =
        (swap((s and 0xffffffff).toInt()).toLong() shl 32) or (swap((s ushr 32).toInt()).toLong() and 0xffffffff)
