//package org.kodein.memory.file
//
//import kotlinx.cinterop.*
//import org.kodein.memory.io.IOException
//import org.kodein.memory.io.Readable
//import org.kodein.memory.io.toBigEndian
//import platform.posix.*
//
//
//@OptIn(ExperimentalUnsignedTypes::class)
//private class NativeReadableFile(private val file: CPointer<FILE>) : ReadableFile {
//
//    private val size: Int
//
//    private val alloc = nativeHeap.allocArray<ByteVar>(8)
//
//    init {
//        fseek(file, 0.convert(), SEEK_END)
//        size = ftell(file).toInt()
//        fseek(file, 0.convert(), SEEK_SET)
//    }
//
//    override val available: Int get() = size - ftell(file).toInt()
//
//    override fun valid() = available != 0
//
//    override fun receive(): Int {
//        val b = fgetc(file)
//        if (b == EOF) return -1
//        return b
//    }
//
//    override fun receive(dst: ByteArray, dstOffset: Int, length: Int): Int {
//        val r = fread(dst.refTo(0), 1.convert(), length.convert(), file).toInt()
//        if (r == 0 && feof(file) != 0) return -1
//        return r
//    }
//
//    override fun readByte(): Byte {
//        val b = fgetc(file)
//        if (b == EOF) throw IOException.fromErrno("read")
//        return b.toByte()
//    }
//
//    override fun readChar(): Char = readShort().toChar()
//
//    override fun readShort(): Short {
//        val read = fread(alloc, Short.SIZE_BYTES.convert(), 1.convert(), file).toInt()
//        if (read != 1) throw IOException.fromErrno("read")
//        return alloc.reinterpret<ShortVar>().pointed.value.toBigEndian()
//    }
//
//    override fun readInt(): Int {
//        val read = fread(alloc, Int.SIZE_BYTES.convert(), 1.convert(), file).toInt()
//        if (read != 1) throw IOException.fromErrno("read")
//        return alloc.reinterpret<IntVar>().pointed.value.toBigEndian()
//    }
//
//    override fun readLong(): Long {
//        val read = fread(alloc, Long.SIZE_BYTES.convert(), 1.convert(), file).toInt()
//        if (read != 1) throw IOException.fromErrno("read")
//        return alloc.reinterpret<LongVar>().pointed.value.toBigEndian()
//    }
//
//    override fun readFloat(): Float = Float.fromBits(readInt())
//
//    override fun readDouble(): Double = Double.fromBits(readLong())
//
//    override fun readBytes(dst: ByteArray, dstOffset: Int, length: Int) {
//        val r = fread(dst.refTo(0), 1.convert(), length.convert(), file).toInt()
//        if (r == 0 && feof(file) != 0) throw IOException.fromErrno("read")
//    }
//
//    override fun skip(count: Int): Int {
//        val old = ftell(file).toInt()
//        fseek(file, count.convert(), SEEK_CUR)
//        val new = ftell(file).toInt()
//        return new - old
//    }
//
//    override fun internalBuffer(): Readable = this
//
//    override fun close() {
//        nativeHeap.free(alloc)
//        fclose(file)
//    }
//
//}
//
//actual fun Path.openReadableFile(): ReadableFile {
//    val file = fopen(path, "r") ?: throw IOException.fromErrno("open")
//    return NativeReadableFile(file)
//}
//
//@OptIn(ExperimentalUnsignedTypes::class)
//private class NativeWriteableFile(private val file: CPointer<FILE>) : WriteableFile {
//
//    override val available: Int = Int.MAX_VALUE
//
//    private val alloc = nativeHeap.allocArray<ByteVar>(8)
//
//    override fun putByte(value: Byte) {
//        val w = fputc(value.toInt(), file)
//        if (w == EOF) throw IOException.fromErrno("write")
//    }
//
//    override fun putChar(value: Char) = putShort(value.toShort())
//
//    override fun putShort(value: Short) {
//        alloc.reinterpret<ShortVar>().pointed.value = value.toBigEndian()
//        val w = fwrite(alloc, Short.SIZE_BYTES.convert(), 1.convert(), file).toInt()
//        if (w != 1) throw IOException.fromErrno("write")
//    }
//
//    override fun putInt(value: Int) {
//        alloc.reinterpret<IntVar>().pointed.value = value.toBigEndian()
//        val w = fwrite(alloc, Int.SIZE_BYTES.convert(), 1.convert(), file).toInt()
//        if (w != 1) throw IOException.fromErrno("write")
//    }
//
//    override fun putLong(value: Long) {
//        alloc.reinterpret<LongVar>().pointed.value = value.toBigEndian()
//        val w = fwrite(alloc, Long.SIZE_BYTES.convert(), 1.convert(), file).toInt()
//        if (w != 1) throw IOException.fromErrno("write")
//    }
//
//    override fun putFloat(value: Float) = putInt(value.toBits())
//
//    override fun putDouble(value: Double) = putLong(value.toBits())
//
//    override fun putBytes(src: ByteArray, srcOffset: Int, length: Int) {
//        val w = fwrite(src.refTo(srcOffset), 1.convert(), length.convert(), file).toInt()
//        if (w != length) throw IOException.fromErrno("write")
//    }
//
//    override fun putBytes(src: Readable, length: Int) {
//        repeat(length) { putByte(src.readByte()) }
//    }
//
//    override fun flush() {
//        fflush(file)
//    }
//
//    override fun close() {
//        nativeHeap.free(alloc)
//        fclose(file)
//    }
//}
//
//actual fun Path.openWriteableFile(append: Boolean): WriteableFile {
//    val mode = if (append) "a" else "w"
//    val file = fopen(path, mode) ?: throw IOException.fromErrno("write")
//    return NativeWriteableFile(file)
//}
//
