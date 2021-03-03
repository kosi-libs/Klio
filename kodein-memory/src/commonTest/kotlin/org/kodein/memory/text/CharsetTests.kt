package org.kodein.memory.text

import org.kodein.memory.io.KBuffer
import org.kodein.memory.io.array
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CharsetTests {

    @Test
    fun testAscii() {
        val name = "Salomon BRYS"

        assertEquals(12, Charset.ASCII.sizeOf(name))

        val buffer = KBuffer.array(12)
        buffer.putString(name, Charset.ASCII)

        assertFalse(buffer.valid())

        buffer.flip()

        val read = buffer.readString(Charset.ASCII)
        assertEquals(name, read)
    }

    @Test
    fun testUTF16() {
        assertEquals(palindrome.length * 2, Charset.UTF16.sizeOf(palindrome))

        val buffer = KBuffer.array(palindrome.length * 2)
        buffer.putString(palindrome, Charset.UTF16)

        assertFalse(buffer.valid())

        buffer.flip()

        val read = buffer.readString(Charset.UTF16)
        assertEquals(palindrome, read)
    }

    @Test
    fun testUTF8() {
        val buffer = KBuffer.wrap(palindrome, Charset.UTF8)

        assertTrue(palindrome.length < buffer.remaining)
        assertTrue((palindrome.length * 2) > buffer.remaining)

        val read = buffer.readString(Charset.UTF8)

        assertEquals(palindrome, read)
    }

}
