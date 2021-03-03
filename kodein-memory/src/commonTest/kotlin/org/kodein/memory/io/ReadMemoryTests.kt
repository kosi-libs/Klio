package org.kodein.memory.io

import org.kodein.memory.text.readString
import kotlin.test.*

class ReadMemoryTests {

    @Test
    fun compare() {
        assertEquals(0, KBuffer.wrap("Test".encodeToByteArray()).compareTo("Test".encodeToByteArray()))
        assertEquals(0, KBuffer.wrap("Test".encodeToByteArray()).compareTo(KBuffer.wrap("Test".encodeToByteArray())))

        assertTrue(KBuffer.wrap("Test".encodeToByteArray()).compareTo("Tests".encodeToByteArray()) < 0)
        assertTrue(KBuffer.wrap("Test".encodeToByteArray()).compareTo(KBuffer.wrap("Tests".encodeToByteArray())) < 0)

        assertTrue(KBuffer.wrap("Test".encodeToByteArray()).compareTo("Tesla".encodeToByteArray()) > 0)
        assertTrue(KBuffer.wrap("Test".encodeToByteArray()).compareTo(KBuffer.wrap("Tesla".encodeToByteArray())) > 0)
    }

    @Test
    fun firstIndexOf() {
        assertEquals(8, KBuffer.wrap("This is a test!".encodeToByteArray()).firstIndexOf('a'.toByte()))
        assertEquals(-1, KBuffer.wrap("This is a test!".encodeToByteArray()).firstIndexOf('w'.toByte()))
    }

    @Test
    fun startsWith() {
        assertTrue(KBuffer.wrap("This is a test!".encodeToByteArray()).startsWith("This".encodeToByteArray()))
        assertTrue(KBuffer.wrap("This is a test!".encodeToByteArray()).startsWith(KBuffer.wrap("This".encodeToByteArray())))

        assertFalse(KBuffer.wrap("This is a test!".encodeToByteArray()).startsWith("That".encodeToByteArray()))
        assertFalse(KBuffer.wrap("This is a test!".encodeToByteArray()).startsWith(KBuffer.wrap("That".encodeToByteArray())))
    }

    @Test
    fun markBuffer() {
        val buffer = KBuffer.wrap("This is a test!".encodeToByteArray())
        var passed = false
        assertEquals(0, buffer.position)
        buffer.markBuffer {
            it.skip(5)
            assertEquals(5, it.position)
            assertEquals("is a", it.readString(maxChars = 4))
            assertEquals(9, it.position)
            passed = true
        }
        assertTrue(passed)
        assertEquals(0, buffer.position)
    }
}
