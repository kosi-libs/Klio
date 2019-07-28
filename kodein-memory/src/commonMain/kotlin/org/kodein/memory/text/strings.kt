package org.kodein.memory.text

import org.kodein.memory.io.*


fun Charset.sizeOf(str: CharSequence): Int =
        str.fold(0) { count, char -> count + sizeOf(char) }

fun Writeable.putString(str: CharSequence, charset: Charset = Charset.UTF8): Int =
        str.fold(0) { count, char -> count + charset.encode(char, this) }

fun Readable.readString(charset: Charset = Charset.UTF8, sizeBytes: Int = remaining, maxChars: Int = Int.MAX_VALUE): String {
    var readSize = 0
    var countChars = 0
    val builder = StringBuilder()
    while (readSize < sizeBytes && countChars < maxChars) {
        val char = charset.decode(this)
        builder.append(char)
        readSize += charset.sizeOf(char)
        countChars += 1
    }
    return builder.toString()
}

fun ReadBuffer.getString(index: Int, charset: Charset = Charset.UTF8, sizeBytes: Int = limit - index, maxChars: Int = Int.MAX_VALUE): String = slice(index).readString(charset, sizeBytes, maxChars)

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

