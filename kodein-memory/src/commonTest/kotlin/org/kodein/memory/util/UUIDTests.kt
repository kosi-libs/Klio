package org.kodein.memory.util

import org.kodein.memory.io.KBuffer
import org.kodein.memory.io.array
import org.kodein.memory.io.hasRemaining
import org.kodein.memory.text.Charset
import org.kodein.memory.text.putString
import org.kodein.memory.text.readString
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotSame

class UUIDTests {

    @Test
    fun fromTime() {
        val id = UUID.timeUUID(unixTimestampMillis = 8506165020000, clockSequence = 21, node = 42)

        assertEquals(2, id.variant())
        assertEquals(1, id.version())

        assertEquals(8506165020000, id.unixTimestapMillis())
        assertEquals(21, id.clockSequence())
        assertEquals(42, id.node())
        assertEquals("d7af3600-50f0-12e0-8015-00000000002a", id.toString())
    }

    @Test
    fun fromString() {
        val id = UUID.fromString("d7af3600-50f0-12e0-8015-00000000002a")

        assertEquals(2, id.variant())
        assertEquals(1, id.version())

        assertEquals(8506165020000, id.unixTimestapMillis())
        assertEquals(21, id.clockSequence())
        assertEquals(42, id.node())
        assertEquals("d7af3600-50f0-12e0-8015-00000000002a", id.toString())
    }

    @Test
    fun random() {
        val id = UUID.randomUUID()
        assertEquals(2, id.variant())
        assertEquals(4, id.version())
        val other = KBuffer.array(16) { putUUID(id) } .readUUID()
        assertEquals(id, other)
        assertNotSame(id, other)
        assertEquals(id.toString(), other.toString())
    }

    @Test
    fun writeRead() {
        val id = UUID.randomUUID()
        val other = KBuffer.array(16) { putUUID(id) } .readUUID()
        assertEquals(id, other)
        assertNotSame(id, other)
        assertEquals(id.toString(), other.toString())
    }

    @Test
    fun from14Bytes() {
        val id = UUID.from14Bytes(KBuffer.array(20) {
            putLong(1234567890123456789L)
            putInt(123456789)
            putShort(2142)
            putString("abcdef", Charset.ASCII)
        })

        assertEquals(2, id.variant())
        assertEquals(12, id.version())

        val buffer = KBuffer.array(20) { id.write14Bytes(this) }
        assertEquals(1234567890123456789L, buffer.readLong())
        assertEquals(buffer.readInt(), 123456789)
        assertEquals(buffer.readShort(), 2142)
        assertFalse(buffer.hasRemaining())
    }

    @Test
    fun from8Bytes() {
        val id = UUID.from14Bytes(KBuffer.array(8) {
            putString("abcdefgh", Charset.ASCII)
        })

        assertEquals(2, id.variant())
        assertEquals(12, id.version())

        val buffer = KBuffer.array(8) { id.write14Bytes(this, 8) }
        assertEquals("abcdefgh", buffer.readString(Charset.ASCII, 8))
        assertFalse(buffer.hasRemaining())
    }
}
