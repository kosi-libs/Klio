package org.kodein.memory.text

import org.kodein.memory.io.*
import kotlin.math.min


public fun Charset.sizeOf(str: CharSequence): Int =
    str.fold(headerSize()) { count, char -> count + sizeOf(char) }

public fun Writeable.writeChar(char: Char, charset: Charset = Charset.UTF8): Int =
    charset.encode(char, this)

public fun Writeable.writeString(str: CharSequence, charset: Charset = Charset.UTF8): Int {
    requestCanWrite(charset.sizeOf(str))
    return str.fold(0) { count, char -> count + charset.encode(char, this) }
}

public fun Readable.readChar(charset: Charset = Charset.UTF8): Char =
    charset.decode(this)

public fun Readable.readString(sizeBytes: Int, charset: Charset = Charset.UTF8, maxChars: Int = Int.MAX_VALUE): String {
    var readSize = 0
    val array = CharArray(min(sizeBytes, maxChars))
    var pos = 0

    while (readSize < sizeBytes && pos < maxChars) {
        val start = position
        val char = charset.decode(this)
        array[pos++] = char
        readSize += position - start
    }
    return array.concatToString(0, 0 + pos)
}

public fun CursorReadable.readString(charset: Charset = Charset.UTF8, maxChars: Int = Int.MAX_VALUE): String =
    readString(remaining, charset, maxChars)


public fun Writeable.writeStringThenNull(str: CharSequence, charset: Charset = Charset.UTF8): Int {
    require(str.none { it.toInt() == 0 }) { "Char sequence must not have a null char ('\\0')." }
    val size = writeString(str, charset)
    writeByte(0)
    return size + 1
}

public fun Readable.readStringThenNull(charset: Charset = Charset.UTF8): String {
    val sb = StringBuilder()
    while (true) {
        val char = charset.decode(this)
        if (char == 0.toChar())
            break
        sb.append(char)
    }
    return sb.toString()
}

public fun Memory.Companion.array(str: CharSequence, charset: Charset = Charset.UTF8): ReadMemory =
    Memory.array(charset.sizeOf(str)) { writeString(str, charset) }

public fun Allocation.Companion.native(str: CharSequence, charset: Charset = Charset.UTF8): ReadAllocation =
    Allocation.native(charset.sizeOf(str)) { writeString(str, charset) }

public fun Readable.readLine(charset: Charset = Charset.UTF8): String? = buildString {
    var count = 0
    while (true) {
        val next = charset.tryDecode(this@readLine)
        if (next < 0) {
            if (count == 0) return null
            break
        }
        val nextChar = next.toChar()
        if (nextChar == '\n') break
        if (nextChar == '\r') {
            val after = charset.tryDecode(this@readLine)
            if (after == -1) {
                append(nextChar)
                break
            }
            val afterChar = after.toChar()
            if (afterChar == '\n') break
            append(nextChar)
            append(afterChar)
            continue
        }
        append(nextChar)
        ++count
    }
}
