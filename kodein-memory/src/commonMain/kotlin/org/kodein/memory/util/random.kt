package org.kodein.memory.util

import kotlin.random.Random

fun Random.nextString(length: Int, chars: String = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789") = buildString {
    repeat(length) {
        append(chars[nextInt(chars.length)])
    }
}
