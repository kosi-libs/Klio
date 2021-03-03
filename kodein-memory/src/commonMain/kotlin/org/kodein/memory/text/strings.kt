package org.kodein.memory.text

import org.kodein.memory.io.*
import kotlin.math.min


public fun Charset.sizeOf(str: CharSequence): Int =
    str.fold(0) { count, char -> count + sizeOf(char) }

public fun Writeable.putString(str: CharSequence, charset: Charset = Charset.UTF8): Int {
    requireCanWrite(charset.sizeOf(str))
    return str.fold(0) { count, char -> count + charset.encode(char, this) }
}

public fun Readable.readString(charset: Charset = Charset.UTF8, sizeBytes: Int, maxChars: Int = Int.MAX_VALUE): String {
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

public fun ReadBuffer.readString(charset: Charset = Charset.UTF8, maxChars: Int = Int.MAX_VALUE): String = readString(charset, remaining, maxChars)


public fun Writeable.putSizeThenString(str: CharSequence, charset: Charset = Charset.UTF8): Int {
    putInt(charset.sizeOf(str))
    return putString(str, charset) + 4
}

public fun Readable.readSizeThenString(charset: Charset = Charset.UTF8): String {
    val size = readInt()
    requireCanRead(size)
    return readString(charset, sizeBytes = size)
}

public fun Writeable.putStringThenNull(str: CharSequence, charset: Charset = Charset.UTF8): Int {
    val size = putString(str, charset)
    putByte(0)
    return size + 1
}

public fun Readable.readStringThenNull(charset: Charset = Charset.UTF8): String {
    val sb = StringBuilder()
    while (true) {
        val char = charset.decode(this)
        if (char != 0.toChar())
            break
        sb.append(char)
    }
    return sb.toString()
}

public fun KBuffer.Companion.wrap(str: CharSequence, charset: Charset = Charset.UTF8): KBuffer =
        KBuffer.array(charset.sizeOf(str)) { putString(str, charset) }
