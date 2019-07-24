package org.kodein.memory.util

import kotlin.test.*

class IdentityWeakMapTests {

    data class Key(val i: Int)

    @Test
    fun putGetDelete() {
        val wm = WeakIdentityMap<Key, String>()

        val key = Key(42)

        assertFalse(key in wm)
        assertNull(wm[key])

        wm[key] = "test"

        assertTrue(key in wm)
        assertEquals("test", wm[key])

        wm.delete(key)

        assertFalse(key in wm)
        assertNull(wm[key])
    }

    @Test
    fun identityOnly() {
        val wm = WeakIdentityMap<Key, String>()

        val key = Key(42)

        wm.set(key, "test")

        assertFalse(Key(42) in wm)
        assertNull(wm[Key(42)])
    }

}
