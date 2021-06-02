package org.kodein.memory.util

import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.kodein.memory.io.Memory
import org.kodein.memory.io.array
import org.kodein.memory.io.asReadable
import org.kodein.memory.text.arrayFromHex
import org.kodein.memory.text.byteArrayFromHex
import org.kodein.memory.text.toHex
import kotlin.test.*

class UUIDTests {

    @Test
    fun fromTime() {
        val id = UUID.timeUUID(unixTimestampMillis = 8506165020000, clockSequence = 21, node = 42)

        assertEquals(1, id.variant())
        assertEquals(1, id.version())

        assertEquals(8506165020000, id.unixTimestamp())
        assertEquals(21, id.clockSequence())
        assertEquals(42, id.node())
        assertEquals("d7af3600-50f0-12e0-8015-00000000002a", id.toString())
    }

    @Test
    fun fromString() {
        val id = UUID.fromString("d7af3600-50f0-12e0-8015-00000000002a")

        assertEquals(1, id.variant())
        assertEquals(1, id.version())

        assertEquals(8506165020000, id.unixTimestamp())
        assertEquals(21, id.clockSequence())
        assertEquals(42, id.node())
        assertEquals("d7af3600-50f0-12e0-8015-00000000002a", id.toString())
    }

    @Test
    fun random() {
        val id = UUID.randomUUID()
        assertEquals(1, id.variant())
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

    @Test
    fun nilTest() {
        assertEquals("00000000-0000-0000-0000-000000000000", UUID.NIL.toString())
        assertEquals(0, UUID.NIL.version())
        assertEquals(0, UUID.NIL.variant())
    }

    @Test
    @Suppress("EXPERIMENTAL_UNSIGNED_LITERALS")
    fun rawLongsTest() {
        val uuid = UUID.fromRawLongs(21uL, 42uL, 6)

        assertEquals("00000000-0000-6015-8000-00000000002a", uuid.toString())

        assertEquals(6, uuid.version())
        assertEquals(1, uuid.variant())

        assertEquals(21uL, uuid.rawLongHigh())
        assertEquals(42uL, uuid.rawLongLow())
    }

    @Test
    @Suppress("EXPERIMENTAL_UNSIGNED_LITERALS")
    fun rawPartsLongTest() {
        val uuid = UUID.fromRawParts(0x12345678u, 0xABCDu, 0x0CCCu, 0x369Cu, 0xF0E1D2C3B4A5u, 6)

        assertEquals("12345678-abcd-6ccc-b69c-f0e1d2c3b4a5", uuid.toString())

        assertEquals(6, uuid.version())
        assertEquals(1, uuid.variant())

        assertEquals(0x12345678u, uuid.part1())
        assertEquals(0xABCDu, uuid.part2())
        assertEquals(0x0CCCu, uuid.part3())
        assertEquals(0x369Cu, uuid.part4())
        assertEquals(0xF0E1D2C3B4A5u, uuid.part5Long())
    }

    @Test
    @OptIn(ExperimentalStdlibApi::class)
    @Suppress("EXPERIMENTAL_UNSIGNED_LITERALS")
    fun rawPartsByteTest() {
        val uuid = UUID.fromRawParts(0x12345678u, 0xABCDu, 0x0CCCu, 0x369Cu, Memory.arrayFromHex("F0E1D2C3B4A5"), 6)

        assertEquals("12345678-abcd-6ccc-b69c-f0e1d2c3b4a5", uuid.toString())

        assertEquals("F0E1D2C3B4A5", uuid.part5Bytes().toHex().uppercase())
    }

    @Test
    @OptIn(ExperimentalStdlibApi::class)
    fun rawBytesTest() {
        val uuid = UUID.fromRawBytes(Memory.arrayFromHex("00112233445566778899AABBCCDDEE"), 15)

        assertEquals("00112233-4455-f667-8788-99aabbccddee", uuid.toString())

        assertEquals(15, uuid.version())
        assertEquals(1, uuid.variant())

        assertEquals("00112233445566778899AABBCCDDEE", uuid.rawBytes().toHex().uppercase())
    }
}
