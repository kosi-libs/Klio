package org.kodein.memory.util

import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertTrue

class SecureRandomTests {

    @Test fun randomInts() {
        val values = IntArray(200)

        repeat(values.size) { index ->
            values[index] = Random.secure().nextInt()
        }

        assertTrue(values.any { it != values[0] })
    }

    @Test fun randomBoundInts() {
        val values = IntArray(200)

        repeat(values.size) { index ->
            values[index] = Random.secure().nextInt(1000, 2000)
        }

        assertTrue(values.any { it != values[0] })
        assertTrue { values.all { it in 1000..1999 } }
    }

    @Test fun randomBytes() {
        val values = ByteArray(200)

        Random.secure().nextBytes(values)

        assertTrue(values.any { it != values[0] })
    }

    @Test fun randomBits() {
        val values = IntArray(200)

        repeat(values.size) { index ->
            values[index] = Random.secure().nextBits(8)
        }

        assertTrue(values.any { it != values[0] })
        assertTrue { values.all { it in 0..255 } }
    }

}
