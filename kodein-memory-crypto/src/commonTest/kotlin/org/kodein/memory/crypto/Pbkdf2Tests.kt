package org.kodein.memory.crypto

import org.kodein.memory.io.Memory
import org.kodein.memory.io.asMemory
import org.kodein.memory.text.array
import org.kodein.memory.text.fromHex
import org.kodein.memory.text.toHex
import kotlin.test.Test
import kotlin.test.assertEquals

class Pbkdf2Tests {

    private val results = listOf(
        DigestAlgorithm.SHA1   to "d443eae0f22b2029315d16fa74ce40124a445035d14f2bb21534495ff958d105",
        DigestAlgorithm.SHA224 to "0ab825e32b157442fd5541db25dce55732d9da1f4a84f2d730a9ec03b35d00ad",
        DigestAlgorithm.SHA256 to "9aa5e981cd93874d5e5e83cb6c8d003a7fd40389cc9a0c17d3bf7f3a335bcc15",
        DigestAlgorithm.SHA384 to "7f7ee96d1bf65084c930c25660ef06de291653f27898d6f7a0d84a1606cddee8",
        DigestAlgorithm.SHA512 to "8a49fc88f1988d74c7d002dfc9dbf2d23b516de5ead799d1ae6e37a6fbfa2f84"
    )

    private val key = Memory.array("secret-key")

    private val salt = "0123456789abcdef0123456789abcdef".fromHex().asMemory()

    @Test
    fun pbkdf2() {
        for ((algo, result) in results) {
            assertEquals(Pbkdf2.withHmac(algo, key, salt, 1000, 32).toHex(), result)
        }
    }

}
