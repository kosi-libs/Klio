package org.kodein.memory.io

import org.kodein.memory.text.readChar
import org.kodein.memory.text.writeChar
import org.kodein.memory.use
import kotlin.test.Test
import kotlin.test.assertEquals

abstract class AbstractSliceBuilderTests : AbstractIOTests() {

    companion object {
        const val DEFAULT_SIZE = 16384
    }

    abstract fun sliceBuilder(size: Int = DEFAULT_SIZE): SliceBuilder

    @Test
    fun simpleSlices() {
        val sb = sliceBuilder().useInScope()
        val s1 = sb.slice {
            writeShort(2142)
            writeShort(4221)
        }.asReadable()
        val s2 = sb.slice {
            writeChar('É')
            writeChar('À')
        }.asReadable()
        assertEquals(4, s1.remaining)
        assertEquals(4, s2.remaining)
        assertEquals(2142, s1.readShort())
        assertEquals(4221, s1.readShort())
        assertEquals('É', s2.readChar())
        assertEquals('À', s2.readChar())
        assertEquals(0, s1.remaining)
        assertEquals(0, s2.remaining)
    }

    @Test
    fun slicesInRemap() {
        val sb = sliceBuilder(5).useInScope()
        val s1 = sb.slice { writeInt(1234567890) }
        assertEquals(4, s1.size)
        assertEquals(1, sb.allocationCount)
        assertEquals(5, sb.allocationSize)
        val s2 = sb.slice { writeInt(987654321) ; writeChar('Ç') ; writeByte(42) ; writeInt(1234567890) }
        assertEquals(11, s2.size)
        assertEquals(2, sb.allocationCount)
        assertEquals(25, sb.allocationSize)
        val s3 = sb.slice { writeInt(1234567890) }
        assertEquals(4, s3.size)
        assertEquals(2, sb.allocationCount)
        assertEquals(25, sb.allocationSize)
    }

    @Test
    fun simpleSubSlice() {
        val sb = sliceBuilder().useInScope()
        lateinit var sub: Readable

        val slice = sb.slice {
            writeChar('a')
            sub = subSlice {
                writeChar('b')
                writeChar('c')
            }.asReadable()
            writeChar('d')
        }.asReadable()

        assertEquals('a', slice.readChar())
        assertEquals('b', slice.readChar())
        assertEquals('c', slice.readChar())
        assertEquals('d', slice.readChar())

        assertEquals('b', sub.readChar())
        assertEquals('c', sub.readChar())
    }

    @Test
    fun subSliceInRemap() {
        sliceBuilder(3).use {
            lateinit var sub: Readable

            val slice = it.slice {
                writeChar('a')
                sub = subSlice {
                    writeChar('b')
                    writeChar('c')
                }.asReadable()
                writeChar('d')
                writeChar('e')
                writeChar('f')
            }.asReadable()

            assertEquals('a', slice.readChar())
            assertEquals('b', slice.readChar())
            assertEquals('c', slice.readChar())
            assertEquals('d', slice.readChar())

            assertEquals('b', sub.readChar())
            assertEquals('c', sub.readChar())
        }
    }
}
