package org.kodein.memory.io

import org.kodein.memory.text.putString
import org.kodein.memory.text.readString
import kotlin.test.Test
import kotlin.test.assertEquals

abstract class AbstractExpandableBufferTests {

    abstract fun alloc(size: Int): Allocation

    inner class Allocator {
        val allocations = ArrayList<Int>()

        fun alloc(size: Int): Allocation {
            allocations.add(size)
            return this@AbstractExpandableBufferTests.alloc(size)
        }
    }

    @Test
    fun hasRoom() {
        val allocator = Allocator()

        val buffer = ExpandableBuffer(4, allocator::alloc)
        buffer.putString("Te")
        buffer.flip()
        buffer.skip(2)
        buffer.resetHere()
        buffer.putString("st")
        buffer.flip()

        assertEquals(listOf(4), allocator.allocations)
        assertEquals("Test", buffer.readString())

        buffer.resetHere()
        buffer.putString("ing")
        buffer.flip()

        assertEquals(listOf(4, 8), allocator.allocations)
        assertEquals("Testing", buffer.readString())
    }

    fun noCopyAfterPos() {
        val allocator = Allocator()

        val buffer = ExpandableBuffer(4, allocator::alloc)
        buffer.putString("Test")
        buffer.flip()

    }

}
