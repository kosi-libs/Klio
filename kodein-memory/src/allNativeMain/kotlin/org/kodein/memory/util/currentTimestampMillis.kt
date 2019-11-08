package org.kodein.memory.util

import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import platform.linux.ftime
import platform.linux.timeb

@UseExperimental(ExperimentalUnsignedTypes::class)
actual fun currentTimestampMillis(): Long {
    memScoped {
        val time = alloc<timeb>()
        ftime(time.ptr)

        return time.time * 1000L + time.millitm.toLong()
    }
}
