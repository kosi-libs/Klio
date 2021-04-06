package org.kodein.memory.crypto

import org.khronos.webgl.Uint8Array
import org.kodein.memory.crypto.lib.pbkdf2.pbkdf2Sync
import org.kodein.memory.io.*


public actual object Pbkdf2 {

    private fun toUInt8Array(memory: ReadMemory): Uint8Array = when (val m = memory.internalMemory()) {
        is ArrayBufferMemory -> m.uint8Array
        else -> ArrayBufferMemory.copyOf(memory).uint8Array
    }

    public actual fun withHmac(digestAlgorithm: DigestAlgorithm, password: ReadMemory, salt: ReadMemory, rounds: Int, dst: Memory) {
        val derivedKey = pbkdf2Sync(
            toUInt8Array(password),
            toUInt8Array(salt),
            rounds,
            dst.size,
            digestAlgorithm.jsAlgorithmName
        )
        dst.putBytes(0, ArrayBufferMemory(derivedKey))
    }
}
