package org.kodein.memory.util

import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.kodein.memory.io.Memory
import org.kodein.memory.io.array
import org.kodein.memory.io.asReadable
import kotlin.test.*

class UUIDTests {

    @Test
    fun fromTime() {
        val id = UUID.timeUUID(unixTimestampMillis = 8506165020000, clockSequence = 21, node = 42)

        assertEquals(2, id.variant())
        assertEquals(1, id.version())

        assertEquals(8506165020000, id.unixTimestamp())
        assertEquals(21, id.clockSequence())
        assertEquals(42, id.node())
        assertEquals("d7af3600-50f0-12e0-8015-00000000002a", id.toString())
    }

    @Test
    fun fromString() {
        val id = UUID.fromString("d7af3600-50f0-12e0-8015-00000000002a")

        assertEquals(2, id.variant())
        assertEquals(1, id.version())

        assertEquals(8506165020000, id.unixTimestamp())
        assertEquals(21, id.clockSequence())
        assertEquals(42, id.node())
        assertEquals("d7af3600-50f0-12e0-8015-00000000002a", id.toString())
    }

    @Test
    fun random() {
        val id = UUID.randomUUID()
        assertEquals(2, id.variant())
        assertEquals(4, id.version())
        val other = Memory.array(16).apply { putUUID(0, id) } .getUUID(0)
        assertEquals(id, other)
        assertNotSame(id, other)
        assertEquals(id.toString(), other.toString())
    }

    @Test
    fun writeRead() {
        val id = UUID.randomUUID()
        val other = Memory.array(16) { writeUUID(id) } .asReadable().readUUID()
        assertEquals(id, other)
        assertNotSame(id, other)
        assertEquals(id.toString(), other.toString())
    }

    @Serializable
    data class StringDocument(val id: UUID)

    @Test
    fun serializedStrings() {
        val doc = StringDocument(UUID.fromString("d7af3600-50f0-12e0-8015-00000000002a"))
        val str = Json.encodeToString(doc)
        assertEquals("""{"id":"d7af3600-50f0-12e0-8015-00000000002a"}""", str)
        val doc2 = Json.decodeFromString<StringDocument>(str)
        assertEquals(doc, doc2)
    }

    @Serializable
    data class BinaryDocument(@Serializable(with = UUID.KXBinarySerializer::class) val id: UUID)

    @Test
    fun serializedBits() {
        val doc = BinaryDocument(UUID.fromString("d7af3600-50f0-12e0-8015-00000000002a"))
        val str = Json.encodeToString(doc)
        assertEquals("""{"id":[-2905043859644869920,-9217461062343851990]}""", str)
        val doc2 = Json.decodeFromString<BinaryDocument>(str)
        assertEquals(doc, doc2)
    }
}
