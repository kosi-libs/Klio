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

public fun Random.nextBytes(dst: Memory) {
    val buffer = ByteArray(min(dst.size, 64))
    var remaining = dst.size
    var current = 0
    while (remaining > 0) {
        val count = min(remaining, 64)
        nextBytes(buffer, 0, count)
        dst.putBytes(current, buffer, 0, count)
        current += count
        remaining -= count
    }
}
