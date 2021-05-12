package org.kodein.memory.io

import kotlin.test.*

@OptIn(ExperimentalStdlibApi::class)
class ReadMemoryTests {

    @Test
    fun compare() {
        assertEquals(0, Memory.wrap("Test".encodeToByteArray()).compareTo("Test".encodeToByteArray()))
        assertEquals(0, Memory.wrap("Test".encodeToByteArray()).compareTo(Memory.wrap("Test".encodeToByteArray())))

        assertTrue(Memory.wrap("Test".encodeToByteArray()).compareTo("Tests".encodeToByteArray()) < 0)
        assertTrue(Memory.wrap("Test".encodeToByteArray()).compareTo(Memory.wrap("Tests".encodeToByteArray())) < 0)

        assertTrue(Memory.wrap("Test".encodeToByteArray()).compareTo("Tesla".encodeToByteArray()) > 0)
        assertTrue(Memory.wrap("Test".encodeToByteArray()).compareTo(Memory.wrap("Tesla".encodeToByteArray())) > 0)
    }

    @Test
    fun firstIndexOf() {
        assertEquals(8, Memory.wrap("This is a test!".encodeToByteArray()).firstIndexOf('a'.code.toByte()))
        assertEquals(-1, Memory.wrap("This is a test!".encodeToByteArray()).firstIndexOf('w'.code.toByte()))
    }

    @Test
    fun startsWith() {
        assertTrue(Memory.wrap("This is a test!".encodeToByteArray()).startsWith("This".encodeToByteArray()))
        assertTrue(Memory.wrap("This is a test!".encodeToByteArray()).startsWith(Memory.wrap("This".encodeToByteArray())))

        assertFalse(Memory.wrap("This is a test!".encodeToByteArray()).startsWith("That".encodeToByteArray()))
        assertFalse(Memory.wrap("This is a test!".encodeToByteArray()).startsWith(Memory.wrap("That".encodeToByteArray())))
    }

}
