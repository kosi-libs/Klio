package org.kodein.memory.text

import org.kodein.memory.io.*
import kotlin.math.min


public fun Charset.sizeOf(str: CharSequence): Int =
        str.fold(0) { count, char -> count + sizeOf(char) }

public fun Writeable.putString(str: CharSequence, charset: Charset = Charset.UTF8): Int =
        str.fold(0) { count, char -> count + charset.encode(char, this) }

public fun Readable.readString(charset: Charset = Charset.UTF8, sizeBytes: Int = available, maxChars: Int = Int.MAX_VALUE): String {
    var readSize = 0
    val array = CharArray(min(sizeBytes, maxChars))
    var pos = 0

    while (readSize < sizeBytes && pos < maxChars) {
        val char = charset.decode(this)
        array[pos++] = char
        readSize += charset.sizeOf(char)
    }
    return array.concatToString(0, 0 + pos)
}

public fun ReadMemory.getString(index: Int, charset: Charset = Charset.UTF8, sizeBytes: Int = limit - index, maxChars: Int = Int.MAX_VALUE): String = slice(index).readString(charset, sizeBytes, maxChars)

public fun Writeable.putSizeAndString(str: CharSequence, charset: Charset = Charset.UTF8): Int {
    putInt(charset.sizeOf(str))
    return putString(str, charset)
}

public fun Readable.readSizeAndString(charset: Charset = Charset.UTF8): String {
    val size = readInt()
    return readString(charset, sizeBytes = size)
}

public fun KBuffer.Companion.wrap(str: CharSequence, charset: Charset = Charset.UTF8): KBuffer =
        KBuffer.array(charset.sizeOf(str)) { putString(str, charset) }

public fun String.toAsciiBytes(): ByteArray = Charset.ASCII.stringToBytes(this)
public fun ByteArray.toAsciiString(): String = Charset.ASCII.bytesToString(this)
