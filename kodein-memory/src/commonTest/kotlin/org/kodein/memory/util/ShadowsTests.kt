package org.kodein.memory.util

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ShadowsTests {

    @Test
    fun shadows() {

        val ex = IllegalStateException()
        ex.addShadowed(IllegalArgumentException("0"))
        ex.addShadowed(NoSuchElementException("1"))

        assertNull(ex.cause)

        val shadowed = ex.getShadowed()
        assertTrue(shadowed[0] is IllegalArgumentException)
        assertEquals("0", shadowed[0].message)
        assertTrue(shadowed[1] is NoSuchElementException)
        assertEquals("1", shadowed[1].message)
    }

}
