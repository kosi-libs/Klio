package org.kodein.memory.io

import org.kodein.memory.assertNear
import org.kodein.memory.text.readChar
import org.kodein.memory.text.writeChar
import org.kodein.memory.use
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

abstract class AbstractMemoryTests : AbstractIOTests() {

    companion object {
        const val DEFAULT_SIZE = 16384
    }

    abstract fun alloc(size: Int = DEFAULT_SIZE): Allocation

    @Test
    fun primitivesWrite() {
        val alloc = alloc().useInScope()
        val slice = alloc.slice {
            writeByte(123)
            writeShort(12345)
            writeInt(1234567890)
            writeLong(1234567890123456L)
            writeFloat(1234.56f)
            writeDouble(123456789.987654321)
        }

        val r = slice.asReadable()

        assertEquals(27, r.remaining)

        assertEquals(123, r.readByte())
        assertEquals(12345, r.readShort())
        assertEquals(1234567890, r.readInt())
        assertEquals(1234567890123456L, r.readLong())
        assertNear(1234.56f, r.readFloat())
        assertEquals(123456789.987654321, r.readDouble())

        assertEquals(0, r.remaining)
    }

    @Test
    fun primitivesSet() {
        val alloc = alloc().useInScope()
        alloc[10] = 123
        alloc.putShort(30, 12345)
        alloc.putInt(40, 1234567890)
        alloc.putLong(50, 1234567890123456L)
        alloc.putFloat(60, 1234.56f)
        alloc.putDouble(70, 123456789.987654321)

        assertEquals(123, alloc[10])
        assertEquals(12345, alloc.getShort(30))
        assertEquals(1234567890, alloc.getInt(40))
        assertEquals(1234567890123456L, alloc.getLong(50))
        assertNear(1234.56f, alloc.getFloat(60))
        assertEquals(123456789.987654321, alloc.getDouble(70))
    }

    @Test
    fun bulkPut() {
        val alloc = alloc().useInScope()
        val r = alloc.slice {
            writeBytes(byteArrayOf(1, 2, 3))
            writeBytes(byteArrayOf(1, 2, 3, 4, 5, 6, 7, 8, 9), 3, 3)
            writeBytes(byteArrayOf(1, 2, 3, 4, 5, 6, 7, 8, 9), 6, 3)

            alloc().use { src ->
                src.putBytes(0, byteArrayOf(10, 11, 12))
                writeBytes(src.slice(0, 3))
            }

            alloc().use { src ->
                src.asWriteable().writeBytes(byteArrayOf(13, 14, 15, 16, 17, 18))
                writeBytes(src.slice(0, 6).asReadable(), 3)
            }
        }.asReadable()

        val dst1 = ByteArray(5)
        r.readBytes(dst1)
        assertTrue(byteArrayOf(1, 2, 3, 4, 5).contentEquals(dst1))

        val dst2 = ByteArray(7)
        r.readBytes(dst2, 1, 5)
        assertTrue(byteArrayOf(0, 6, 7, 8, 9, 10, 0).contentEquals(dst2))

        val dst3 = ByteArray(6)
        r.readBytes(dst3, 1, 5)
        assertTrue(byteArrayOf(0, 11, 12, 13, 14, 15).contentEquals(dst3))
    }

    @Test
    fun bulkSet() {
        val alloc = alloc().useInScope()

        alloc.putBytes(0, byteArrayOf(1, 2, 3))
        alloc.putBytes(3, byteArrayOf(1, 2, 3, 4, 5, 6, 7, 8, 9), 3, 3)
        alloc.putBytes(6, byteArrayOf(1, 2, 3, 4, 5, 6, 7, 8, 9), 6, 3)

        alloc().use { src ->
            src.putBytes(0, byteArrayOf(10, 11, 12))
            alloc.putBytes(9, src.slice(0, 3))
        }

        alloc().use { src ->
            src.asWriteable().writeBytes(byteArrayOf(10, 11, 12, 13, 14, 15, 16, 17, 18))
            alloc.putBytes(12, src.slice( 3, 3))
        }

        val dst1 = ByteArray(5)
        alloc.getBytes(0, dst1)
        assertTrue(byteArrayOf(1, 2, 3, 4, 5).contentEquals(dst1))

        val dst2 = ByteArray(7)
        alloc.getBytes(5, dst2, 1, 5)
        assertTrue(byteArrayOf(0, 6, 7, 8, 9, 10, 0).contentEquals(dst2))

        val dst3 = ByteArray(6)
        alloc.getBytes(10, dst3, 1, 5)
        assertTrue(byteArrayOf(0, 11, 12, 13, 14, 15).contentEquals(dst3))
    }

    @Test
    fun arrays() {
        val r = alloc().useInScope().slice {
            writeBytes(byteArrayOf(10, 11, 12, 13, 14, 15))
        }.asReadable()

        assertEquals(6, r.remaining)

        val read = ByteArray(6)
        r.readBytes(read)

        assertEquals(0, r.remaining)

        assertTrue(read.contentEquals(byteArrayOf(10, 11, 12, 13, 14, 15)))
    }

    @Test
    fun arraysOffset() {
        val r = alloc().useInScope().slice {
            writeBytes(byteArrayOf(5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20), 5, 6)
        }.asReadable()

        assertNotEquals(0, r.remaining)

        val read = ByteArray(8)
        r.readBytes(read, 1, 6)

        assertEquals(0, r.remaining)

        assertTrue(read.contentEquals(byteArrayOf(0, 10, 11, 12, 13, 14, 15, 0)))
    }

    @Test
    fun readable() {
        val src = alloc().useInScope().slice {
            writeByte(123)
            writeChar('*')
            writeShort(12345)
            writeInt(1234567890)
            writeLong(1234567890123456L)
            writeFloat(1234.56f)
            writeDouble(123456789.987654321)
        }

        val dst = alloc().useInScope().slice {
            writeBytes(src)
        }.asReadable()

        assertNotEquals(0, dst.remaining)

        assertEquals(123, dst.readByte())
        assertEquals('*', dst.readChar())
        assertEquals(12345, dst.readShort())
        assertEquals(1234567890, dst.readInt())
        assertEquals(1234567890123456L, dst.readLong())
        assertNear(1234.56f, dst.readFloat())
        assertEquals(123456789.987654321, dst.readDouble())

        assertEquals(0, dst.remaining)
    }

    @Test
    fun readableOffset() {
        val src = alloc().useInScope().slice {
            writeBytes(byteArrayOf(1, 2, 3, 4, 5, 6, 7, 8, 9))
        }

        val dst = alloc().useInScope().slice {
            writeBytes(src.slice(3, 3))
        }.asReadable()

        assertEquals(3, dst.remaining)

        assertEquals(4, dst.readByte())
        assertEquals(5, dst.readByte())
        assertEquals(6, dst.readByte())

        assertEquals(0, dst.remaining)
    }

    @Test
    fun manipulation() {
        val alloc = alloc().useInScope()

        assertEquals(DEFAULT_SIZE, alloc.size)

        val slice = alloc.slice {
            writeByte(123)
            writeChar('*')
            writeShort(12345)
            writeInt(1234567890)

            assertEquals(8, position)
        }

        assertEquals(8, slice.size)

        val r = slice.asReadable()
        assertEquals(0, r.position)
        assertEquals(8, r.remaining)
        r.skip(5)
        assertEquals(3, r.remaining)
        assertEquals(5, r.position)
    }

    @Test
    fun slice() {
        val memory = alloc().useInScope().slice { writeBytes(byteArrayOf(1, 2, 3, 4, 5, 6, 7, 8, 9)) }

        val slice1 = memory.slice(3, 3).asReadable()
        assertEquals(0, slice1.position)
        assertEquals(0, slice1.position)
        assertEquals(3, slice1.remaining)
        assertTrue(byteArrayOf(4, 5, 6).contentEquals(slice1.readBytes()))

        val slice2 = memory.sliceAt(6).asReadable()
        assertEquals(0, slice2.position)
        assertEquals(3, slice2.remaining)
        assertTrue(byteArrayOf(7, 8, 9).contentEquals(slice2.readBytes()))
    }

}
