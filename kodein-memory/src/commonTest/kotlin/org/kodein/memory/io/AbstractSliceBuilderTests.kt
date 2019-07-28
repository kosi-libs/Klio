package org.kodein.memory.io

import org.kodein.memory.use
import kotlin.test.Test
import kotlin.test.assertEquals

abstract class AbstractSliceBuilderTests {

    companion object {
        const val DEFAULT_SIZE = 16384
    }

    abstract fun sliceBuilder(size: Int = DEFAULT_SIZE): SliceBuilder

    @Test
    fun simpleSlices() {
        sliceBuilder().use {
            val v1 = it.newSlice {
                putShort(2142)
                putShort(4221)
            }
            val v2 = it.newSlice {
                putChar('S')
                putChar('B')
            }
            assertEquals(4, v1.remaining)
            assertEquals(4, v2.remaining)
            assertEquals(2142, v1.readShort())
            assertEquals(4221, v1.readShort())
            assertEquals('S', v2.readChar())
            assertEquals('B', v2.readChar())
            assertEquals(0, v1.remaining)
            assertEquals(0, v2.remaining)
        }
    }

    @Test
    fun slicesInRemap() {
        sliceBuilder(5).use {
            val v1 = it.newSlice { putInt(1234567890) }
            assertEquals(0, v1.position)
            assertEquals(4, v1.limit)
            assertEquals(4, v1.capacity)
            assertEquals(1, it.allocationCount)
            assertEquals(5, it.allocationSize)
            val v2 = it.newSlice { putInt(987654321) ; putChar('S') ; put(42) ; putInt(1234567890) }
            assertEquals(0, v2.position)
            assertEquals(11, v2.limit, "LIMIT")
            assertEquals(11, v2.capacity, "CAPACITY")
            assertEquals(2, it.allocationCount)
            assertEquals(25, it.allocationSize)
            val v3 = it.newSlice { putInt(1234567890) }
            assertEquals(0, v3.position)
            assertEquals(4, v3.limit)
            assertEquals(4, v3.capacity)
            assertEquals(2, it.allocationCount)
            assertEquals(25, it.allocationSize)
        }
    }

    @Test
    fun simpleSubSlice() {
        sliceBuilder().use {
            lateinit var sub: ReadBuffer

            val slice = it.newSlice {
                putChar('a')
                sub = subSlice {
                    putChar('b')
                    putChar('c')
                }
                putChar('d')
            }

            assertEquals('a', slice.readChar())
            assertEquals('b', slice.readChar())
            assertEquals('c', slice.readChar())
            assertEquals('d', slice.readChar())

            assertEquals('b', sub.readChar())
            assertEquals('c', sub.readChar())
        }
    }

    @Test
    fun subSliceInRemap() {
        sliceBuilder(3).use {
            lateinit var sub: ReadBuffer

            val slice = it.newSlice {
                putChar('a')
                sub = subSlice {
                    putChar('b')
                    putChar('c')
                }
                putChar('d')
                putChar('e')
                putChar('f')
            }

            assertEquals('a', slice.readChar())
            assertEquals('b', slice.readChar())
            assertEquals('c', slice.readChar())
            assertEquals('d', slice.readChar())

            assertEquals('b', sub.readChar())
            assertEquals('c', sub.readChar())
        }
    }
}
