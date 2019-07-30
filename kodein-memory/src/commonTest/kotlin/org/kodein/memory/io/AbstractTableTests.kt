package org.kodein.memory.io

import org.kodein.memory.assertNear
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

abstract class AbstractTableTests {

    companion object {
        const val DEFAULT_SIZE = 16384
    }

    abstract fun sliceBuilder(size: Int = DEFAULT_SIZE): SliceBuilder

    @Test
    fun testPrimitives() {
        val buffer = sliceBuilder().newSlice {
            putTable {
                byte("byte", 21)
                char("char", 'c')
                short("short", 2142)
                int("int", 21426384)
                long("long", 21426384105126147)
                float("float", 21.42f)
                double("double", 2142.6384)
                string("string", "Ä ẁéïŕð śẗríñǵ ẁìẗĥ ŝẗŕäńgë çhårâćtêrŝ")
                booleans("booleans", booleanArrayOf(false, true, false, false, true, true, false, false, false, true, true, true))
                bytes("bytes", byteArrayOf(21, 42, 63, 84, 105, 126))
            }
        }

        val table = buffer.readTable()

        assertFalse(buffer.hasRemaining())

        assertEquals(10, table.size)

        assertEquals(0, table.index("byte"))
        assertEquals("byte", table.name(0))
        assertEquals(1, table.index("char"))
        assertEquals("char", table.name(1))
        assertEquals(2, table.index("short"))
        assertEquals("short", table.name(2))
        assertEquals(3, table.index("int"))
        assertEquals("int", table.name(3))
        assertEquals(4, table.index("long"))
        assertEquals("long", table.name(4))
        assertEquals(5, table.index("float"))
        assertEquals("float", table.name(5))
        assertEquals(6, table.index("double"))
        assertEquals("double", table.name(6))
        assertEquals(7, table.index("string"))
        assertEquals("string", table.name(7))
        assertEquals(8, table.index("booleans"))
        assertEquals("booleans", table.name(8))
        assertEquals(9, table.index("bytes"))
        assertEquals("bytes", table.name(9))

        assertEquals(21, table.byte("byte"))
        assertEquals('c', table.char("char"))
        assertEquals(2142, table.short("short"))
        assertEquals(21426384, table.int("int"))
        assertEquals(21426384105126147, table.long("long"))
        assertNear(21.42f, table.float("float"))
        assertEquals(2142.6384, table.double("double"))
        assertEquals("Ä ẁéïŕð śẗríñǵ ẁìẗĥ ŝẗŕäńgë çhårâćtêrŝ", table.string("string"))
        assertEquals("FTFFTTFFFTTTFFFF", table.booleans("booleans").joinToString("") { if (it) "T" else "F" })
        assertEquals("21, 42, 63, 84, 105, 126", table.bytes("bytes").joinToString())
    }

}
