package org.kodein.memory.util

import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import platform.posix.*

@OptIn(ExperimentalUnsignedTypes::class)
actual fun currentTimestampMillis(): Long {
    memScoped {
        val ts = alloc<timespec>()
        clock_gettime(CLOCK_REALTIME, ts.ptr)
        @Suppress("RemoveRedundantCallsOfConversionMethods")
        return (ts.tv_sec.toLong() * 1_000L) + (ts.tv_nsec.toLong() / 1_000_000L)
    }
}
