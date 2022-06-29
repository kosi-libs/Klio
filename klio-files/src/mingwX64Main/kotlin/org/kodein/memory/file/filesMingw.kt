package org.kodein.memory.file

import kotlinx.cinterop.*
import org.kodein.memory.io.*
import org.kodein.memory.*
import platform.windows.*


@OptIn(ExperimentalUnsignedTypes::class)
public actual fun Path.getType(): EntityType {

    if (PathFileExistsW(path) == 0) return EntityType.Non.Existent

    val attrs = GetFileAttributesW(path)
    if (attrs == INVALID_FILE_ATTRIBUTES) return EntityType.Non.Accessible

    fun flag(flag: Int): Boolean = (attrs.toInt() and flag) == flag

    return when {
        flag(FILE_ATTRIBUTE_DIRECTORY) -> EntityType.Directory
        flag(FILE_ATTRIBUTE_DEVICE) -> EntityType.File.Other
        else -> EntityType.File.Regular

    }
}

public actual fun Path.getLType(): EntityType = getType()

public actual fun Path.createDir() {
    CreateDirectoryW(path, null)
}

public actual fun Path.listDir(): List<Path> {
    memScoped {
        val ffd = alloc<WIN32_FIND_DATAW>()
        val handle = FindFirstFileW(if (path.endsWith('\\')) "$path*" else "$path\\*", ffd.ptr)
        if (handle == INVALID_HANDLE_VALUE) throw IOException.fromLastError("directory")

        try {
            return sequence {
                do {
                    val fileName = ffd.cFileName.toKString()
                    if (fileName != "." && fileName != "..") yield(resolve(fileName))
                } while (FindNextFileW(handle, ffd.ptr) != 0)
            }.toList()
        } finally {
            FindClose(handle)
        }
    }
}

public actual fun Path.delete() {
    val ret = when (getType()) {
        is EntityType.Directory -> RemoveDirectoryW(path)
        is EntityType.File -> DeleteFileW(path)
        else -> 1
    }

    if (ret == 0) throw IOException.fromLastError("file")
}

@OptIn(ExperimentalUnsignedTypes::class)
private class MingwReadableFile(private val handle: HANDLE?) : ReadableFile {

    override val size: Int = GetFileSize(handle, null).toInt()

    private val buffer = nativeHeap.allocArray<ByteVar>(8)
    private val nread = nativeHeap.alloc<DWORDVar>()

    override val remaining: Int get() = size - position

    override var position: Int
            get() = SetFilePointer(handle, 0, null, FILE_CURRENT).toInt()
            set(value) { SetFilePointer(handle, value, null, FILE_BEGIN) }

    override fun requestCanRead(needed: Int) {
        if (needed > remaining) throw IOException("End of file: needed $needed bytes, but has only $remaining remaining bytes.")
    }

    override fun valid() = remaining != 0

    override fun tryReadByte(): Int {
        val ret = ReadFile(handle, buffer, 1u, nread.ptr, null)
        if (ret == 0) throw IOException.fromLastError("read")
        if (nread.value == 0u) return -1
        return buffer[0].toInt()
    }

    override fun tryReadBytes(dst: ByteArray, dstOffset: Int, length: Int): Int {
        require(dstOffset >= 0)
        require(dstOffset + length <= dst.size)
        memScoped {
            val ret = ReadFile(handle, dst.refTo(dstOffset).getPointer(this), length.toUInt(), nread.ptr, null)
            if (ret == 0) throw IOException.fromLastError("read")
            if (nread.value == 0u) return -1
            return nread.value.toInt()
        }
    }

    override fun tryReadBytes(dst: Memory): Int {
        when (dst) {
            is ByteArrayMemory -> memScoped {
                val ret = ReadFile(handle, dst.array.refTo(dst.offset).getPointer(this), dst.size.toUInt(), nread.ptr, null)
                if (ret == 0) throw IOException.fromLastError("read")
                if (nread.value == 0u) return -1
                return nread.value.toInt()
            }
            is CPointerMemory -> {
                val ret = ReadFile(handle, dst.pointer, dst.size.toUInt(), nread.ptr, null)
                if (ret == 0) throw IOException.fromLastError("read")
                if (nread.value == 0u) return -1
                return nread.value.toInt()
            }
            else -> {
                val buffer = ByteArray(dst.size)
                val r = tryReadBytes(buffer)
                if (r > 0) dst.putBytes(0, buffer, 0, r)
                return r
            }
        }
    }

    private inline fun <T> readValue(size: Int, getValue: CPointer<ByteVarOf<Byte>>.() -> T): T {
        val ret = ReadFile(handle, buffer, size.toUInt(), nread.ptr, null)
        if (ret == 0) throw IOException.fromLastError("read")
        if (nread.value.toInt() != size) throw IOException("Could not read $size bytes (only ${nread.value} bytes)")
        return buffer.getValue()
    }

    override fun readByte(): Byte = readValue(1) { pointed.value }
    override fun readShort(): Short = readValue(2) { reinterpret<ShortVar>().pointed.value.toBigEndian() }
    override fun readInt(): Int = readValue(4) { reinterpret<IntVar>().pointed.value.toBigEndian() }
    override fun readLong(): Long = readValue(8) { reinterpret<LongVar>().pointed.value.toBigEndian() }

    override fun readBytes(dst: ByteArray, dstOffset: Int, length: Int) {
        require(dstOffset >= 0)
        require(dstOffset + length <= dst.size)
        memScoped {
            val ret = ReadFile(handle, dst.refTo(dstOffset).getPointer(this), length.toUInt(), nread.ptr, null)
            if (ret == 0) throw IOException.fromLastError("read")
            if (nread.value != length.toUInt()) throw IOException("Could not read $length bytes (only ${nread.value})")
        }
    }

    override fun skip(count: Int) {
        require(count >= 0)
        val skipped = skipAtMost(count)
        if (skipped != count) throw IOException("Could not skip $count bytes, only $skipped bytes.")
    }

    override fun skipAtMost(count: Int): Int {
        val old = SetFilePointer(handle, 0, null, FILE_CURRENT)
        val new = SetFilePointer(handle, count, null, FILE_CURRENT)
        if (new == INVALID_SET_FILE_POINTER) throw IOException.fromLastError("skip")
        return (new - old).toInt()
    }

    override fun close() {
        nativeHeap.free(buffer)
        nativeHeap.free(nread)
        CloseHandle(handle)
    }

}

public actual fun Path.openReadableFile(): ReadableFile {
    val handle = CreateFileW(
            path,
            GENERIC_READ,
            FILE_SHARE_READ,
            null,
            OPEN_EXISTING,
            FILE_ATTRIBUTE_NORMAL,
            null
    )
    if (handle == INVALID_HANDLE_VALUE) throw IOException.fromLastError("read")
    return MingwReadableFile(handle)
}

@OptIn(ExperimentalUnsignedTypes::class)
private class WinWriteableFile(private val handle: HANDLE?) : WriteableFile {

    override val position: Int get() = SetFilePointer(handle, 0, null, FILE_CURRENT).toInt()

    override val remaining: Int get() = Int.MAX_VALUE

    override fun requestCanWrite(needed: Int) {} // noop

    private val buffer = nativeHeap.allocArray<ByteVar>(8)
    private val nwritten = nativeHeap.alloc<DWORDVar>()

    private inline fun <T> writeValue(size: Int, value: T, setValue: CPointer<ByteVarOf<Byte>>.(T) -> Unit) {
        buffer.setValue(value)
        val ret = WriteFile(handle, buffer, size.toUInt(), nwritten.ptr, null)
        if (ret == 0) throw IOException.fromLastError("write")
        if (nwritten.value.toInt() != size) throw IOException("Could not write $size bytes (only ${nwritten.value} bytes)")
    }

    override fun writeByte(value: Byte) = writeValue(1, value) { pointed.value = it }
    override fun writeShort(value: Short) = writeValue(2, value) { reinterpret<ShortVar>().pointed.value = it.toBigEndian() }
    override fun writeInt(value: Int) = writeValue(4, value) { reinterpret<IntVar>().pointed.value = it.toBigEndian() }
    override fun writeLong(value: Long) = writeValue(8, value) { reinterpret<LongVar>().pointed.value = it.toBigEndian() }

    override fun writeBytes(src: ByteArray, srcOffset: Int, length: Int) {
        require(srcOffset >= 0)
        require(srcOffset + length <= src.size)
        memScoped {
            val ret = WriteFile(handle, src.refTo(srcOffset).getPointer(this), length.toUInt(), nwritten.ptr, null)
            if (ret == 0) throw IOException.fromLastError("write")
            if (nwritten.value != length.toUInt()) throw IOException("Could not write $length bytes (only ${nwritten.value})")
        }
    }

    override fun writeBytes(src: Readable, length: Int): Unit = writeBytesBuffered(src, length)

    override fun writeBytes(src: ReadMemory) {
        val ret = when (src) {
            is ByteArrayMemory -> memScoped {
                WriteFile(handle, src.array.refTo(src.offset).getPointer(this), src.size.toUInt(), nwritten.ptr, null)
            }
            is CPointerMemory -> {
                WriteFile(handle, src.pointer, src.size.toUInt(), nwritten.ptr, null)
            }
            else -> {
                Allocation.nativeCopy(src).use {
                    WriteFile(handle, it.memory.pointer, it.size.toUInt(), nwritten.ptr, null)
                }
            }
        }
        if (ret == 0) throw IOException.fromLastError("write")
        if (nwritten.value != src.size.toUInt()) throw IOException("Could not write ${src.size} bytes (only ${nwritten.value})")
    }

    override fun skip(count: Int) {
        val old = SetFilePointer(handle, 0, null, FILE_CURRENT).toInt()
        SetFilePointer(handle, count, null, FILE_CURRENT).toInt()
        val new = SetFilePointer(handle, 0, null, FILE_CURRENT).toInt()
        val skipped = new - old
        if (skipped != count) throw IOException("Could not skip $count bytes, only $skipped bytes.")
    }

    override fun flush() {
        val ret = FlushFileBuffers(handle)
        if (ret == 0) throw IOException.fromLastError("write")
    }

    override fun close() {
        nativeHeap.free(buffer)
        nativeHeap.free(nwritten)
        CloseHandle(handle)
    }
}

@OptIn(ExperimentalUnsignedTypes::class)
public actual fun Path.openWriteableFile(append: Boolean): WriteableFile {
    val handle = CreateFileW(
            path,
            GENERIC_WRITE,
            0u,
            null,
            (if (append) OPEN_ALWAYS else CREATE_ALWAYS).toUInt(),
            FILE_ATTRIBUTE_NORMAL,
            null
    )
    if (append) SetFilePointer(handle, 0, null, FILE_END)
    if (handle == INVALID_HANDLE_VALUE) throw IOException.fromLastError("write")
    return WinWriteableFile(handle)
}
