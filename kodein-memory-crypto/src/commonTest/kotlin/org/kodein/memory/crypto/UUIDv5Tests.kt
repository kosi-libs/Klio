package org.kodein.memory.crypto

import org.kodein.memory.io.Memory
import org.kodein.memory.text.array
import org.kodein.memory.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals

class UUIDv5Tests {

    @Test
    fun uuidv5() {
        val uuid = UUID.v5From(UUIDv5Namespaces.URL, Memory.array("https://kodein.org"))

        assertEquals("6444da04-6770-50f0-82dd-d1154fc4c1d7", uuid.toString())
    }

}
