package org.kodein.memory.util

import kotlinx.cinterop.*
import platform.posix.O_RDONLY
import platform.posix.close
import platform.posix.open
import platform.posix.read
import kotlin.random.Random


private object SecureRandomPosix : Random() {

    override fun nextBytes(array: ByteArray, fromIndex: Int, toIndex: Int): ByteArray {
        require(fromIndex in 0..array.size && toIndex in 0..array.size) { "fromIndex ($fromIndex) or toIndex ($toIndex) are out of range: 0..${array.size}." }
        require(fromIndex <= toIndex) { "fromIndex ($fromIndex) must be not greater than toIndex ($toIndex)." }

        val size = toIndex - fromIndex
        require(size >= 0) { "negative size" }
        if (size == 0) return array

        val fd = open("/dev/urandom", O_RDONLY).takeIf { it >= 0 } ?: error("Could not open /dev/urandom")
        try {
            array.usePinned { pinnedBytes ->
                val read = read(fd, pinnedBytes.addressOf(fromIndex), size.convert())
                when {
                    read < 0 -> error("Could not read /dev/urandom")
                    read < size -> error("Interrupted while reading /dev/urandom")
                }
                return array
            }
        } finally {
            close(fd)
        }
    }

    override fun nextInt(): Int {
        val a = ByteArray(Int.SIZE_BYTES)
        nextBytes(a)
        return (a[0].toInt() shl 24) or
                (a[1].toInt() shl 16) or
                (a[2].toInt() shl 8) or
                (a[3].toInt() shl 0)
    }

    override fun nextBits(bitCount: Int): Int = nextInt().takeUpperBits(bitCount)
}

public actual fun Random.Default.secure(): Random = SecureRandomPosix
