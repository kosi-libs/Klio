package org.kodein.memory.crypto

import kotlinx.cinterop.*
import org.kodein.memory.crypto.libssl.PKCS5_PBKDF2_HMAC
import org.kodein.memory.io.*


public actual object Pbkdf2 {

    public actual fun withHmac(digestAlgorithm: DigestAlgorithm, password: ReadMemory, salt: ReadMemory, rounds: Int, dst: Memory) {
        val (_, evp) = digestAlgorithm.linuxDigest()

        memScoped {
            val nPassword = toReadOnlyCPointerMemory(password)
            val nSalt = toReadOnlyCPointerMemory(salt)
            val nDst = toReadWriteCPointerMemory(dst)

            PKCS5_PBKDF2_HMAC(
                    nPassword.pointer, nPassword.size,
                    nSalt.pointer.reinterpret(), nSalt.size,
                    rounds, evp,
                    nDst.size, nDst.pointer.reinterpret()
            ).requireOpenSSLSuccess("PKCS5_PBKDF2_HMAC")
        }
    }
}
