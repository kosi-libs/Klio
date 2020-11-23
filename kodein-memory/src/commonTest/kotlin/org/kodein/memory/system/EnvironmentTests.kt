package org.kodein.memory.system

import kotlin.test.*

abstract class EnvironmentTests {

    @Test
    fun getVariable() {
        assertEquals("Working!", Environment.getVariable("TEST_VARIABLE"))
    }

    @Test
    fun findVariable() {
        assertEquals("Working!", Environment.findVariable("TEST_VARIABLE"))
        assertNull(Environment.findVariable("THIS_DOES_NOT_EXIST"))
    }

    @Test
    fun allVariables() {
        val all = Environment.allVariables()
        assertTrue("TEST_VARIABLE" in all)
        assertEquals("Working!", all["TEST_VARIABLE"])
        assertFalse("THIS_DOES_NOT_EXIST" in all)
    }

}
