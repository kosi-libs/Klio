@file:Suppress("NOTHING_TO_INLINE")
package org.kodein.memory.io

import org.kodein.memory.internal.PLATFORM_BIG_ENDIAN
import org.kodein.memory.internal.UNALIGNED_ACCESS_ALLOWED

@PublishedApi
internal const val platformBigEndian: Boolean = (PLATFORM_BIG_ENDIAN == 1)

internal const val unalignedAccessAllowed: Boolean = (UNALIGNED_ACCESS_ALLOWED == 1)

public inline fun Short.toBigEndian(): Short = when {
    platformBigEndian -> this
    else -> swapEndian(this)
}

public inline fun Int.toBigEndian(): Int = when {
    platformBigEndian -> this
    else -> swapEndian(this)
}

public inline fun Long.toBigEndian(): Long = when {
    platformBigEndian -> this
    else -> swapEndian(this)
}
