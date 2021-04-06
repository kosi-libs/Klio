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


class ShaTests {

    private val results = listOf(
        DigestAlgorithm.SHA1   to "8b6ccb43dca2040c3cfbcd7bfff0b387d4538c33",
        DigestAlgorithm.SHA224 to "95d3fe7c2d46ccf69357159bc6f65540f8cbe48ee13464be4898350b",
        DigestAlgorithm.SHA256 to "54ba1fdce5a89e0d3eee6e4c587497833bc38c3586ff02057dd6451fd2d6b640",
        DigestAlgorithm.SHA384 to "85ad7920556a9ded0cead8e9e90092a32c7ce11e0225065cce037a45a58c2aa3959e2e216fd159c41a2cd16e68ab7a49",
        DigestAlgorithm.SHA512 to "d4d6331e89ced845639272bc64ca3ef4e94a57c88431c61aef91f4399e30c6ada32c042f72cedad9cb1c7cfaf04d92e06ad044b557ca16f554f1c6d66b06d0e0"
    )

    @Test
    fun array() {
        val input = Memory.array("This is a test!")
        for ((algo, result) in results) {
            assertEquals(algo.hashOf(input).toHex(), result)
        }
    }

    @Test
    fun native() {
        Allocation.native("This is a test!").use { input ->
            for ((algo, result) in results) {
                assertEquals(algo.hashOf(input).toHex(), result)
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
            assertEquals(algo.hashOf { writeInputValues() } .toHex(), result)
        }
    }

}
