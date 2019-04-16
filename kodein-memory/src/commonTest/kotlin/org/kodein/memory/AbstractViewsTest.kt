package org.kodein.memory

import kotlin.test.Test
import kotlin.test.assertEquals

abstract class AbstractViewsTest {

    companion object {
        const val DEFAULT_SIZE = 16384
    }

    abstract fun viewMaker(size: Int = DEFAULT_SIZE): SliceBuilder

    @Test
    fun views() {
        viewMaker().use {
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
    fun small() {
        viewMaker(5).use {
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
}
