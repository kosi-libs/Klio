package org.kodein.memory.file

import kotlinx.cinterop.*
import org.kodein.memory.io.IOException
import platform.posix.*
import platform.windows.*

@OptIn(ExperimentalUnsignedTypes::class)
fun getLastErrorMessage(): String? {
    memScoped {
        val lpstr = alloc<LPWSTRVar>()
        val ret = FormatMessageW(
                (
                        FORMAT_MESSAGE_ALLOCATE_BUFFER or
                        FORMAT_MESSAGE_FROM_SYSTEM or
                        FORMAT_MESSAGE_IGNORE_INSERTS
                ).toUInt(),
                null,
                GetLastError(),
                LANG_USER_DEFAULT,
                lpstr.ptr.reinterpret(),
                0u,
                null
        )

        if (ret == 0u) return null

        val msg = lpstr.value?.toKString() ?: return null
        return msg.substring(0, msg.length - 2)
    }
}

fun IOException.Companion.fromLastError(type: String) = IOException(getLastErrorMessage() ?: "Unknown $type error")

@OptIn(ExperimentalUnsignedTypes::class)
actual object FileSystem {
    internal actual val pathSeparator: String = "\\"

    actual val tempDirectory: Path by lazy {
        memScoped {
            val lpstr = allocArray<WCHARVar>(MAX_PATH + 1)

            val length = GetTempPathW((MAX_PATH + 1).convert(), lpstr)

            if (length == 0u || length > MAX_PATH.toUInt()) {
                throw IOException.fromLastError("file system")
            }

            Path(lpstr.toKString())
        }
    }

    actual var currentDirectory: Path
        get() {
            memScoped {
                val lpstr = allocArray<WCHARVar>(PATH_MAX + 1)
                val ret = GetCurrentDirectoryW((PATH_MAX + 1).toUInt(), lpstr)
                if (ret == 0u) throw IOException.fromLastError("file system")
                return Path(lpstr.toKString())
            }
        }
        set(value) {
            SetCurrentDirectoryW(value.path)
        }

    private fun getRoots(size: Int): List<Path> {
        memScoped {
            val lpstr = allocArray<WCHARVar>(size)
            val length = GetLogicalDriveStringsW(size.toUInt(), lpstr).toInt()
            if (length == 0) throw IOException.fromLastError("file system")
            if (length > 128) return getRoots(length)

            return sequence {
                var index = 0
                while (index < length) {
                    val drive = (lpstr + index)!!.toKString()
                    yield(Path(drive))
                    index += drive.length + 1
                }
            }.toList()
        }
    }

    actual val roots: List<Path> by lazy { getRoots(129) }
}
