package org.kodein.memory.util

import kotlin.random.Random


// https://github.com/Kotlin/KEEP/issues/184
public expect fun Random.Default.secure(): Random

// https://github.com/JetBrains/kotlin/blob/79e426270cb6e2700bf227ee44b9eaca2c3d1eb4/libraries/stdlib/src/kotlin/random/Random.kt#L376
internal fun Int.takeUpperBits(bitCount: Int): Int = this.ushr(32 - bitCount) and (-bitCount).shr(31)
