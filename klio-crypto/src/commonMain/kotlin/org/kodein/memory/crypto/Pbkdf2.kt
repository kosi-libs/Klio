package org.kodein.memory.crypto

import org.kodein.memory.io.Memory
import org.kodein.memory.io.ReadMemory
import org.kodein.memory.io.array


public expect object Pbkdf2 {
    public fun withHmac(digestAlgorithm: DigestAlgorithm, password: ReadMemory, salt: ReadMemory, rounds: Int, dst: Memory)
}

public fun Pbkdf2.withHmac(digestAlgorithm: DigestAlgorithm, password: ReadMemory, salt: ReadMemory, rounds: Int, dkLen: Int): ByteArray {
    val dk = Memory.array(dkLen)
    withHmac(digestAlgorithm, password, salt, rounds, dk)
    return dk.array
}
