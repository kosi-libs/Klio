package org.kodein.memory.text

import org.kodein.memory.io.*
import kotlin.math.min


fun Charset.sizeOf(str: CharSequence): Int =
        str.fold(0) { count, char -> count + sizeOf(char) }

fun Writeable.putString(str: CharSequence, charset: Charset = Charset.UTF8): Int =
        str.fold(0) { count, char -> count + charset.encode(char, this) }

fun Readable.readString(charset: Charset = Charset.UTF8, sizeBytes: Int = remaining, maxChars: Int = Int.MAX_VALUE): String {
    var readSize = 0
    val array = CharArray(min(sizeBytes, maxChars))
    var pos = 0

    while (readSize < sizeBytes && pos < maxChars) {
        val char = charset.decode(this)
        array[pos++] = char
        readSize += charset.sizeOf(char)
    }
    return String(array, 0, pos)
}

fun ReadMemory.getString(index: Int, charset: Charset = Charset.UTF8, sizeBytes: Int = limit - index, maxChars: Int = Int.MAX_VALUE): String = slice(index).readString(charset, sizeBytes, maxChars)

fun Writeable.putSizeAndString(str: CharSequence, charset: Charset = Charset.UTF8): Int {
    putInt(charset.sizeOf(str))
    return putString(str, charset)
}

fun Readable.readSizeAndString(charset: Charset = Charset.UTF8): String {
    val size = readInt()
    return readString(charset, sizeBytes = size)
}

fun KBuffer.Companion.wrap(str: CharSequence, charset: Charset = Charset.UTF8): KBuffer =
        KBuffer.array(charset.sizeOf(str)) { putString(str, charset) }

fun String.toAsciiBytes() = Charset.ASCII.stringToBytes(this)
fun ByteArray.toAsciiString() = Charset.ASCII.bytesToString(this)
