package org.kodein.memory.file

import kotlinx.cinterop.*
import org.kodein.memory.io.*
import platform.posix.*
import kotlin.math.min

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

public actual fun Path.getType(): EntityType = getType(::stat)
public actual fun Path.getLType(): EntityType = getType(::lstat)

@OptIn(ExperimentalUnsignedTypes::class)
public actual fun Path.createDir() {
    mkdir(path, "775".toUInt(8).convert())
}

public actual fun Path.listDir(): List<Path> {
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

public actual fun Path.delete() {
    if (remove(path) != 0) throw IOException.fromErrno("delete")
}

@OptIn(ExperimentalUnsignedTypes::class)
private class PosixReadableFile(private val file: CPointer<FILE>) : ReadableFile {

    private val size: Int

    private val alloc = nativeHeap.allocArray<ByteVar>(8)

    init {
        fseek(file, 0.convert(), SEEK_END)
        size = ftell(file).toInt()
        fseek(file, 0.convert(), SEEK_SET)
    }

    override var position: Int
        get() = ftell(file).toInt()
        set(value) {
            require(value <= size) { "Position $value is out of file (size: $size)" }
            fseek(file, value.convert(), SEEK_SET)
        }

    override val remaining: Int get() = size - ftell(file).toInt()

    override fun valid() = remaining != 0

    override fun requestCanRead(needed: Int) {
        if (needed > remaining) throw IOException("End of channel file: needed $needed bytes, but has only $remaining remaining bytes.")
    }

    override fun tryReadByte(): Int {
        val b = fgetc(file)
        if (b == EOF) return -1
        return b
    }

    override fun tryReadBytes(dst: ByteArray, dstOffset: Int, length: Int): Int {
        require(dstOffset >= 0)
        require(dstOffset + length <= dst.size)
        val r = fread(dst.refTo(dstOffset), 1.convert(), length.convert(), file).toInt()
        if (r == 0 && feof(file) != 0) return -1
        return r
    }

    override fun tryReadBytes(dst: Memory): Int {
        when (dst) {
            is ByteArrayMemory -> {
                val r = fread(dst.array.refTo(dst.offset), 1.convert(), dst.size.convert(), file).toInt()
                if (r == 0 && feof(file) != 0) return -1
                return r
            }
            is CPointerMemory -> {
                val r = fread(dst.pointer, 1.convert(), dst.size.convert(), file).toInt()
                if (r == 0 && feof(file) != 0) return -1
                return r
            }
            else -> {
                val buffer = ByteArray(dst.size)
                val r = tryReadBytes(buffer)
                if (r > 0) dst.putBytes(0, buffer, 0, r)
                return r
            }
        }
    }

    override fun readByte(): Byte {
        val b = fgetc(file)
        if (b == EOF) throw IOException.fromErrno("read")
        return b.toByte()
    }

    private inline fun <T> readValue(size: Int, getValue: CPointer<ByteVarOf<Byte>>.() -> T): T {
        val read = fread(alloc, size.convert(), 1.convert(), file).toInt()
        if (read != 1) throw IOException.fromErrno("read")
        return alloc.getValue()
    }

    override fun readShort(): Short = readValue(2) { reinterpret<ShortVar>().pointed.value.toBigEndian() }
    override fun readInt(): Int = readValue(4) { reinterpret<IntVar>().pointed.value.toBigEndian() }
    override fun readLong(): Long = readValue(8) { reinterpret<LongVar>().pointed.value.toBigEndian() }

    override fun readBytes(dst: ByteArray, dstOffset: Int, length: Int) {
        val r = fread(dst.refTo(0), 1.convert(), length.convert(), file).toInt()
        if (r == 0 && feof(file) != 0) throw IOException.fromErrno("read")
    }

    override fun skip(count: Int) {
        require(count >= 0)
        val skipped = skipAtMost(count)
        if (skipped != count) throw IOException("Could not skip $count bytes, only $skipped bytes.")
    }

    override fun skipAtMost(count: Int): Int {
        val old = ftell(file).toInt()
        fseek(file, count.convert(), SEEK_CUR)
        val new = ftell(file).toInt()
        return new - old
    }

    override fun close() {
        nativeHeap.free(alloc)
        fclose(file)
    }
}

public actual fun Path.openReadableFile(): ReadableFile {
    val file = fopen(path, "r") ?: throw IOException.fromErrno("open")
    return PosixReadableFile(file)
}

@OptIn(ExperimentalUnsignedTypes::class)
private class PosixWriteableFile(private val file: CPointer<FILE>) : WriteableFile {

    private val alloc = nativeHeap.allocArray<ByteVar>(8)

    override val remaining: Int get() = Int.MAX_VALUE

    override val position: Int get() = ftell(file).toInt()

    override fun requestCanWrite(needed: Int) {} // noop

    override fun writeByte(value: Byte) {
        val w = fputc(value.toInt(), file)
        if (w == EOF) throw IOException.fromErrno("write")
    }

    private inline fun <T> writeValue(size: Int, value: T, setValue: CPointer<ByteVarOf<Byte>>.(T) -> Unit) {
        alloc.setValue(value)
        val w = fwrite(alloc, size.convert(), 1.convert(), file).toInt()
        if (w != 1) throw IOException.fromErrno("write")
    }

    override fun writeShort(value: Short) = writeValue(2, value) { reinterpret<ShortVar>().pointed.value = it.toBigEndian() }
    override fun writeInt(value: Int) = writeValue(4, value) { reinterpret<IntVar>().pointed.value = it.toBigEndian() }
    override fun writeLong(value: Long) = writeValue(8, value) { reinterpret<LongVar>().pointed.value = it.toBigEndian() }

    override fun writeBytes(src: ByteArray, srcOffset: Int, length: Int) {
        val w = fwrite(src.refTo(srcOffset), 1.convert(), length.convert(), file).toInt()
        if (w != length) throw IOException.fromErrno("write")
    }

    override fun writeBytes(src: ReadMemory) {
        val w = when (src) {
            is ByteArrayMemory -> fwrite(src.array.refTo(src.offset), 1.convert(), src.size.convert(), file).toInt()
            is CPointerMemory -> fwrite(src.pointer, 1.convert(), src.size.convert(), file).toInt()
            else -> {
                val buffer = src.getBytes(0, src.size)
                fwrite(buffer.refTo(0), 1.convert(), src.size.convert(), file).toInt()
            }
        }
        if (w != src.size) throw IOException.fromErrno("write")
    }

    override fun writeBytes(src: Readable, length: Int) {
        if (src is MemoryReadable) {
            writeBytes(src.readMemory(length))
        } else {
            writeBytesBuffered(src, length)
        }
    }

    override fun skip(count: Int) {
        val old = ftell(file).toInt()
        fseek(file, count.convert(), SEEK_CUR)
        val new = ftell(file).toInt()
        val skipped = new - old
        if (skipped != count) throw IOException("Could not skip $count bytes, only $skipped bytes.")
    }

    override fun flush() {
        fflush(file)
    }

    override fun close() {
        nativeHeap.free(alloc)
        fclose(file)
    }
}

public actual fun Path.openWriteableFile(append: Boolean): WriteableFile {
    val mode = if (append) "a" else "w"
    val file = fopen(path, mode) ?: throw IOException.fromErrno("write")
    return PosixWriteableFile(file)
}
