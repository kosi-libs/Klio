package org.kodein.memory.io

import org.kodein.memory.text.readString
import org.kodein.memory.text.writeString
import kotlin.test.Test
import kotlin.test.assertEquals

abstract class AbstractReusableTests : AbstractIOTests() {

    abstract fun alloc(size: Int): Allocation

    inner class Allocator {
        val allocations = ArrayList<Int>()

        fun alloc(size: Int): Allocation {
            allocations.add(size)
            return this@AbstractReusableTests.alloc(size)
        }
    }

    @Test
    fun hasRoom() {
        val allocator = Allocator()

        val alloc = ReusableAllocation(4, allocator::alloc).useInScope()
        alloc.slice {
            writeString("Te")
        }
        val slice1 = alloc.slice {
            skip(2)
            writeString("st")
        }

        assertEquals(listOf(4), allocator.allocations)
        assertEquals("Test", slice1.asReadable().readString())

        val slice2 = alloc.slice {
            skip(4)
            writeString("ing")
        }

        assertEquals(listOf(4, 8), allocator.allocations)
        assertEquals("Testing", slice2.asReadable().readString())
    }

    @Test
    fun noCopyAfterPos() {
        val allocator = Allocator()

        val alloc = ReusableAllocation(4, allocator::alloc).useInScope()
        alloc.slice {
            writeString("Test")
        }

        val slice = alloc.slice {
            skip(3)
            requestCanWrite(10)
        }

        assertEquals(listOf(4, 16), allocator.allocations)
        assertEquals(3, alloc.bytesCopied)

        assertEquals("Tes", slice.asReadable().readString())
    }

}
