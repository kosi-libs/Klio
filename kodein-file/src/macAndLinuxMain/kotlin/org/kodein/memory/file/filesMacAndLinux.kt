package org.kodein.memory.file

import platform.posix.S_IFDIR
import platform.posix.S_IFLNK
import platform.posix.S_IFREG
import platform.posix.mkdir
import platform.posix.stat
import platform.posix.lstat

@OptIn(ExperimentalUnsignedTypes::class)
private fun Path.getType(statFun: (String?, CValuesRef<stat>?) -> Int): EntityType {
    memScoped {
        val stat = alloc<stat>()
        val status = statFun(path, stat.ptr)
        if (status == -1) {
            return when (errno) {
                ENOENT, ENOTDIR -> EntityType.Non.Existent
                EACCES -> EntityType.Non.Accessible
                ELOOP, ENAMETOOLONG -> EntityType.Non.Understandable
                else -> throw IOException.fromErrno("path")
            }
        }

        return when (stat.st_mode.toInt() and S_IFMT) {
            S_IFREG -> EntityType.File.Regular
            S_IFDIR -> EntityType.Directory
            S_IFLNK -> EntityType.File.SymbolicLink
            else -> EntityType.File.Other
        }
    }
}

actual fun Path.getType(): EntityType = getType(::stat)
actual fun Path.getLType(): EntityType = getType(::lstat)

@OptIn(ExperimentalUnsignedTypes::class)
actual fun Path.createDir() {
    mkdir(path, "775".toUInt(8))
}

actual fun Path.listDir(): List<Path> {
    val dir = opendir(path)
    try {
        return sequence {
            while (true) {
                val next = readdir(dir)?.pointed?.d_name?.toKString() ?: break
                if (next != "." && next != "..")
                    yield(resolve(next))
            }
        } .toList()
    } finally {
        closedir(dir)
    }
}

actual fun Path.delete() {
    if (remove(path) != 0) throw IOException.fromErrno("delete")
}
