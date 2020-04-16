package org.kodein.memory.file

import kotlinx.cinterop.*
import org.kodein.memory.io.IOException
import org.kodein.memory.io.Readable
import org.kodein.memory.io.putBytesBuffered
import org.kodein.memory.io.toBigEndian
import platform.windows.*


@OptIn(ExperimentalUnsignedTypes::class)
actual fun Path.getType(): EntityType {

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

actual fun Path.getLType() = getType()

actual fun Path.createDir() {
    CreateDirectoryW(path, null)
}

actual fun Path.listDir(): List<Path> {
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

actual fun Path.delete() {
    val ret = when (getType()) {
        is EntityType.Directory -> RemoveDirectoryW(path)
        is EntityType.File -> DeleteFileW(path)
        else -> 1
    }

    if (ret == 0) throw IOException.fromLastError("file")
}

@OptIn(ExperimentalUnsignedTypes::class)
private class WinReadableFile(private val handle: HANDLE?) : ReadableFile {

    private val size: Int = GetFileSize(handle, null).toInt()

    private val buffer = nativeHeap.allocArray<UByteVar>(8)
    private val nread = nativeHeap.alloc<DWORDVar>()

    override val available: Int get() = size - SetFilePointer(handle, 0, null, FILE_CURRENT).toInt()

    override fun valid() = available != 0

    override fun receive(): Int {
        val ret = ReadFile(
                handle,
                buffer,
                1u,
                nread.ptr,
                null
        )
        if (ret == 0) throw IOException.fromLastError("read")
        if (nread.value == 0u) return -1
        return buffer[0].toInt()
    }

    override fun receive(dst: ByteArray, dstOffset: Int, length: Int): Int {
        require(dstOffset >= 0)
        require(dstOffset + length <= dst.size)
        memScoped {
            val ret = ReadFile(
                    handle,
                    dst.refTo(dstOffset).getPointer(this),
                    length.toUInt(),
                    nread.ptr,
                    null
            )
            if (ret == 0) throw IOException.fromLastError("read")
            if (nread.value == 0u) return -1
            return nread.value.toInt()
        }
    }

    private fun readX(size: UInt, name: String) {
        val ret = ReadFile(
                handle,
                buffer,
                size,
                nread.ptr,
                null
        )
        if (ret == 0) throw IOException.fromLastError("read")
        if (nread.value != size) throw IOException("Could not read $size bytes for a $name (only ${nread.value} bytes)")
    }

    override fun readByte(): Byte {
        readX(Byte.SIZE_BYTES.toUInt(), "Byte")
        return buffer[0].toByte()
    }

    override fun readChar(): Char = readShort().toChar()

    override fun readShort(): Short {
        readX(Short.SIZE_BYTES.toUInt(), "Short")
        return buffer.reinterpret<ShortVar>().pointed.value.toBigEndian()
    }

    override fun readInt(): Int {
        readX(Int.SIZE_BYTES.toUInt(), "Int")
        return buffer.reinterpret<IntVar>().pointed.value.toBigEndian()
    }

    override fun readLong(): Long {
        readX(Long.SIZE_BYTES.toUInt(), "Long")
        return buffer.reinterpret<LongVar>().pointed.value.toBigEndian()
    }

    override fun readFloat(): Float = Float.fromBits(readInt())

    override fun readDouble(): Double = Double.fromBits(readLong())

    override fun readBytes(dst: ByteArray, dstOffset: Int, length: Int) {
        require(dstOffset >= 0)
        require(dstOffset + length <= dst.size)
        memScoped {
            val ret = ReadFile(
                    handle,
                    dst.refTo(dstOffset).getPointer(this),
                    length.toUInt(),
                    nread.ptr,
                    null
            )
            if (ret == 0) throw IOException.fromLastError("read")
            if (nread.value != length.toUInt()) throw IOException("Could not read $length bytes (only ${nread.value})")
        }
    }

    override fun skip(count: Int): Int {
        val old = SetFilePointer(handle, 0, null, FILE_CURRENT)
        val new = SetFilePointer(handle, count, null, FILE_CURRENT)
        if (new == INVALID_SET_FILE_POINTER) throw IOException.fromLastError("skip")
        return (new - old).toInt()
    }

    override fun internalBuffer(): Readable = this

    override fun close() {
        nativeHeap.free(buffer)
        nativeHeap.free(nread)
        CloseHandle(handle)
    }

}

actual fun Path.openReadableFile(): ReadableFile {
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
    return WinReadableFile(handle)
}

@OptIn(ExperimentalUnsignedTypes::class)
private class WinWriteableFile(private val handle: HANDLE?) : WriteableFile {

    override val available: Int = Int.MAX_VALUE

    private val buffer = nativeHeap.allocArray<UByteVar>(8)
    private val nwritten = nativeHeap.alloc<DWORDVar>()

    private fun writeX(size: UInt, name: String) {
        val ret = WriteFile(
                handle,
                buffer,
                size,
                nwritten.ptr,
                null
        )
        if (ret == 0) throw IOException.fromLastError("write")
        if (nwritten.value != size) throw IOException("Could not write $size bytes for a $name (only ${nwritten.value} bytes)")
    }

    override fun putByte(value: Byte) {
        buffer[0] = value.toUByte()
        writeX(Byte.SIZE_BYTES.toUInt(), "Byte")
    }

    override fun putChar(value: Char) = putShort(value.toShort())

    override fun putShort(value: Short) {
        buffer.reinterpret<ShortVar>().pointed.value = value.toBigEndian()
        writeX(Short.SIZE_BYTES.toUInt(), "Short")
    }

    override fun putInt(value: Int) {
        buffer.reinterpret<IntVar>().pointed.value = value.toBigEndian()
        writeX(Int.SIZE_BYTES.toUInt(), "Int")
    }

    override fun putLong(value: Long) {
        buffer.reinterpret<LongVar>().pointed.value = value.toBigEndian()
        writeX(Long.SIZE_BYTES.toUInt(), "Long")
    }

    override fun putFloat(value: Float) = putInt(value.toBits())

    override fun putDouble(value: Double) = putLong(value.toBits())

    override fun putBytes(src: ByteArray, srcOffset: Int, length: Int) {
        require(srcOffset >= 0)
        require(srcOffset + length <= src.size)
        memScoped {
            val ret = WriteFile(
                    handle,
                    src.refTo(srcOffset).getPointer(this),
                    length.toUInt(),
                    nwritten.ptr,
                    null
            )
            if (ret == 0) throw IOException.fromLastError("write")
            if (nwritten.value != length.toUInt()) throw IOException("Could not write $length bytes (only ${nwritten.value})")
        }
    }

    override fun putBytes(src: Readable, length: Int) = putBytesBuffered(src, length)

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
actual fun Path.openWriteableFile(append: Boolean): WriteableFile {
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
