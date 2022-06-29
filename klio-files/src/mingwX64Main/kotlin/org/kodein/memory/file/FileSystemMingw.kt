package org.kodein.memory.file

import kotlinx.cinterop.*
import org.kodein.memory.io.IOException
import platform.posix.*
import platform.windows.*

@OptIn(ExperimentalUnsignedTypes::class)
public fun getLastErrorMessage(): String? {
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

public fun IOException.Companion.fromLastError(type: String): IOException = IOException(getLastErrorMessage() ?: "Unknown $type error")

@OptIn(ExperimentalUnsignedTypes::class)
public actual object FileSystem {
    internal actual val pathSeparator: String = "\\"

    public actual val tempDirectory: Path by lazy {
        memScoped {
            val lpstr = allocArray<WCHARVar>(MAX_PATH + 1)

            val length = GetTempPathW((MAX_PATH + 1).convert(), lpstr)

            if (length == 0u || length > MAX_PATH.toUInt()) {
                throw IOException.fromLastError("file system")
            }

            Path(lpstr.toKString())
        }
    }

    public actual fun workingDir(): Path {
        memScoped {
            val lpstr = allocArray<WCHARVar>(PATH_MAX + 1)
            val ret = GetCurrentDirectoryW((PATH_MAX + 1).toUInt(), lpstr)
            if (ret == 0u) throw IOException.fromLastError("file system")
            return Path(lpstr.toKString())
        }
    }
    public actual fun changeWorkingDir(path: Path) { SetCurrentDirectoryW(path.path) }

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

    public actual val roots: List<Path> by lazy { getRoots(129) }
}
