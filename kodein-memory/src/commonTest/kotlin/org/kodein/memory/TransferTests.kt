package org.kodein.memory

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class TransferTests {

    companion object {
        const val DEFAULT_SIZE = 16384
    }

    @Test
    fun equalsAndHashcode() {
        Allocation.array(DEFAULT_SIZE).use { arrayBuffer ->
            Allocation.native(DEFAULT_SIZE).use { nativeBuffer ->
                listOf(arrayBuffer, nativeBuffer).forEach {
                    it.put(123)
                    it.putChar('*')
                    it.putShort(12345)
                    it.putInt(1234567890)
                    it.putLong(1234567890123456L)
                    it.putFloat(123456.789f)
                    it.putDouble(123456789.987654321)
                    it.flip()
                }

                assertEquals(arrayBuffer, nativeBuffer)
                assertEquals(arrayBuffer.hashCode(), nativeBuffer.hashCode())

                listOf(arrayBuffer, nativeBuffer).forEach { it.skip(4) }

                assertEquals(arrayBuffer, nativeBuffer)
                assertEquals(arrayBuffer.hashCode(), nativeBuffer.hashCode())

                arrayBuffer.position = 0

                assertNotEquals(arrayBuffer, nativeBuffer)
                assertNotEquals(arrayBuffer.hashCode(), nativeBuffer.hashCode())
            }
        }
    }

    fun transfer(from: KBuffer, to: KBuffer) {
        from.put(123)
        from.putChar('*')
        from.putShort(12345)
        from.putInt(1234567890)
        from.putLong(1234567890123456L)
        from.putFloat(123456.789f)
        from.putDouble(123456789.987654321)
        from.flip()

        to.putBytes(from)
        to.flip()

        assertEquals(29, to.remaining)

        assertEquals(123, to.read())
        assertEquals('*', to.readChar())
        assertEquals(12345, to.readShort())
        assertEquals(1234567890, to.readInt())
        assertEquals(1234567890123456L, to.readLong())
        assertEquals(123456.789f, to.readFloat())
        assertEquals(123456789.987654321, to.readDouble())

        assertEquals(0, to.remaining)
    }

    @Test
    fun transferNativeToArray() {
        Allocation.array(DEFAULT_SIZE).use { arrayBuffer ->
            Allocation.native(DEFAULT_SIZE).use { nativeBuffer ->
                transfer(nativeBuffer, arrayBuffer)
            }
        }
    }

    @Test
    fun transferArrayToNative() {
        Allocation.array(DEFAULT_SIZE).use { arrayBuffer ->
            Allocation.native(DEFAULT_SIZE).use { nativeBuffer ->
                transfer(arrayBuffer, nativeBuffer)
            }
        }
    }

}
