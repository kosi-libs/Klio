package org.kodein.memory.util


import java.security.SecureRandom
import java.security.SecureRandomParameters
import kotlin.random.Random

private object SecureRandomJvm : Random() {

    private val random: SecureRandom = SecureRandom()

    override fun nextBytes(array: ByteArray): ByteArray {
        random.nextBytes(array)
        return array
    }

    override fun nextBytes(array: ByteArray, fromIndex: Int, toIndex: Int): ByteArray {
        require(fromIndex in 0..array.size && toIndex in 0..array.size) { "fromIndex ($fromIndex) or toIndex ($toIndex) are out of range: 0..${array.size}." }
        require(fromIndex <= toIndex) { "fromIndex ($fromIndex) must be not greater than toIndex ($toIndex)." }

        if (fromIndex == 0 && toIndex == array.size) return nextBytes(array)

        val buffer = nextBytes(toIndex - fromIndex)
        buffer.copyInto(array, fromIndex)
        return array
    }

    override fun nextBits(bitCount: Int): Int = random.nextInt().takeUpperBits(bitCount)

    override fun nextInt(): Int = random.nextInt()
    override fun nextInt(until: Int): Int = random.nextInt(until)
    override fun nextLong(): Long = random.nextLong()
    override fun nextBoolean(): Boolean = random.nextBoolean()
    override fun nextDouble(): Double = random.nextDouble()
    override fun nextFloat(): Float = random.nextFloat()
}

public actual fun Random.Default.secure(): Random = SecureRandomJvm
