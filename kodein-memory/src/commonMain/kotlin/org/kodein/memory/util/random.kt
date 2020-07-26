package org.kodein.memory.util

import kotlin.random.Random

public fun Random.nextString(length: Int, chars: String = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"): String = buildString {
    repeat(length) {
        append(chars[nextInt(chars.length)])
    }
}
