package org.kodein.memory

import kotlin.test.*

abstract class AbstractKBufferTests {

    companion object {
        const val DEFAULT_SIZE = 16384
    }

    abstract fun alloc(size: Int = DEFAULT_SIZE): Allocation

    @Test
    fun primitivesPut() {
        alloc().use {
            it.put(123)
            it.putChar('*')
            it.putShort(12345)
            it.putInt(1234567890)
            it.putLong(1234567890123456L)
            it.putFloat(123456.789f)
            it.putDouble(123456789.987654321)

            it.flip()

            assertNotEquals(0, it.remaining)

            assertEquals(123, it.read())
            assertEquals('*', it.readChar())
            assertEquals(12345, it.readShort())
            assertEquals(1234567890, it.readInt())
            assertEquals(1234567890123456L, it.readLong())
            assertEquals(123456.789f, it.readFloat())
            assertEquals(123456789.987654321, it.readDouble())

            assertEquals(0, it.remaining)
        }
    }

    @Test
    fun primitivesSet() {
        alloc().use {
            it.set(10, 123)
            it.setChar(20, '*')
            it.setShort(30, 12345)
            it.setInt(40, 1234567890)
            it.setLong(50, 1234567890123456L)
            it.setFloat(60, 123456.789f)
            it.setDouble(70, 123456789.987654321)

            assertEquals(0, it.position)

            assertEquals(123, it.get(10))
            assertEquals('*', it.getChar(20))
            assertEquals(12345, it.getShort(30))
            assertEquals(1234567890, it.getInt(40))
            assertEquals(1234567890123456L, it.getLong(50))
            assertEquals(123456.789f, it.getFloat(60))
            assertEquals(123456789.987654321, it.getDouble(70))

            assertEquals(0, it.position)
        }
    }

    @Test
    fun arrays() {
        alloc().use {
            val array = byteArrayOf(10, 11, 12, 13, 14, 15)
            it.putBytes(array)

            it.flip()

            assertNotEquals(0, it.remaining)

            val read = ByteArray(6)
            it.readBytes(read)

            assertEquals(0, it.remaining)

            assertTrue(read.contentEquals(byteArrayOf(10, 11, 12, 13, 14, 15)))
        }
    }

    @Test
    fun arraysOffset() {
        alloc().use {
            val array = byteArrayOf(5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20)
            it.putBytes(array, 5, 6)

            it.flip()

            assertNotEquals(0, it.remaining)

            val read = ByteArray(8)
            it.readBytes(read, 1, 6)

            assertEquals(0, it.remaining)

            assertTrue(read.contentEquals(byteArrayOf(0, 10, 11, 12, 13, 14, 15, 0)))
        }
    }

    @Test
    fun readable() {
        alloc().use { dst ->
            alloc().use { src ->
                src.put(123)
                src.putChar('*')
                src.putShort(12345)
                src.putInt(1234567890)
                src.putLong(1234567890123456L)
                src.putFloat(123456.789f)
                src.putDouble(123456789.987654321)

                src.flip()
                dst.putBytes(src)
            }

            dst.flip()

            assertNotEquals(0, dst.remaining)

            assertEquals(123, dst.read())
            assertEquals('*', dst.readChar())
            assertEquals(12345, dst.readShort())
            assertEquals(1234567890, dst.readInt())
            assertEquals(1234567890123456L, dst.readLong())
            assertEquals(123456.789f, dst.readFloat())
            assertEquals(123456789.987654321, dst.readDouble())

            assertEquals(0, dst.remaining)
        }
    }

    @Test
    fun readableOffset() {
        alloc().use { dst ->
            alloc().use { src ->
                src.put(123)
                src.putChar('*')
                src.putShort(12345)
                src.putInt(1234567890)
                src.putLong(1234567890123456L)

                src.flip()
                src.skip(3)
                dst.putBytes(src, 6)
            }

            dst.flip()

            assertNotEquals(0, dst.remaining)

            assertEquals(12345, dst.readShort())
            assertEquals(1234567890, dst.readInt())

            assertEquals(0, dst.remaining)
        }
    }

    @Test
    fun manipulation() {
        alloc().use {
            assertEquals(DEFAULT_SIZE, it.capacity)
            assertEquals(DEFAULT_SIZE, it.limit)
            assertEquals(0, it.position)

            it.put(123)
            it.putChar('*')
            it.putShort(12345)
            it.putInt(1234567890)

            assertEquals(9, it.position)

            it.flip()

            assertEquals(9, it.limit)
            assertEquals(0, it.position)

            it.clear()

            assertEquals(DEFAULT_SIZE, it.limit)
            assertEquals(0, it.position)

            it.skip(5)

            assertEquals(DEFAULT_SIZE, it.limit)
            assertEquals(5, it.position)

            it.rewind()

            assertEquals(DEFAULT_SIZE, it.limit)
            assertEquals(0, it.position)
        }
    }

    @Test
    fun mark() {
        alloc().use {
            it.putInt(1234567890)
            it.mark()
            it.putLong(1234567890123456L)
            it.reset()
            assertEquals(1234567890123456L, it.readLong())
        }
    }

    @Test
    fun slice() {
        alloc().use {
            it.putInt(1234567890)
            val slice = it.slice()
            assertEquals(4, it.position)
            assertEquals(0, slice.position)
            assertEquals(DEFAULT_SIZE, it.capacity)
            assertEquals(DEFAULT_SIZE, it.limit)
            assertEquals(DEFAULT_SIZE - 4, slice.capacity)
            assertEquals(DEFAULT_SIZE - 4, slice.limit)
            it.putLong(1234567890123456L)
            assertEquals(1234567890123456, slice.readLong())
        }
    }
}
