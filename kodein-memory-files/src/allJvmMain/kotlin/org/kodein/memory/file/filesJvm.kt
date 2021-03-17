package org.kodein.memory.file

import org.kodein.memory.io.*
import java.io.*
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.channels.FileChannel
import java.nio.file.Files
import java.nio.file.LinkOption
import java.nio.file.OpenOption
import java.nio.file.StandardOpenOption
import java.nio.file.attribute.BasicFileAttributes


public class JVMReadableFile(channel: FileChannel): ReadableFile,
    CursorReadable by channel.asReadable(),
    Closeable by channel

public actual fun Path.openReadableFile(): ReadableFile =
    JVMReadableFile(FileChannel.open(asNio(), StandardOpenOption.READ))

public class JVMWriteableFile(channel: FileChannel): WriteableFile,
        CursorWriteable by channel.asWriteable(),
        Closeable by channel

public actual fun Path.openWriteableFile(append: Boolean): WriteableFile =
        JVMWriteableFile(FileChannel.open(asNio(),
            StandardOpenOption.WRITE,
            StandardOpenOption.CREATE,
            if (append) StandardOpenOption.APPEND else StandardOpenOption.TRUNCATE_EXISTING)
        )



private interface FileTypeGetter {
    fun getType(of: Path): EntityType
    fun getLType(of: Path): EntityType

    class Nio : FileTypeGetter {
        fun getType(of: Path, lOpts: Array<out LinkOption>): EntityType {
            return try {
                val attrs = Files.readAttributes(java.nio.file.Paths.get(of.path), BasicFileAttributes::class.java, *lOpts)
                when {
                    attrs.isRegularFile -> EntityType.File.Regular
                    attrs.isDirectory -> EntityType.Directory
                    attrs.isSymbolicLink -> EntityType.File.SymbolicLink
                    attrs.isOther -> EntityType.File.Other
                    else -> EntityType.Non.Understandable
                }
            } catch (ex: IOException) {
                when (ex) {
                    is java.nio.file.NoSuchFileException -> EntityType.Non.Existent
                    is java.nio.file.AccessDeniedException -> EntityType.Non.Accessible
                    else -> EntityType.Non.Understandable
                }
            }
        }

        override fun getType(of: Path): EntityType = getType(of, emptyArray())
        override fun getLType(of: Path): EntityType = getType(of, arrayOf(LinkOption.NOFOLLOW_LINKS))
    }

    class Legacy : FileTypeGetter {
        override fun getType(of: Path): EntityType {
            val f = File(of.path)
            return when {
                f.exists() -> EntityType.Non.Existent
                f.isDirectory -> EntityType.Directory
                f.isFile -> EntityType.File.Regular
                else -> EntityType.Non.Understandable
            }
        }
        override fun getLType(of: Path): EntityType = getType(of)
    }

    companion object {
        val instance by lazy {
            try {
                Class.forName("java.nio.file.Files")
                Nio()
            } catch (_: ClassNotFoundException) {
                Legacy()
            }
        }
    }
}

public actual fun Path.getType(): EntityType = FileTypeGetter.instance.getType(this)

public actual fun Path.getLType(): EntityType = FileTypeGetter.instance.getLType(this)

public actual fun Path.listDir(): List<Path> = File(path).list()?.map { resolve(it) } ?: throw FileNotFoundException(path)

public actual fun Path.createDir() {
    File(path).mkdir()
}

public actual fun Path.delete() {
    if (!File(path).delete()) throw IOException("Error deleting $path")
}


