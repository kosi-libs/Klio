package org.kodein.memory.io

import org.kodein.memory.assertNear
import org.kodein.memory.text.readChar
import org.kodein.memory.text.writeChar
import org.kodein.memory.use
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class TransferTests : AbstractIOTests() {

    companion object {
        const val DEFAULT_SIZE = 16384
    }

    @Test
    fun equalsAndHashcode() {
        fun Memory.dataSlice() = slice {
            writeByte(123)
            writeChar('*')
            writeShort(12345)
            writeInt(1234567890)
            writeLong(1234567890123456L)
            writeFloat(1234.56f)
            writeDouble(123456789.987654321)
        }

        val arraySlice = Memory.array(DEFAULT_SIZE).dataSlice()
        val nativeSlice = Allocation.native(DEFAULT_SIZE).useInScope().dataSlice()

        assertEquals(arraySlice, nativeSlice)
        assertEquals(arraySlice.hashCode(), nativeSlice.hashCode())

        val arraySubSlice = arraySlice.sliceAt(4)
        val nativeSubSlice = nativeSlice.sliceAt(4)

        assertEquals(arraySubSlice, nativeSubSlice)
        assertEquals(arraySubSlice.hashCode(), nativeSubSlice.hashCode())

        assertNotEquals(arraySlice, nativeSubSlice)
        assertNotEquals(arraySubSlice, nativeSlice)
        assertNotEquals(arraySlice.hashCode(), nativeSubSlice.hashCode())
        assertNotEquals(arraySubSlice.hashCode(), nativeSlice.hashCode())
    }

    private fun transfer(from: Memory, to: Memory) {
        val fromSlice = from.slice {
            writeByte(123)
            writeChar('*')
            writeShort(12345)
            writeInt(1234567890)
            writeLong(1234567890123456L)
            writeFloat(1234.56f)
            writeDouble(123456789.987654321)
        }

        val slice = to.slice { writeBytes(fromSlice.asReadable()) } .asReadable()

        assertEquals(28, slice.remaining)

        assertEquals(123, slice.readByte())
        assertEquals('*', slice.readChar())
        assertEquals(12345, slice.readShort())
        assertEquals(1234567890, slice.readInt())
        assertEquals(1234567890123456L, slice.readLong())
        assertNear(1234.56f, slice.readFloat())
        assertEquals(123456789.987654321, slice.readDouble())

        assertEquals(0, slice.remaining)
    }

    @Test
    fun transferNativeToArray() {
        val arrayMemory = Memory.array(DEFAULT_SIZE)
        Allocation.native(DEFAULT_SIZE).use { nativeMemory ->
            transfer(nativeMemory, arrayMemory)
        }
    }

    @Test
    fun transferArrayToNative() {
        val arrayMemory = Memory.array(DEFAULT_SIZE)
        Allocation.native(DEFAULT_SIZE).use { nativeMemory ->
            transfer(arrayMemory, nativeMemory)
        }
    }

}
