package org.kodein.memory.util

import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import platform.posix.gettimeofday
import platform.posix.timeval

@OptIn(ExperimentalUnsignedTypes::class)
actual fun currentTimestampMillis(): Long {
    memScoped {
        val tv = alloc<timeval>()
        gettimeofday(tv.ptr, null)
        return (tv.tv_sec * 1000L) + (tv.tv_usec / 1000L)
    }
}
