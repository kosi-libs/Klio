package org.kodein.memory.file

import org.kodein.memory.io.Readable
import org.kodein.memory.io.Writeable
import org.kodein.memory.io.asReadable
import org.kodein.memory.io.asWriteable
import java.io.*
import java.nio.file.Files
import java.nio.file.LinkOption
import java.nio.file.attribute.BasicFileAttributes

class JVMReadableFile(inputStream: FileInputStream): ReadableFile,
        Readable by inputStream.asReadable(),
        Closeable by inputStream

actual fun Path.openReadableFile(): ReadableFile =
        JVMReadableFile(FileInputStream(File(path)))

class JVMWriteableFile(outputStream: FileOutputStream): WriteableFile,
        Writeable by outputStream.asWriteable(),
        Closeable by outputStream

actual fun Path.openWriteableFile(append: Boolean): WriteableFile =
        JVMWriteableFile(FileOutputStream(File(path), append))



private interface FileTypeGetter {
    fun getType(of: Path): EntityType
    fun getLType(of: Path): EntityType

    class Nio : FileTypeGetter {
        fun getType(of: Path, lOpts: Array<out LinkOption>): EntityType {
            return try {
                val attrs = Files.readAttributes(java.nio.file.Path.of(of.path), BasicFileAttributes::class.java, *lOpts)
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

actual fun Path.getType() = FileTypeGetter.instance.getType(this)

actual fun Path.getLType() = FileTypeGetter.instance.getLType(this)

actual fun Path.listDir(): List<Path> = File(path).list()?.map { resolve(it) } ?: throw FileNotFoundException(path)

actual fun Path.createDir() {
    File(path).mkdir()
}

actual fun Path.delete() {
    if (!File(path).delete()) throw IOException("Error deleting $path")
}
