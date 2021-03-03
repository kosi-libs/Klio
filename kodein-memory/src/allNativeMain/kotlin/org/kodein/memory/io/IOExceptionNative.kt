package org.kodein.memory.io

import kotlinx.cinterop.toKString
import platform.posix.errno
import platform.posix.strerror

public actual open class IOException actual constructor(msg: String) : Exception(msg) {

    public companion object {
        public fun fromErrno(type: String): IOException = IOException(strerror(errno)?.toKString() ?: "Unknown $type error")
    }
}
