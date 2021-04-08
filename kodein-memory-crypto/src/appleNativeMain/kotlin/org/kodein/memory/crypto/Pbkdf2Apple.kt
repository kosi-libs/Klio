package org.kodein.memory.crypto

import kotlinx.cinterop.*
import org.kodein.memory.io.*
import org.kodein.memory.*
import platform.CoreCrypto.*
import platform.KCoreCrypto.KCCKeyDerivationPBKDF


public actual object Pbkdf2 {

    @OptIn(ExperimentalUnsignedTypes::class)
    public actual fun withHmac(digestAlgorithm: DigestAlgorithm, password: ReadMemory, salt: ReadMemory, rounds: Int, dst: Memory) {
        val prfHmacAlgorithm = when (digestAlgorithm) {
            DigestAlgorithm.SHA1 -> kCCPRFHmacAlgSHA1
            DigestAlgorithm.SHA224 -> kCCPRFHmacAlgSHA224
            DigestAlgorithm.SHA256 -> kCCPRFHmacAlgSHA256
            DigestAlgorithm.SHA384 -> kCCPRFHmacAlgSHA384
            DigestAlgorithm.SHA512 -> kCCPRFHmacAlgSHA512
        }

        memScoped {
            val nPassword = toReadOnlyCPointerMemory(password)
            val nSalt = toReadOnlyCPointerMemory(salt)
            val nDst = toReadWriteCPointerMemory(dst)

            KCCKeyDerivationPBKDF(
                kCCPBKDF2,
                nPassword.pointer, nPassword.size.convert(),
                nSalt.pointer.reinterpret(), nSalt.size.convert(),
                prfHmacAlgorithm, rounds.toUInt(),
                nDst.pointer.reinterpret(), nDst.size.convert()
            )
        }

    }
}
