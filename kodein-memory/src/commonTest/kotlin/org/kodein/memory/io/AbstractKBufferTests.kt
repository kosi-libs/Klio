package org.kodein.memory.io

import org.kodein.memory.assertNear
import org.kodein.memory.use
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

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
            it.putFloat(1234.56f)
            it.putDouble(123456789.987654321)

            it.flip()

            assertEquals(29, it.remaining)

            assertEquals(123, it.read())
            assertEquals('*', it.readChar())
            assertEquals(12345, it.readShort())
            assertEquals(1234567890, it.readInt())
            assertEquals(1234567890123456L, it.readLong())
            assertNear(1234.56f, it.readFloat())
            assertEquals(123456789.987654321, it.readDouble())

            assertEquals(0, it.remaining)
        }
    }

    @Test
    fun primitivesSet() {
        alloc().use {
            it[10] = 123
            it.setChar(20, '*')
            it.setShort(30, 12345)
            it.setInt(40, 1234567890)
            it.setLong(50, 1234567890123456L)
            it.setFloat(60, 1234.56f)
            it.setDouble(70, 123456789.987654321)

            assertEquals(0, it.position)

            assertEquals(123, it[10])
            assertEquals('*', it.getChar(20))
            assertEquals(12345, it.getShort(30))
            assertEquals(1234567890, it.getInt(40))
            assertEquals(1234567890123456L, it.getLong(50))
            assertNear(1234.56f, it.getFloat(60))
            assertEquals(123456789.987654321, it.getDouble(70))

            assertEquals(0, it.position)
        }
    }

    @Test
    fun bulkPut() {
        alloc().use {
            it.putBytes(byteArrayOf(1, 2, 3))
            it.putBytes(byteArrayOf(1, 2, 3, 4, 5, 6, 7, 8, 9), 3, 3)
            it.putBytes(byteArrayOf(1, 2, 3, 4, 5, 6, 7, 8, 9), 6)

            alloc().use { src ->
                src.putBytes(byteArrayOf(10, 11, 12))
                src.flip()
                it.putBytes(src)
            }

            alloc().use { src ->
                src.putBytes(byteArrayOf(13, 14, 15, 16, 17, 18))
                src.flip()
                it.putBytes(src, 3)
            }

            it.flip()

            val dst1 = ByteArray(5)
            it.readBytes(dst1)
            assertTrue(byteArrayOf(1, 2, 3, 4, 5).contentEquals(dst1))

            val dst2 = ByteArray(7)
            it.readBytes(dst2, 1, 5)
            assertTrue(byteArrayOf(0, 6, 7, 8, 9, 10, 0).contentEquals(dst2))

            val dst3 = ByteArray(6)
            it.readBytes(dst3, 1)
            assertTrue(byteArrayOf(0, 11, 12, 13, 14, 15).contentEquals(dst3))
        }
    }

    @Test
    fun bulkSet() {
        alloc().use {
            it.limit = 15

            it.setBytes(0, byteArrayOf(1, 2, 3))
            it.setBytes(3, byteArrayOf(1, 2, 3, 4, 5, 6, 7, 8, 9), 3, 3)
            it.setBytes(6, byteArrayOf(1, 2, 3, 4, 5, 6, 7, 8, 9), 6)

            alloc().use { src ->
                src.putBytes(byteArrayOf(10, 11, 12))
                src.flip()
                it.setBytes(9, src)
            }

            alloc().use { src ->
                src.putBytes(byteArrayOf(10, 11, 12, 13, 14, 15, 16, 17, 18))
                src.flip()
                it.setBytes(12, src, 3, 3)
            }

            val dst1 = ByteArray(5)
            it.getBytes(0, dst1)
            assertTrue(byteArrayOf(1, 2, 3, 4, 5).contentEquals(dst1))

            val dst2 = ByteArray(7)
            it.getBytes(5, dst2, 1, 5)
            assertTrue(byteArrayOf(0, 6, 7, 8, 9, 10, 0).contentEquals(dst2))

            val dst3 = ByteArray(6)
            it.getBytes(10, dst3, 1)
            assertTrue(byteArrayOf(0, 11, 12, 13, 14, 15).contentEquals(dst3))
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
                src.putFloat(1234.56f)
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
            assertNear(1234.56f, dst.readFloat())
            assertEquals(123456789.987654321, dst.readDouble())

            assertEquals(0, dst.remaining)
        }
    }

    @Test
    fun readableOffset() {
        alloc().use { dst ->
            alloc().use { src ->
                src.putBytes(byteArrayOf(1, 2, 3, 4, 5, 6, 7, 8, 9))
                src.flip()
                src.skip(3)
                dst.putBytes(src, 3)
            }

            dst.flip()

            assertEquals(3, dst.remaining)

            assertEquals(4, dst.read())
            assertEquals(5, dst.read())
            assertEquals(6, dst.read())

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

            it.reset()

            assertEquals(DEFAULT_SIZE, it.limit)
            assertEquals(0, it.position)

            it.skip(5)

            assertEquals(DEFAULT_SIZE, it.limit)
            assertEquals(5, it.position)
        }
    }

    @Test
    fun slice() {
        alloc().use {
            it.putBytes(byteArrayOf(1, 2, 3, 4, 5, 6, 7, 8, 9))
            it.flip()

            val view1 = it.slice(3, 3)
            assertEquals(0, view1.position)
            assertEquals(0, view1.position)
            assertEquals(DEFAULT_SIZE, view1.capacity)
            assertEquals(3, view1.offset)
            assertEquals(3, view1.limit)
            assertTrue(byteArrayOf(4, 5, 6).contentEquals(view1.readBytes()))

            val view2 = it.slice(6)
            assertEquals(0, view2.position)
            assertEquals(DEFAULT_SIZE, view2.capacity)
            assertEquals(6, view2.offset)
            assertEquals(3, view2.limit)
            assertTrue(byteArrayOf(7, 8, 9).contentEquals(view2.readBytes()))
        }
    }

    @Test
    fun view() {
        alloc().use {
            it.putBytes(byteArrayOf(1, 2, 3, 4, 5, 6, 7, 8, 9))
            it.flip()

            it.view(3, 3) {
                assertEquals(0, it.position)
                assertEquals(0, it.position)
                assertEquals(DEFAULT_SIZE, it.capacity)
                assertEquals(3, it.offset)
                assertEquals(3, it.limit)
                assertTrue(byteArrayOf(4, 5, 6).contentEquals(it.readBytes()))
            }

            it.view(6) {
                assertEquals(0, it.position)
                assertEquals(DEFAULT_SIZE, it.capacity)
                assertEquals(6, it.offset)
                assertEquals(3, it.limit)
                assertTrue(byteArrayOf(7, 8, 9).contentEquals(it.readBytes()))
            }
        }
    }

    @Test
    fun sliceWrite() {

        alloc().use { base ->
            base.put(123)
            base.slice().also { slice ->
                slice.set(0, 123)
                slice.setChar(1, '*')
                slice.setShort(3, 12345)
                slice.setInt(5, 1234567890)
                slice.setLong(9, 1234567890123456L)
                slice.setFloat(17, 1234.56f)
                slice.setDouble(21, 123456789.987654321)
                slice.setBytes(29, byteArrayOf(1, 2, 3, 4, 5))
            }

            base.reset()
            base.slice(1).also { slice ->
                assertEquals(123, slice.get(0))
                assertEquals('*', slice.getChar(1))
                assertEquals(12345, slice.getShort(3))
                assertEquals(1234567890, slice.getInt(5))
                assertEquals(1234567890123456L, slice.getLong(9))
                assertNear(1234.56f, slice.getFloat(17))
                assertEquals(123456789.987654321, slice.getDouble(21))
                assertTrue(byteArrayOf(1, 2, 3, 4, 5).contentEquals(slice.getBytes(29, 5)))
            }
        }

    }

}
