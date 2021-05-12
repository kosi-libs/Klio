package org.kodein.memory.text

import org.kodein.memory.io.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CharsetTests {

    @Test
    fun testAscii() {
        val name = "Salomon BRYS"

        assertEquals(12, Charset.ASCII.sizeOf(name))

        val memory = Memory.array(12)
        val size = memory.asWriteable().writeString(name, Charset.ASCII)
        assertEquals(12, size)

        val read = memory.asReadable().readString(Charset.ASCII)
        assertEquals(name, read)
    }

    private fun testUTF16(charset: Charset.Type.UTF16Charset) {
        assertEquals(palindrome.length * 2, charset.sizeOf(palindrome))

        val memory = Memory.array(palindrome.length * 2)
        val size = memory.asWriteable().writeString(palindrome, charset)
        assertEquals(palindrome.length * 2, size)

        val read = memory.asReadable().readString(charset)
        assertEquals(palindrome, read)
    }

    @Test fun testUTF16BE() = testUTF16(Charset.UTF16BE)
    @Test fun testUTF16LE() = testUTF16(Charset.UTF16LE)

    fun testUTF16WithMark(charset: Charset.Type.UTF16Charset) {
        val memory = Memory.array(palindrome, Charset.UTF16(charset))
        assertEquals(palindrome.length * 2 + 2, memory.size)
        assertEquals(charset.byteOrderMark, memory.getShort(0))

        val slice = memory.sliceAt(2).asReadable().readString(charset)
        assertEquals(palindrome, slice)

        val read = memory.asReadable().readString(Charset.UTF16())
        assertEquals(palindrome, read)
    }

    @Test fun testUTF16withBEMark() = testUTF16WithMark(Charset.UTF16BE)
    @Test fun testUTF16withLEMark() = testUTF16WithMark(Charset.UTF16LE)

    @Test
    fun testUTF8() {
        val memory = Memory.array(palindrome, Charset.UTF8)
        assertTrue(memory.size in palindrome.length..(palindrome.length * 2))

        val read = memory.asReadable().readString(Charset.UTF8)
        assertEquals(palindrome, read)
    }

}
