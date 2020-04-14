package org.kodein.memory.file

import org.kodein.memory.Closeable
import org.kodein.memory.io.IOException
import org.kodein.memory.io.Readable
import org.kodein.memory.io.Writeable


interface ReadableFile : Readable, Closeable

expect fun Path.openReadableFile(): ReadableFile


interface WriteableFile : Writeable, Closeable

expect fun Path.openWriteableFile(append: Boolean = false): WriteableFile


sealed class EntityType {
    sealed class File : EntityType() {
        object Regular : File()
        object SymbolicLink : File()
        object Other: File()
    }
    object Directory : EntityType()

    sealed class Non(val what: String) : EntityType() {
        object Existent : Non("existent")
        object Accessible : Non("accessible")
        object Understandable : Non("understandable")
    }
}

expect fun Path.getType(): EntityType

expect fun Path.getLType(): EntityType

expect fun Path.listDir(): List<Path>

expect fun Path.createDir()

fun Path.createDirs() {
    if (getType() is EntityType.Non.Existent) {
        parent().createDirs()
        createDir()
    }
}

expect fun Path.delete()

private fun Path.deleteRecursive(check: Boolean) {
    when (val type = getLType()) {
        is EntityType.File -> delete()
        is EntityType.Directory -> {
            listDir().forEach { it.deleteRecursive(true) }
            delete()
        }
        is EntityType.Non -> {
            if (check) throw IOException("Stumbled on a non ${type.what} path: ${path}")
        }
    }
}

fun Path.deleteRecursive() = deleteRecursive(false)
