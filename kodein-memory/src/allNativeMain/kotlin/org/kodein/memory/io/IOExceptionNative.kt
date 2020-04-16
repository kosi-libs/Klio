package org.kodein.memory.io

import kotlinx.cinterop.toKString
import platform.posix.errno
import platform.posix.strerror

actual class IOException actual constructor(msg: String) : Exception(msg) {

    companion object {
        fun fromErrno(type: String) = IOException(strerror(errno)?.toKString() ?: "Unknown $type error")
    }
}
