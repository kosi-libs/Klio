package org.kodein.memory.crypto

import org.kodein.memory.io.Memory
import org.kodein.memory.io.ReadMemory
import org.kodein.memory.io.getBytes
import org.kodein.memory.io.putBytes
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec


public actual object Pbkdf2 {

    public actual fun withHmac(digestAlgorithm: DigestAlgorithm, password: ReadMemory, salt: ReadMemory, rounds: Int, dst: Memory) {
        val algorithmName = when (digestAlgorithm) {
            DigestAlgorithm.SHA1 -> "PBKDF2WithHmacSHA1"
            DigestAlgorithm.SHA256 -> "PBKDF2WithHmacSHA256"
            DigestAlgorithm.SHA384 -> "PBKDF2WithHmacSHA384"
            DigestAlgorithm.SHA512 -> "PBKDF2WithHmacSHA512"
        }

        val derivedKey = SecretKeyFactory.getInstance(algorithmName)
            .generateSecret(
                PBEKeySpec(
                    CharArray(password.size) { password[it].toInt().toChar() },
                    salt.getBytes(),
                    rounds,
                    dst.size * 8
                )
            )
            .encoded

        dst.putBytes(0, derivedKey)
    }

}
