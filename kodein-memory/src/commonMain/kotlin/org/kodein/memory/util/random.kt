package org.kodein.memory.util

import org.kodein.memory.io.Memory
import org.kodein.memory.io.Writeable
import kotlin.math.min
import kotlin.random.Random

public fun Random.nextString(length: Int, chars: String = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"): String = buildString {
    repeat(length) {
        append(chars[nextInt(chars.length)])
    }
}

public fun Random.nextBytes(dst: Writeable, len: Int) {
    val buffer = ByteArray(min(len, 64))
    var remaining = len
    while (remaining > 0) {
        val count = min(remaining, 64)
        nextBytes(buffer, 0, count)
        dst.writeBytes(buffer, 0, count)
        remaining -= count
    }
}

public fun Random.nextBytes(dst: Memory, index: Int = 0, len: Int = dst.size - index) {
    val buffer = ByteArray(min(len, 64))
    var remaining = len
    var current = index
    while (remaining > 0) {
        val count = min(remaining, 64)
        nextBytes(buffer, 0, count)
        dst.setBytes(current, buffer, 0, count)
        current += count
        remaining -= count
    }
}
