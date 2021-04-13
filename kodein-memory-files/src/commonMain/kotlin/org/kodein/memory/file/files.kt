package org.kodein.memory.file

import org.kodein.memory.Closeable
import org.kodein.memory.io.*


public interface ReadableFile : SeekableCursorReadable, Closeable

public expect fun Path.openReadableFile(): ReadableFile


public interface WriteableFile : CursorWriteable, Closeable

public expect fun Path.openWriteableFile(append: Boolean = false): WriteableFile


public sealed class EntityType {
    public sealed class File : EntityType() {
        public object Regular : File()
        public object SymbolicLink : File()
        public object Other: File()
    }
    public object Directory : EntityType()

    public sealed class Non(public val what: String) : EntityType() {
        public object Existent : Non("existent")
        public object Accessible : Non("accessible")
        public object Understandable : Non("understandable")
    }
}

public expect fun Path.getType(): EntityType

public expect fun Path.getLType(): EntityType

public expect fun Path.listDir(): List<Path>

public expect fun Path.createDir()

public fun Path.createDirs() {
    if (getType() is EntityType.Non.Existent) {
        parent().createDirs()
        createDir()
    }
}

public expect fun Path.delete()

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

public fun Path.deleteRecursive(): Unit = deleteRecursive(false)
