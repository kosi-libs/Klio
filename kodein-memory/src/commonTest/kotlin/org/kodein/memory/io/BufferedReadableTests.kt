package org.kodein.memory.io

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue


class BufferedReadableTests {

    private data class Counter(var count: Int = 0)

    private fun sequence(counter: Counter? = null) = sequence {
        repeat(4) {
            counter?.let { it.count += 1 }
            yield(Memory.wrap(byteArrayOf(
                (it * 3 + 1).toByte(),
                (it * 3 + 2).toByte(),
                (it * 3 + 3).toByte(),
            )))
        }
    }

    @Test fun readAllBytes() {
        val result = ByteArray(12)
        BufferedMemoryPullReadable(sequence()).readBytes(result)
        assertTrue(byteArrayOf(0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09, 0x0A, 0x0B, 0x0C).contentEquals(result))
    }

    @Test fun readSomeBytes() {
        val counter = Counter()
        val readable = BufferedMemoryPullReadable(sequence(counter))
        val result = ByteArray(4)

        readable.readBytes(result)
        assertTrue(byteArrayOf(1, 2, 3, 4).contentEquals(result))
        assertEquals(counter.count, 2)

        readable.readBytes(result)
        assertTrue(byteArrayOf(5, 6, 7, 8).contentEquals(result))
        assertEquals(counter.count, 3)

        readable.readBytes(result)
        assertTrue(byteArrayOf(9, 10, 11, 12).contentEquals(result))
        assertEquals(counter.count, 4)

        assertFalse(readable.valid())
    }

    @Test
    fun bufferedInts() {
        val ints = BufferedMemoryPullReadable(sequence()).asIntSequence()
        assertEquals(listOf(0x01_02_03_04, 0x05_06_07_08, 0x09_0A_0B_0C), ints.toList())
    }

    @Test
    fun bufferedShorts() {
        val counter = Counter()
        val readable = BufferedMemoryPullReadable(sequence(counter))
        assertEquals(0, counter.count)
        assertEquals(0x01_02, readable.readShort())
        assertEquals(1, counter.count)
        assertEquals(0x03_04, readable.readShort())
        assertEquals(2, counter.count)
        assertEquals(0x05_06, readable.readShort())
        assertEquals(2, counter.count)
        assertEquals(0x07_08, readable.readShort())
        assertEquals(3, counter.count)
        assertEquals(0x09_0A, readable.readShort())
        assertEquals(4, counter.count)
        assertEquals(0x0B_0C, readable.readShort())
        assertEquals(4, counter.count)
        assertFalse(readable.valid())
    }

    @Test
    fun skip() {
        val counter = Counter()
        val readable = BufferedMemoryPullReadable(sequence(counter))
        assertEquals(0, counter.count)
        readable.skip(2)
        assertEquals(1, counter.count)
        readable.skip(2)
        assertEquals(2, counter.count)
        readable.skip(2)
        assertEquals(3, counter.count)
        readable.skip(4)
        assertEquals(4, counter.count)
        readable.skip(2)
        assertEquals(4, counter.count)
        assertFalse(readable.valid())
    }

}
