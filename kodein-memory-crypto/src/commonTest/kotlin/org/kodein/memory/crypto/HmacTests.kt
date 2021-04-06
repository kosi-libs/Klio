package org.kodein.memory.crypto

import org.kodein.memory.io.Allocation
import org.kodein.memory.io.Memory
import org.kodein.memory.io.Writeable
import org.kodein.memory.text.array
import org.kodein.memory.text.native
import org.kodein.memory.text.toHex
import org.kodein.memory.use
import kotlin.test.Test
import kotlin.test.assertEquals


class HmacTests {
    private val results = listOf(
        DigestAlgorithm.SHA1   to "c02cd76be815fc56e3a395bea9dcee33e94ee9b9",
        DigestAlgorithm.SHA224 to "64edd51203ef58d91bee9396b994b4dc5cf3f8f0abdfeb081c6ac3ee",
        DigestAlgorithm.SHA256 to "3f1b482abd5da9037a02559488a098eaef6250e93763ae690dd263a126429332",
        DigestAlgorithm.SHA384 to "e1c0fdf536192e032e37b883be38d028554e824b31fb5d3eb5a83086d3f9711aace49c4e408b81555c68f9e16a4200b7",
        DigestAlgorithm.SHA512 to "b347b6bef1ea04b499f45c9aa2269ec821cff61c5854b733297b2b49602251beec8fc90b9bb6e1ab63988909fdfeb806222527785a4a53f47496819b4ccabbe2"
    )

    private val key = Memory.array("secret-key")

    @Test
    fun array() {
        val input = Memory.array("This is a test!")
        for ((algo, result) in results) {
            assertEquals(algo.hmacOf(key, input).toHex(), result)
        }
    }

    @Test
    fun native() {
        Allocation.native("This is a test!").use { input ->
            for ((algo, result) in results) {
                assertEquals(algo.hmacOf(key, input).toHex(), result)
            }
        }
    }

    @Test
    fun values() {
        fun Writeable.writeInputValues() {
            writeByte(0x54)
            writeShort(0x6869)
            writeInt(0x73206973)
            writeLong(0x2061207465737421)
        }

        for ((algo, result) in results) {
            assertEquals(algo.hmacOf(key) { writeInputValues() } .toHex(), result)
        }
    }

}
